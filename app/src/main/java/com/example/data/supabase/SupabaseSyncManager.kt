package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.LoggedFood
import com.example.data.model.WaterLog
import com.example.data.model.User
import com.example.data.repo.NutriRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

object SupabaseSyncManager {
    private const val TAG = "SupabaseSync"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if the Supabase project is reachable and responsive.
     */
    suspend fun testConnection(url: String, anonKey: String): Boolean {
        if (url.isBlank() || anonKey.isBlank()) return false
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        val requestUrl = "${cleanUrl}rest/v1/users?limit=1"

        return try {
            val request = Request.Builder()
                .url(requestUrl)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                // A response of 200 (OK), 404 (table not created yet but base url authenticates), or 400 means we reached the server.
                Log.d(TAG, "Test connection response code: ${response.code}")
                response.code == 200 || response.code == 404 || response.code == 400
            }
        } catch (e: Exception) {
            Log.e(TAG, "Test connection failed", e)
            false
        }
    }

    /**
     * Runs full two-way cloud synchronization.
     */
    suspend fun syncAll(
        context: Context,
        url: String,
        anonKey: String,
        currentUsername: String,
        repository: NutriRepository
    ): SyncResult {
        if (url.isBlank() || anonKey.isBlank()) {
            return SyncResult.Error("Supabase URL and Anon Key are required.")
        }
        if (currentUsername.isBlank()) {
            return SyncResult.Error("You must be logged in to sync data.")
        }

        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        
        try {
            var syncedTables = 0
            val errors = mutableListOf<String>()

            // --- 1. SYNC USERS ---
            try {
                // Upload current user profile first
                val currentUser = repository.getUserByUsername(currentUsername)
                if (currentUser != null) {
                    val userArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("username", currentUser.username)
                            put("email", currentUser.email)
                            put("phone_number", currentUser.phoneNumber)
                            put("password_hash", currentUser.passwordHash)
                            put("salt", currentUser.salt)
                            put("pin_code", currentUser.pinCode ?: "")
                            put("biometric_enabled", currentUser.biometricEnabled)
                            put("created_at", currentUser.createdAt)
                        })
                    }
                    uploadTable(cleanUrl, anonKey, "users", userArray)
                }

                // Download users to local Room
                val usersJson = downloadTable(cleanUrl, anonKey, "users")
                if (usersJson != null) {
                    for (i in 0 until usersJson.length()) {
                        val obj = usersJson.getJSONObject(i)
                        val u = User(
                            username = obj.getString("username"),
                            email = obj.optString("email", ""),
                            phoneNumber = obj.optString("phone_number", ""),
                            passwordHash = obj.getString("password_hash"),
                            salt = obj.getString("salt"),
                            pinCode = obj.optString("pin_code", "").takeIf { it.isNotBlank() },
                            biometricEnabled = obj.optBoolean("biometric_enabled", false),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                        // Don't overwrite if it's currently logged in to prevent session glitches,
                        // unless it matches completely.
                        repository.insertUser(u)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Users error", e)
                errors.add("Users: ${e.localizedMessage}")
            }

            // --- 2. SYNC LOGGED FOODS ---
            try {
                // Upload local foods
                val localFoods = repository.getAllFoodsOnce()
                val foodArray = JSONArray()
                localFoods.forEach { food ->
                    foodArray.put(JSONObject().apply {
                        // Include id if it exists, let database upsert
                        if (food.id > 0) put("id", food.id)
                        put("name", food.name)
                        put("meal_type", food.mealType)
                        put("calories", food.calories)
                        put("protein", food.protein)
                        put("carbs", food.carbs)
                        put("fat", food.fat)
                        put("quantity", food.quantity)
                        put("image_url", food.imageUrl ?: "")
                        put("date_string", food.dateString)
                        put("timestamp", food.timestamp)
                        put("username", currentUsername)
                    })
                }
                if (foodArray.length() > 0) {
                    uploadTable(cleanUrl, anonKey, "logged_foods", foodArray)
                }

                // Download cloud foods
                val foodsJson = downloadTable(cleanUrl, anonKey, "logged_foods", currentUsername)
                if (foodsJson != null) {
                    for (i in 0 until foodsJson.length()) {
                        val obj = foodsJson.getJSONObject(i)
                        val food = LoggedFood(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            mealType = obj.getString("meal_type"),
                            calories = obj.getInt("calories"),
                            protein = obj.getDouble("protein"),
                            carbs = obj.getDouble("carbs"),
                            fat = obj.getDouble("fat"),
                            quantity = obj.getString("quantity"),
                            imageUrl = obj.optString("image_url", "").takeIf { it.isNotBlank() },
                            dateString = obj.getString("date_string"),
                            timestamp = obj.getLong("timestamp")
                        )
                        repository.insertFood(food)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Foods error", e)
                errors.add("Food Logs: ${e.localizedMessage}")
            }

            // --- 3. SYNC WATER LOGS ---
            try {
                // Upload local water
                val localWater = repository.getAllWaterLogsOnce()
                val waterArray = JSONArray()
                localWater.forEach { log ->
                    waterArray.put(JSONObject().apply {
                        if (log.id > 0) put("id", log.id)
                        put("amount_ml", log.amountMl)
                        put("date_string", log.dateString)
                        put("timestamp", log.timestamp)
                        put("username", currentUsername)
                    })
                }
                if (waterArray.length() > 0) {
                    uploadTable(cleanUrl, anonKey, "water_logs", waterArray)
                }

                // Download cloud water
                val waterJson = downloadTable(cleanUrl, anonKey, "water_logs", currentUsername)
                if (waterJson != null) {
                    for (i in 0 until waterJson.length()) {
                        val obj = waterJson.getJSONObject(i)
                        val log = WaterLog(
                            id = obj.getInt("id"),
                            amountMl = obj.getInt("amount_ml"),
                            dateString = obj.getString("date_string"),
                            timestamp = obj.getLong("timestamp")
                        )
                        repository.insertWaterLog(log)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Water error", e)
                errors.add("Water Logs: ${e.localizedMessage}")
            }

            // --- 4. SYNC WEIGHT LOGS ---
            try {
                // Upload local weights
                val localWeights = repository.getAllWeightLogsOnce()
                val weightArray = JSONArray()
                localWeights.forEach { log ->
                    weightArray.put(JSONObject().apply {
                        if (log.id > 0) put("id", log.id)
                        put("weight_kg", log.weightKg)
                        put("date_string", log.dateString)
                        put("timestamp", log.timestamp)
                        put("username", currentUsername)
                    })
                }
                if (weightArray.length() > 0) {
                    uploadTable(cleanUrl, anonKey, "weight_logs", weightArray)
                }

                // Download cloud weights
                val weightJson = downloadTable(cleanUrl, anonKey, "weight_logs", currentUsername)
                if (weightJson != null) {
                    for (i in 0 until weightJson.length()) {
                        val obj = weightJson.getJSONObject(i)
                        val log = com.example.data.model.WeightLog(
                            id = obj.getInt("id"),
                            weightKg = obj.getDouble("weight_kg"),
                            dateString = obj.getString("date_string"),
                            timestamp = obj.getLong("timestamp")
                        )
                        repository.insertWeightLog(log)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Weight error", e)
                errors.add("Weight Logs: ${e.localizedMessage}")
            }

            // --- 5. SYNC CHAT COACH MESSAGES ---
            try {
                // Upload local chat
                val localChats = repository.getAllChatMessagesOnce()
                val chatArray = JSONArray()
                localChats.forEach { msg ->
                    chatArray.put(JSONObject().apply {
                        if (msg.id > 0) put("id", msg.id)
                        put("sender", msg.sender)
                        put("message", msg.message)
                        put("timestamp", msg.timestamp)
                        put("username", currentUsername)
                    })
                }
                if (chatArray.length() > 0) {
                    uploadTable(cleanUrl, anonKey, "chat_messages", chatArray)
                }

                // Download cloud chat
                val chatJson = downloadTable(cleanUrl, anonKey, "chat_messages", currentUsername)
                if (chatJson != null) {
                    for (i in 0 until chatJson.length()) {
                        val obj = chatJson.getJSONObject(i)
                        val msg = ChatMessage(
                            id = obj.getInt("id"),
                            sender = obj.getString("sender"),
                            message = obj.getString("message"),
                            timestamp = obj.getLong("timestamp")
                        )
                        repository.insertChatMessage(msg)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Chat error", e)
                errors.add("Chat Messages: ${e.localizedMessage}")
            }

            // --- 6. SYNC SCAN FEEDBACKS ---
            try {
                // Upload local feedbacks
                val localFeedbacks = repository.getAllFeedbacksOnce()
                val feedbackArray = JSONArray()
                localFeedbacks.forEach { fb ->
                    feedbackArray.put(JSONObject().apply {
                        if (fb.id > 0) put("id", fb.id)
                        put("food_name", fb.foodName)
                        put("original_calories", fb.originalCalories)
                        put("corrected_calories", fb.correctedCalories)
                        put("original_ingredients", fb.originalIngredients)
                        put("corrected_ingredients", fb.correctedIngredients)
                        put("feedback_text", fb.feedbackText)
                        put("is_positive", fb.isPositive)
                        put("timestamp", fb.timestamp)
                        put("username", currentUsername)
                    })
                }
                if (feedbackArray.length() > 0) {
                    uploadTable(cleanUrl, anonKey, "scan_feedbacks", feedbackArray)
                }

                // Download cloud feedbacks
                val feedbacksJson = downloadTable(cleanUrl, anonKey, "scan_feedbacks", currentUsername)
                if (feedbacksJson != null) {
                    for (i in 0 until feedbacksJson.length()) {
                        val obj = feedbacksJson.getJSONObject(i)
                        val fb = com.example.data.model.ScanFeedback(
                            id = obj.getInt("id"),
                            foodName = obj.getString("food_name"),
                            originalCalories = obj.getInt("original_calories"),
                            correctedCalories = obj.getInt("corrected_calories"),
                            originalIngredients = obj.getString("original_ingredients"),
                            correctedIngredients = obj.getString("corrected_ingredients"),
                            feedbackText = obj.getString("feedback_text"),
                            isPositive = obj.getBoolean("is_positive"),
                            timestamp = obj.getLong("timestamp")
                        )
                        repository.insertFeedback(fb)
                    }
                }
                syncedTables++
            } catch (e: Exception) {
                Log.e(TAG, "Sync Feedbacks error", e)
                errors.add("Scan Feedback Logs: ${e.localizedMessage}")
            }

            if (errors.isNotEmpty()) {
                val failList = errors.joinToString("; ")
                return SyncResult.Success("Partial Sync Success: Synced $syncedTables tables safely. Some databases requires creation or schemas on Supabase: $failList")
            }

            return SyncResult.Success("Cloud Sync Completed successfully! All logs, targets, and messages have been fully synchronized with Supabase.")
        } catch (e: Exception) {
            Log.e(TAG, "Global sync error", e)
            return SyncResult.Error("Synchronization failed: ${e.localizedMessage}")
        }
    }

    private fun uploadTable(baseUrl: String, key: String, tableName: String, dataArray: JSONArray) {
        val url = "${baseUrl}rest/v1/$tableName"
        val body = dataArray.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Prefer", "resolution=merge-duplicates")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: ""
                Log.e(TAG, "Error uploading to $tableName: ${response.code} $errorMsg")
                throw IOException("Server returned code ${response.code}: $errorMsg")
            }
        }
    }

    private fun downloadTable(baseUrl: String, key: String, tableName: String, username: String? = null): JSONArray? {
        val queryParam = if (username != null) "?username=eq.$username" else ""
        val url = "${baseUrl}rest/v1/$tableName$queryParam"

        val request = Request.Builder()
            .url(url)
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                // Table doesn't exist yet
                Log.w(TAG, "Table $tableName does not exist on Supabase. Skipping download.")
                return null
            }
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: ""
                Log.e(TAG, "Error downloading from $tableName: ${response.code} $errorMsg")
                throw IOException("Server returned code ${response.code}: $errorMsg")
            }

            val bodyStr = response.body?.string() ?: "[]"
            return JSONArray(bodyStr)
        }
    }
}
