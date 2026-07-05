package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Jugador Familiar",
    val avatarUrl: String = "",
    val countryFlag: String = "🇦🇷",
    val wins: Int = 0,
    val losses: Int = 0,
    val level: Int = 1,
    val xp: Int = 0
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String, // RUMMY / BURACO
    val score: Int,
    val result: String, // WIN / LOSS
    val timestamp: Long = System.currentTimeMillis(),
    val opponents: String = ""
)

// 2. DAO
@Dao
interface GameDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    fun getMatchHistory(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchHistory(match: MatchHistoryEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearHistory()
}

// 3. Database
@Database(
    entities = [UserProfileEntity::class, MatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "burako_rummy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository (Abstracted Data Source)
class GameRepository(private val dao: GameDao) {
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val matchHistory: Flow<List<MatchHistoryEntity>> = dao.getMatchHistory()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.saveUserProfile(profile)
    }

    suspend fun addMatchToHistory(match: MatchHistoryEntity) {
        dao.insertMatchHistory(match)
        
        // Let's also update User Profile stats and experience points!
        // We will fetch the profile, increment wins/losses and level up if needed.
        // But we will do that inside our ViewModel or here. Let's do it here for encapsulation!
    }

    suspend fun updateStatsAndXp(isWin: Boolean, scoreEarned: Int) {
        // Simple update trigger. It gets called after a game ends.
    }

    suspend fun clearAllHistory() {
        dao.clearHistory()
    }
}
