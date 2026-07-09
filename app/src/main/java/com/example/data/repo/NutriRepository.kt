package com.example.data.repo

import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.NutriDao
import com.example.data.model.ChatMessage
import com.example.data.model.LoggedFood
import com.example.data.model.UserGoal
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FoodScanResult(
    val name: String,
    val quantity: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val ingredients: String,
    val error: String? = null
)

data class CoachResponse(
    val reply: String,
    val foodToLog: LoggedFood? = null
)

class NutriRepository(private val nutriDao: NutriDao) {

    // Logged Foods
    fun getFoodsForDate(date: String): Flow<List<LoggedFood>> = nutriDao.getFoodsForDate(date)

    suspend fun insertFood(food: LoggedFood) = nutriDao.insertFood(food)

    suspend fun deleteFoodById(id: Int) = nutriDao.deleteFoodById(id)

    // User Goals
    fun getUserGoal(): Flow<UserGoal?> = nutriDao.getUserGoal()

    suspend fun getUserGoalOnce(): UserGoal {
        return nutriDao.getUserGoalOnce() ?: UserGoal() // Returns default goal if not set yet
    }

    suspend fun saveUserGoal(goal: UserGoal) = nutriDao.saveUserGoal(goal)

    // Water Logs
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>> = nutriDao.getWaterLogsForDate(date)

    suspend fun insertWaterLog(log: WaterLog) = nutriDao.insertWaterLog(log)

    suspend fun deleteLastWaterLog(date: String) = nutriDao.deleteLastWaterLog(date)

    // Chat Messages
    fun getAllChatMessages(): Flow<List<ChatMessage>> = nutriDao.getAllChatMessages()

    suspend fun insertChatMessage(msg: ChatMessage) = nutriDao.insertChatMessage(msg)

    suspend fun clearChat() = nutriDao.clearChat()

    // Scan Feedback continuous learning operations
    suspend fun insertFeedback(feedback: com.example.data.model.ScanFeedback) = nutriDao.insertFeedback(feedback)
    suspend fun getRecentFeedbacksOnce(): List<com.example.data.model.ScanFeedback> = nutriDao.getRecentFeedbacksOnce()
    fun getRecentFeedbacks(): Flow<List<com.example.data.model.ScanFeedback>> = nutriDao.getRecentFeedbacks()

