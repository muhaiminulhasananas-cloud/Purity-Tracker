package com.example

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "session_history")
data class SessionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val durationDays: Int,
    val statusText: String
)

@Dao
interface SessionHistoryDao {
    @Query("SELECT * FROM session_history ORDER BY endTime DESC")
    fun getAllHistory(): Flow<List<SessionHistory>>

    @Query("SELECT * FROM session_history ORDER BY durationDays DESC LIMIT 1")
    fun getBestRecord(): Flow<SessionHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SessionHistory)

    @Query("DELETE FROM session_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM session_history")
    suspend fun deleteAllHistory()
}

@Database(
    entities = [
        SessionHistory::class,
        WinterArcChallenge::class,
        WinterArcWorkoutItem::class,
        WinterArcDayTask::class,
        WinterArcRunRecord::class,
        WinterArcDayRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionHistoryDao(): SessionHistoryDao
    abstract fun winterArcDao(): WinterArcDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
