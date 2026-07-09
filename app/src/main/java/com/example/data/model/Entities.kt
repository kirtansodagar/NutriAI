package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_foods")
data class LoggedFood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val quantity: String, // e.g. "1 serving", "150g"
    val imageUrl: String? = null,
    val dateString: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val email: String,
    val phoneNumber: String,
    val passwordHash: String,
    val salt: String,
    val pinCode: String? = null,
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_goals")
data class UserGoal(
    @PrimaryKey val id: Int = 1,
    val weightKg: Double = 70.0,
    val heightCm: Double = 175.0,
    val age: Int = 25,
    val gender: String = "Male", // "Male", "Female"
    val activityLevel: String = "Moderate", // "Sedentary", "Moderate", "Active"
    val goalType: String = "Weight Loss", // "Weight Loss", "Maintain", "Muscle Gain"
    val targetWeightKg: Double = 65.0,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 130, // in g
    val targetCarbs: Int = 220, // in g
    val targetFat: Int = 65, // in g
    val targetWaterMl: Int = 2500,
    val customApiKey: String = ""
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMl: Int,
    val dateString: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weightKg: Double,
    val dateString: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_feedbacks")
data class ScanFeedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val originalCalories: Int,
    val correctedCalories: Int,
    val originalIngredients: String,
    val correctedIngredients: String,
    val feedbackText: String,
    val isPositive: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
