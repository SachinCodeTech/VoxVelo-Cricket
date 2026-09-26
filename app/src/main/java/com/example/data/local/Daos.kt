package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY createdAt DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deletePlayerById(id: String)

    @Query("DELETE FROM players")
    suspend fun clearAll()
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE playerId = :playerId ORDER BY createdAt DESC")
    fun getSessionsForPlayer(playerId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY timestamp DESC")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getDeliveriesForSession(sessionId: String): Flow<List<DeliveryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryEntity)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteDeliveryById(id: String)

    @Query("DELETE FROM deliveries")
    suspend fun clearAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)

    @Query("DELETE FROM app_settings")
    suspend fun clearAll()
}
