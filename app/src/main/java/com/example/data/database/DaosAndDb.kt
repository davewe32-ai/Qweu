package com.example.data.database

import androidx.room.*
import com.example.data.model.AuditLogEntity
import com.example.data.model.GemstoneEntity
import com.example.data.model.SystemSettingsEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)
}

@Dao
interface GemstoneDao {
    @Query("SELECT * FROM gemstones ORDER BY id DESC")
    fun getAllGemstones(): Flow<List<GemstoneEntity>>

    @Query("SELECT * FROM gemstones WHERE id = :id LIMIT 1")
    suspend fun getGemstoneById(id: Int): GemstoneEntity?

    @Query("SELECT * FROM gemstones WHERE gemId = :gemId LIMIT 1")
    suspend fun getGemstoneByGemId(gemId: String): GemstoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGemstone(gemstone: GemstoneEntity): Long

    @Update
    suspend fun updateGemstone(gemstone: GemstoneEntity)

    @Delete
    suspend fun deleteGemstone(gemstone: GemstoneEntity)

    @Query("DELETE FROM gemstones WHERE id = :id")
    suspend fun deleteGemstoneById(id: Int)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()
}

@Dao
interface SystemSettingsDao {
    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SystemSettingsEntity)
}

@Database(
    entities = [
        UserEntity::class,
        GemstoneEntity::class,
        AuditLogEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gemstoneDao(): GemstoneDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun systemSettingsDao(): SystemSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gem_vault_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