    // Gemini API: Analyze Food Image
    suspend fun analyzeFoodImage(base64Image: String, apiKey: String): FoodScanResult {
        val recentFeedbacks = nutriDao.getRecentFeedbacksOnce()
        val feedbackPromptSection = if (recentFeedbacks.isEmpty()) {
            ""
        } else {
            "\n\nCRITICAL - USER LEARNING FEEDBACK HISTORY (Use this to align your estimates with user's feedback/corrections to improve precision):\n" +
            recentFeedbacks.joinToString("\n") { fb ->
                "- Food: '${fb.foodName}', original prediction: ${fb.originalCalories} kcal with ingredients [${fb.originalIngredients}]. User corrected to: ${fb.correctedCalories} kcal with ingredients [${fb.correctedIngredients}]. User note: ${fb.feedbackText} (Rating: ${if (fb.isPositive) "Good" else "Incorrect"})"
            } + "\nAdjust your portions, calorie estimates, and ingredient listings accordingly based on the above preferences. Don't repeat previous calibration errors!"
        }

        val prompt = """
            You are a top-tier visual food recognition and nutrition estimation AI. Analyze the uploaded food image. 
            Provide a strictly structured JSON response representing the recognized food item, estimated weight/portion, calories, and macronutrients (protein, carbs, fat).
            Do not include any explanation, markdown formatting blocks, or text outside the JSON.
            The JSON must be an object with the following fields:
            {
              "name": "Food name (e.g., Avocado Toast with Egg)",
              "quantity": "Estimated portion (e.g., 1 serving, 250g)",
              "calories": 380,
              "protein": 14.5,
              "carbs": 32.0,
              "fat": 22.0,
              "ingredients": "avocado, whole wheat bread, poached egg, chili flakes"
            }
            If the image does not contain any recognizable food, return an object with an "error" key:
            {
              "error": "Could not identify any food in this image. Please capture a clear photo of your meal."
            }
            $feedbackPromptSection
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", "No response from AI")

            val cleanedText = cleanJsonString(rawText)
            val json = JSONObject(cleanedText)

            if (json.has("error")) {
                FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", json.getString("error"))
            } else {
                FoodScanResult(
                    name = json.optString("name", "Unknown Food"),
                    quantity = json.optString("quantity", "1 serving"),
                    calories = json.optInt("calories", 0),
                    protein = json.optDouble("protein", 0.0),
                    carbs = json.optDouble("carbs", 0.0),
                    fat = json.optDouble("fat", 0.0),
                    ingredients = json.optString("ingredients", "")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", "Error: ${e.localizedMessage}")
        }
    }

    // Weight Logs
    fun getAllWeightLogs(): Flow<List<com.example.data.model.WeightLog>> = nutriDao.getAllWeightLogs()
    suspend fun insertWeightLog(weightLog: com.example.data.model.WeightLog) = nutriDao.insertWeightLog(weightLog)
    suspend fun deleteWeightLogById(id: Int) = nutriDao.deleteWeightLogById(id)

    // Gemini API: Parse natural language food descriptions
    suspend fun parseNaturalLanguageFood(description: String, apiKey: String): FoodScanResult {
        val prompt = """
            You are a top-tier nutritionist and food analysis AI. Analyze this natural language food description:
            "$description"
            Provide a strictly structured JSON response representing the recognized food item, estimated weight/portion, calories, and macronutrients (protein, carbs, fat).
            Do not include any explanation, markdown formatting blocks, or text outside the JSON.
            The JSON must be an object with the following fields:
            {
              "name": "Food name (e.g., Avocado Toast with Egg)",
              "quantity": "Estimated portion (e.g., 1 serving, 250g)",
              "calories": 380,
              "protein": 14.5,
              "carbs": 32.0,
              "fat": 22.0,
              "ingredients": "avocado, bread, egg"
            }
            If the description does not contain any recognizable food, return an object with an "error" key:
            {
              "error": "Could not identify any food or drinks in this description. Please try with more specific terms."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", "No response from AI")

            val cleanedText = cleanJsonString(rawText)
            val json = JSONObject(cleanedText)

            if (json.has("error")) {
                FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", json.getString("error"))
            } else {
                FoodScanResult(
                    name = json.optString("name", "Unknown Food"),
                    quantity = json.optString("quantity", "1 serving"),
                    calories = json.optInt("calories", 0),
                    protein = json.optDouble("protein", 0.0),
                    carbs = json.optDouble("carbs", 0.0),
                    fat = json.optDouble("fat", 0.0),
                    ingredients = json.optString("ingredients", "")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FoodScanResult("", "", 0, 0.0, 0.0, 0.0, "", "Error: ${e.localizedMessage}")
        }
    }

    // Gemini API: Chat with Coach
    suspend fun chatWithCoach(
        userMessage: String,
        history: List<ChatMessage>,
        goal: UserGoal,
        todayFoods: List<LoggedFood>,
        selectedDateString: String,
        apiKey: String
    ): CoachResponse {
        val consumedCalories = todayFoods.sumOf { it.calories }
        val consumedProtein = todayFoods.sumOf { it.protein }
        val consumedCarbs = todayFoods.sumOf { it.carbs }
        val consumedFat = todayFoods.sumOf { it.fat }

        val systemPrompt = """
            You are NutriAI Coach, a friendly, ultra-knowledgeable, and encouraging AI dietitian.
            You help the user reach their fitness and weight goals.
            The user's details: Age: ${goal.age}, Gender: ${goal.gender}, Height: ${goal.heightCm}cm, Weight: ${goal.weightKg}kg. Goal: ${goal.goalType}, Target Weight: ${goal.targetWeightKg}kg.
            Their daily targets: ${goal.targetCalories} kcal, ${goal.targetProtein}g Protein, ${goal.targetCarbs}g Carbs, ${goal.targetFat}g Fat.
            They are viewing date: $selectedDateString. For this date they have consumed: $consumedCalories / ${goal.targetCalories} kcal (P: ${String.format("%.1f", consumedProtein)}g, C: ${String.format("%.1f", consumedCarbs)}g, F: ${String.format("%.1f", consumedFat)}g).
            Here is their meal log for that day:
            ${
                if (todayFoods.isEmpty()) "No meals logged yet on this date."
                else todayFoods.joinToString("\n") { "- [${it.mealType}] ${it.name} (${it.quantity}): ${it.calories} kcal, P:${it.protein}g, C:${it.carbs}g, F:${it.fat}g" }
            }

            Engage in a friendly conversation. Give concise, actionable advice (max 2-3 sentences to keep it readable in a chat bubble).
            CRITICAL DIRECTIVE: You have the ability to log foods directly from chat. If the user tells you they ate or drank something (e.g. "I had two eggs for breakfast", "just logged 1 apple", "I ate some chicken salad for lunch"), respond with your normal advice and ALWAYS include a special XML-like tag at the very end of your response like this so the app can automatically parse and log it:
            <log_food name="Egg" meal="Breakfast" calories="140" protein="12.0" carbs="1.0" fat="10.0" quantity="2 eggs" />
            Make sure the tag parameters are accurately estimated. You can log for: Breakfast, Lunch, Dinner, or Snack.
            Only include the <log_food ... /> tag if the user explicitly mentions eating or drinking something that should be logged.
        """.trimIndent()

        // Merged history format for 100% stable REST calls
        val chatHistoryText = history.takeLast(10).joinToString("\n") { msg ->
            val speaker = if (msg.sender == "user") "User" else "Coach"
            "$speaker: ${msg.message}"
        }

        val fullPrompt = if (chatHistoryText.isNotEmpty()) {
            "$chatHistoryText\nUser: $userMessage\nCoach:"
        } else {
            "User: $userMessage\nCoach:"
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = fullPrompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawReply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I'm sorry, I couldn't process that. Can you try again?"

            // Parse for direct logging tag
            val logFoodTagPattern = """<log_food\s+[^>]*/>""".toRegex()
            val match = logFoodTagPattern.find(rawReply)

            var cleanReply = rawReply
            var foodToLog: LoggedFood? = null

            if (match != null) {
                val tag = match.value
                cleanReply = rawReply.replace(tag, "").trim()

                try {
                    val name = """name="([^"]+)"""".toRegex().find(tag)?.groupValues?.get(1) ?: "Unknown Food"
                    val meal = """meal="([^"]+)"""".toRegex().find(tag)?.groupValues?.get(1) ?: "Snack"
                    val calories = """calories="(\d+)"""".toRegex().find(tag)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val protein = """protein="([\d\.]+)"""".toRegex().find(tag)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val carbs = """carbs="([\d\.]+)"""".toRegex().find(tag)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val fat = """fat="([\d\.]+)"""".toRegex().find(tag)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    val quantity = """quantity="([^"]+)"""".toRegex().find(tag)?.groupValues?.get(1) ?: "1 serving"

                    foodToLog = LoggedFood(
                        name = name,
                        mealType = meal,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        quantity = quantity,
                        dateString = selectedDateString
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            CoachResponse(reply = cleanReply, foodToLog = foodToLog)
        } catch (e: Exception) {
            e.printStackTrace()
            CoachResponse(reply = "Sorry, I had trouble connecting to the network: ${e.localizedMessage}")
        }
    }

    private fun cleanJsonString(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substringAfter("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substringBeforeLast("```")
        }
        return cleaned.trim()
    }

    // Users authentication persistence
    suspend fun getUserByUsername(username: String): com.example.data.model.User? = nutriDao.getUserByUsername(username)
    suspend fun getUserByEmail(email: String): com.example.data.model.User? = nutriDao.getUserByEmail(email)
    suspend fun getUserByPhone(phoneNumber: String): com.example.data.model.User? = nutriDao.getUserByPhone(phoneNumber)
    suspend fun insertUser(user: com.example.data.model.User) = nutriDao.insertUser(user)

    // Bulk retrievals for Cloud Synchronization
    suspend fun getAllFoodsOnce(): List<LoggedFood> = nutriDao.getAllFoodsOnce()
    suspend fun getAllWaterLogsOnce(): List<WaterLog> = nutriDao.getAllWaterLogsOnce()
    suspend fun getAllWeightLogsOnce(): List<com.example.data.model.WeightLog> = nutriDao.getAllWeightLogsOnce()
    suspend fun getAllChatMessagesOnce(): List<ChatMessage> = nutriDao.getAllChatMessagesOnce()
    suspend fun getAllFeedbacksOnce(): List<com.example.data.model.ScanFeedback> = nutriDao.getAllFeedbacksOnce()
    suspend fun getAllUsersOnce(): List<com.example.data.model.User> = nutriDao.getAllUsersOnce()
}
