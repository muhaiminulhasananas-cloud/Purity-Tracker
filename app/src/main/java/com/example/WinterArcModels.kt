package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "winter_arc_challenges")
data class WinterArcChallenge(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "fitness", "water", "discipline", "mindset", "spiritual", "custom"
    val targetTime: String = "", // e.g. "05:00 AM" (customizable by user)
    val isCompleted: Boolean = false,
    val completedDate: String = "", // "yyyy-MM-dd"
    val customValue: String = "", // e.g. "100 pushups, 4 sets", "3.5L"
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "winter_arc_workouts")
data class WinterArcWorkoutItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sets: Int = 3,
    val repsOrDuration: String = "20 reps",
    val targetTime: String = "07:00 AM",
    val isCompleted: Boolean = false,
    val dateStr: String = "",
    val notificationScheduled: Boolean = true,
    val notificationId: Int = 0
)

@Entity(tableName = "winter_arc_day_tasks")
data class WinterArcDayTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val scheduledTime: String, // e.g. "05:30 AM"
    val targetDate: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = false,
    val notificationScheduled: Boolean = false,
    val notificationId: Int = 0
)

@Entity(tableName = "winter_arc_run_records")
data class WinterArcRunRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val dateStr: String,
    val durationSeconds: Long,
    val distanceMeters: Float,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val caloriesBurned: Int,
    val pathJson: String // serialized JSON list of lat/lng pairs
)

@Entity(tableName = "winter_arc_day_records")
data class WinterArcDayRecord(
    @PrimaryKey val dateStr: String, // "yyyy-MM-dd"
    val completedChallengesCount: Int = 0,
    val totalChallengesCount: Int = 12,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val waterIntakeMl: Int = 0,
    val runDistanceKm: Float = 0f,
    val workoutDone: Boolean = false
)

@Dao
interface WinterArcDao {
    @Query("SELECT * FROM winter_arc_challenges ORDER BY isCompleted ASC, orderIndex ASC")
    fun getAllChallenges(): Flow<List<WinterArcChallenge>>

    @Query("SELECT * FROM winter_arc_challenges ORDER BY isCompleted ASC, orderIndex ASC")
    suspend fun getChallengesList(): List<WinterArcChallenge>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(list: List<WinterArcChallenge>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: WinterArcChallenge)

    @Update
    suspend fun updateChallenge(challenge: WinterArcChallenge)

    @Query("DELETE FROM winter_arc_challenges WHERE id = :id")
    suspend fun deleteChallenge(id: String)

    @Query("SELECT * FROM winter_arc_workouts ORDER BY isCompleted ASC, id ASC")
    fun getAllWorkouts(): Flow<List<WinterArcWorkoutItem>>

    @Query("SELECT * FROM winter_arc_workouts ORDER BY isCompleted ASC, id ASC")
    suspend fun getWorkoutsList(): List<WinterArcWorkoutItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(item: WinterArcWorkoutItem): Long

    @Update
    suspend fun updateWorkout(item: WinterArcWorkoutItem)

    @Query("DELETE FROM winter_arc_workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Int)

    @Query("SELECT * FROM winter_arc_day_tasks ORDER BY isCompleted ASC, scheduledTime ASC")
    fun getAllDayTasks(): Flow<List<WinterArcDayTask>>

    @Query("SELECT * FROM winter_arc_day_tasks ORDER BY isCompleted ASC, scheduledTime ASC")
    suspend fun getDayTasksList(): List<WinterArcDayTask>

    @Query("SELECT * FROM winter_arc_day_tasks WHERE targetDate = :dateStr ORDER BY isCompleted ASC, scheduledTime ASC")
    fun getDayTasksForDate(dateStr: String): Flow<List<WinterArcDayTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTask(task: WinterArcDayTask): Long

    @Update
    suspend fun updateDayTask(task: WinterArcDayTask)

    @Query("DELETE FROM winter_arc_day_tasks WHERE id = :id")
    suspend fun deleteDayTask(id: Int)

    @Query("SELECT * FROM winter_arc_run_records ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<WinterArcRunRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(record: WinterArcRunRecord)

    @Query("DELETE FROM winter_arc_run_records WHERE id = :id")
    suspend fun deleteRun(id: Int)

    @Query("SELECT * FROM winter_arc_day_records ORDER BY dateStr ASC")
    fun getAllDayRecords(): Flow<List<WinterArcDayRecord>>

    @Query("SELECT * FROM winter_arc_day_records ORDER BY dateStr ASC")
    suspend fun getDayRecordsList(): List<WinterArcDayRecord>

    @Query("SELECT * FROM winter_arc_day_records WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDayRecord(dateStr: String): WinterArcDayRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayRecord(record: WinterArcDayRecord)
}

data class WaterLogEntry(
    val id: Long = System.currentTimeMillis(),
    val amountMl: Int,
    val timeFormatted: String
)
