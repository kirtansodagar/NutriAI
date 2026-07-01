package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.db.NutriDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.LoggedFood
import com.example.data.model.UserGoal
import com.example.data.model.WaterLog
import com.example.data.repo.FoodScanResult
import com.example.data.repo.NutriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val result: FoodScanResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

sealed interface ChatUiState {
    object Idle : ChatUiState
    object Sending : ChatUiState
}

class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = androidx.room.Room.databaseBuilder(
        application,
        NutriDatabase::class.java,
        "nutri_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = NutriRepository(db.nutriDao())

    val apiKey: String = BuildConfig.GEMINI_API_KEY

    fun getApiKeyToUse(): String {
        val customKey = userGoal.value.customApiKey
        return if (customKey.isNotBlank()) customKey else apiKey
    }

    // Selected Date State
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Logged Foods for current selected date
    val loggedFoods: StateFlow<List<LoggedFood>> = _selectedDate
        .flatMapLatest { date -> repository.getFoodsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Water Logs for current selected date
    val waterLogs: StateFlow<List<WaterLog>> = _selectedDate
        .flatMapLatest { date -> repository.getWaterLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Goal
    val userGoal: StateFlow<UserGoal> = repository.getUserGoal()
        .flatMapLatest { goal ->
            val finalGoal = goal ?: UserGoal()
            MutableStateFlow(finalGoal)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoal())

    // Chat History
    val chatMessages: StateFlow<List<ChatMessage>> = repository.getAllChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scan Feedback/Continuous Learning History
    val recentFeedbacks: StateFlow<List<com.example.data.model.ScanFeedback>> = repository.getRecentFeedbacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weight Logs History
    val weightLogs: StateFlow<List<com.example.data.model.WeightLog>> = repository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanning UI State
    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    // Quick natural language logging state
    private val _quickLogState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val quickLogState: StateFlow<ScanUiState> = _quickLogState.asStateFlow()

    // Chat Sending UI State
    private val _chatUiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    init {
        // Pre-populate with welcome message from AI Coach if chat is empty
        viewModelScope.launch(Dispatchers.IO) {
            val goal = repository.getUserGoalOnce()
            if (goal == null) {
                // Initialize default goal
                repository.saveUserGoal(UserGoal())
            }
            repository.getAllChatMessages().collect { list ->
                if (list.isEmpty()) {
                    repository.insertChatMessage(
                        ChatMessage(
                            sender = "ai",
                            message = "Hi there! I am your AI Nutrition Coach. You can log food by talking to me (e.g. 'I had avocado toast for breakfast'), ask for recipe recommendations, or upload a photo of your meal to let me analyze it instantly! How can I help you today?"
                        )
                    )
                }
            }
        }
    }

    // Helper to get today's date
    fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Date navigation
    fun selectPreviousDay() {
        changeDayByAmount(-1)
    }

    fun selectNextDay() {
        changeDayByAmount(1)
    }

    fun selectToday() {
        _selectedDate.value = getTodayDateString()
    }

    private fun changeDayByAmount(amount: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = dateFormat.parse(_selectedDate.value) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, amount)
            _selectedDate.value = dateFormat.format(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Food logging operations
    fun logManualFood(name: String, mealType: String, calories: Int, protein: Double, carbs: Double, fat: Double, quantity: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val food = LoggedFood(
                name = name,
                mealType = mealType,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                quantity = quantity,
                dateString = _selectedDate.value
            )
            repository.insertFood(food)
        }
    }

    fun deleteFood(foodId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFoodById(foodId)
        }
    }

    // Water logging operations
    fun addWater(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = WaterLog(
                amountMl = amountMl,
                dateString = _selectedDate.value
            )
            repository.insertWaterLog(log)
        }
    }

    fun removeLastWater() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLastWaterLog(_selectedDate.value)
        }
    }

    // Goal updating with BMR auto-calculation
    fun updateUserProfile(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        activityLevel: String,
        goalType: String,
        targetWeightKg: Double,
        customApiKey: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Mifflin-St Jeor BMR calculation
            val bmr = if (gender.lowercase() == "male") {
                (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
            } else {
                (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
            }

            // TDEE calculation based on physical activity levels
            val multiplier = when (activityLevel.lowercase()) {
                "sedentary" -> 1.2
                "moderate" -> 1.55
                "active" -> 1.725
                else -> 1.375
            }
            val tdee = bmr * multiplier

            // Calorie budget based on target goals
            val targetCalories = when (goalType.lowercase()) {
                "weight loss" -> (tdee - 500).coerceAtLeast(1200.0).toInt()
                "muscle gain" -> (tdee + 350).toInt()
                else -> tdee.toInt() // Maintenance
            }

            // Macro targets calculation based on calories and weight goals
            val targetProtein = when (goalType.lowercase()) {
                "weight loss" -> (2.0 * weightKg).toInt()
                "muscle gain" -> (2.2 * weightKg).toInt()
                else -> (1.6 * weightKg).toInt()
            }

            // 25% of calories from healthy fats (9 kcal/g)
            val targetFat = ((targetCalories * 0.25) / 9.0).toInt()

            // Remaining calories allocated to carbohydrates (4 kcal/g)
            val carbsCalories = targetCalories - (targetProtein * 4) - (targetFat * 9)
            val targetCarbs = (carbsCalories / 4).coerceAtLeast(50)

            val updatedGoal = UserGoal(
                id = 1,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                goalType = goalType,
                targetWeightKg = targetWeightKg,
                targetCalories = targetCalories,
                targetProtein = targetProtein,
                targetCarbs = targetCarbs,
                targetFat = targetFat,
                targetWaterMl = if (weightKg > 80) 3000 else 2500,
                customApiKey = customApiKey
            )

            repository.saveUserGoal(updatedGoal)
        }
    }

    // Direct Image Scanning
    fun scanFoodImage(bitmap: Bitmap) {
        _scanUiState.value = ScanUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val base64 = compressAndConvertBitmap(bitmap)
            val activeKey = getApiKeyToUse()
            if (activeKey.isEmpty() || activeKey == "MY_GEMINI_API_KEY") {
                _scanUiState.value = ScanUiState.Error("Gemini API Key is not set! Please configure it in your Profile Screen or AI Studio Secrets Panel.")
                return@launch
            }
            val result = repository.analyzeFoodImage(base64, activeKey)
            if (result.error != null) {
                _scanUiState.value = ScanUiState.Error(result.error)
            } else {
                _scanUiState.value = ScanUiState.Success(result)
            }
        }
    }

    fun clearScanState() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun submitScanFeedback(
        foodName: String,
        originalCalories: Int,
        correctedCalories: Int,
        originalIngredients: String,
        correctedIngredients: String,
        feedbackText: String,
        isPositive: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val fb = com.example.data.model.ScanFeedback(
                foodName = foodName,
                originalCalories = originalCalories,
                correctedCalories = correctedCalories,
                originalIngredients = originalIngredients,
                correctedIngredients = correctedIngredients,
                feedbackText = feedbackText,
                isPositive = isPositive
            )
            repository.insertFeedback(fb)
        }
    }

    // Weight operations
    fun logWeight(weightKg: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = com.example.data.model.WeightLog(
                weightKg = weightKg,
                dateString = _selectedDate.value
            )
            repository.insertWeightLog(log)
            
            // Also update the current profile weight to keep things in perfect sync
            val goal = repository.getUserGoalOnce()
            updateUserProfile(
                weightKg = weightKg,
                heightCm = goal.heightCm,
                age = goal.age,
                gender = goal.gender,
                activityLevel = goal.activityLevel,
                goalType = goal.goalType,
                targetWeightKg = goal.targetWeightKg,
                customApiKey = goal.customApiKey
            )
        }
    }

    fun deleteWeightLog(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWeightLogById(id)
        }
    }

    // Direct Quick Description Logging
    fun clearQuickLogState() {
        _quickLogState.value = ScanUiState.Idle
    }

    fun quickLogFood(description: String, mealType: String) {
        if (description.isBlank()) return
        _quickLogState.value = ScanUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val activeKey = getApiKeyToUse()
            if (activeKey.isEmpty() || activeKey == "MY_GEMINI_API_KEY") {
                _quickLogState.value = ScanUiState.Error("Gemini API key is not set! Please enter a key in your Profile Screen or secrets panel.")
                return@launch
            }
            val result = repository.parseNaturalLanguageFood(description, activeKey)
            if (result.error != null) {
                _quickLogState.value = ScanUiState.Error(result.error)
            } else {
                val food = LoggedFood(
                    name = result.name,
                    mealType = mealType,
                    calories = result.calories,
                    protein = result.protein,
                    carbs = result.carbs,
                    fat = result.fat,
                    quantity = result.quantity,
                    dateString = _selectedDate.value
                )
                repository.insertFood(food)
                _quickLogState.value = ScanUiState.Success(result)
            }
        }
    }

    // Chat coaching
    fun sendChatMessage(message: String) {
        val userMsg = ChatMessage(sender = "user", message = message)
        _chatUiState.value = ChatUiState.Sending

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertChatMessage(userMsg)

            val activeKey = getApiKeyToUse()
            if (activeKey.isEmpty() || activeKey == "MY_GEMINI_API_KEY") {
                repository.insertChatMessage(
                    ChatMessage(
                        sender = "ai",
                        message = "I see your message, but the Gemini API Key is missing. Please add your GEMINI_API_KEY in the Profile Screen or the secrets panel to start chatting with your coach!"
                    )
                )
                _chatUiState.value = ChatUiState.Idle
                return@launch
            }

            val goal = userGoal.value
            val todayFoods = loggedFoods.value
            val history = chatMessages.value

            val response = repository.chatWithCoach(message, history, goal, todayFoods, _selectedDate.value, activeKey)

            repository.insertChatMessage(
                ChatMessage(sender = "ai", message = response.reply)
            )

            // If a food log was parsed, write it automatically!
            if (response.foodToLog != null) {
                repository.insertFood(response.foodToLog)
                repository.insertChatMessage(
                    ChatMessage(
                        sender = "ai",
                        message = "📝 System: I've automatically added *${response.foodToLog.name}* (${response.foodToLog.quantity}) to your *${response.foodToLog.mealType}* log! (${response.foodToLog.calories} kcal)"
                    )
                )
            }

            _chatUiState.value = ChatUiState.Idle
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
            // insert welcome message back
            repository.insertChatMessage(
                ChatMessage(
                    sender = "ai",
                    message = "Hi! Let's start fresh. Tell me what you ate today or ask me a nutrition question!"
                )
            )
        }
    }

    // Secure live-test for user custom API keys
    fun testApiKey(keyToTest: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (keyToTest.isBlank()) {
                onResult(false, "API key cannot be empty.")
                return@launch
            }
            try {
                val requestBody = com.example.data.api.GenerateContentRequest(
                    contents = listOf(
                        com.example.data.api.Content(
                            parts = listOf(com.example.data.api.Part(text = "Ping test. Please respond with exactly 'OK'"))
                        )
                    )
                )
                val response = com.example.data.api.RetrofitClient.service.generateContent(
                    apiKey = keyToTest.trim(),
                    request = requestBody
                )
                val hasCandidates = !response.candidates.isNullOrEmpty()
                if (hasCandidates) {
                    onResult(true, "Key verified successfully! Connection is live and active.")
                } else {
                    onResult(false, "API returned an empty candidates block. Please check details.")
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "404 Not Found or Invalid Key"
                onResult(false, "Validation failed: $msg")
            }
        }
    }

    // Helper: compress bitmap to fit in Gemini payload limit
    private fun compressAndConvertBitmap(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize first if too large
        val maxDimension = 1024
        val width = bitmap.width
        val height = bitmap.height
        val resizedBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val (newWidth, newHeight) = if (ratio > 1) {
                Pair(maxDimension, (maxDimension / ratio).toInt())
            } else {
                Pair((maxDimension * ratio).toInt(), maxDimension)
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
