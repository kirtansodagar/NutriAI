package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.LoggedFood
import com.example.data.model.UserGoal
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriDao {
    // Logged Foods
    @Query("SELECT * FROM logged_foods WHERE dateString = :date ORDER BY timestamp DESC")
    fun getFoodsForDate(date: String): Flow<List<LoggedFood>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: LoggedFood)

    @Delete
    suspend fun deleteFood(food: LoggedFood)

    @Query("DELETE FROM logged_foods WHERE id = :foodId")
    suspend fun deleteFoodById(foodId: Int)

    // User Goals
    @Query("SELECT * FROM user_goals WHERE id = 1 LIMIT 1")
    fun getUserGoal(): Flow<UserGoal?>

    @Query("SELECT * FROM user_goals WHERE id = 1 LIMIT 1")
    suspend fun getUserGoalOnce(): UserGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserGoal(goal: UserGoal)

    // Water Logs
    @Query("SELECT * FROM water_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE dateString = :date ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastWaterLog(date: String)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    // Scan Feedbacks for Continuous Learning
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: com.example.data.model.ScanFeedback)

    @Query("SELECT * FROM scan_feedbacks ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentFeedbacksOnce(): List<com.example.data.model.ScanFeedback>

    @Query("SELECT * FROM scan_feedbacks ORDER BY timestamp DESC LIMIT 15")
    fun getRecentFeedbacks(): Flow<List<com.example.data.model.ScanFeedback>>

    // Weight Logs
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<com.example.data.model.WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: com.example.data.model.WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Int)

    // Users
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): com.example.data.model.User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): com.example.data.model.User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): com.example.data.model.User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: com.example.data.model.User)

    // Bulk retrievals for Cloud Synchronization
    @Query("SELECT * FROM logged_foods")
    suspend fun getAllFoodsOnce(): List<LoggedFood>

    @Query("SELECT * FROM water_logs")
    suspend fun getAllWaterLogsOnce(): List<WaterLog>

    @Query("SELECT * FROM weight_logs")
    suspend fun getAllWeightLogsOnce(): List<com.example.data.model.WeightLog>

    @Query("SELECT * FROM chat_messages")
    suspend fun getAllChatMessagesOnce(): List<ChatMessage>

    @Query("SELECT * FROM scan_feedbacks")
    suspend fun getAllFeedbacksOnce(): List<com.example.data.model.ScanFeedback>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersOnce(): List<com.example.data.model.User>
}

@Database(
    entities = [
        LoggedFood::class, 
        UserGoal::class, 
        WaterLog::class, 
        ChatMessage::class, 
        com.example.data.model.ScanFeedback::class, 
        com.example.data.model.WeightLog::class,
        com.example.data.model.User::class
    ],
    version = 6,
    exportSchema = false
)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao
}
