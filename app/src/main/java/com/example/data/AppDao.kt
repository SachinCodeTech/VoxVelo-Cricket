package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
  @Query("SELECT * FROM players ORDER BY createdAt ASC")
  fun getAllPlayers(): Flow<List<PlayerEntity>>

  @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
  suspend fun getPlayer(id: String): PlayerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlayer(player: PlayerEntity)

  @Query("DELETE FROM players WHERE id = :id")
  suspend fun deletePlayer(id: String)

  @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
  fun getAllSessions(): Flow<List<SessionEntity>>

  @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
  suspend fun getSession(id: String): SessionEntity?

  @Query("SELECT * FROM sessions WHERE playerId = :playerId ORDER BY createdAt DESC")
  fun getSessionsForPlayer(playerId: String): Flow<List<SessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: SessionEntity)

  @Query("DELETE FROM sessions WHERE id = :id")
  suspend fun deleteSession(id: String)

  @Query("SELECT * FROM deliveries ORDER BY timestamp ASC")
  fun getAllDeliveries(): Flow<List<DeliveryEntity>>

  @Query("SELECT * FROM deliveries WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getDeliveriesForSession(sessionId: String): Flow<List<DeliveryEntity>>

  @Query("SELECT * FROM deliveries WHERE playerId = :playerId ORDER BY timestamp ASC")
  fun getDeliveriesForPlayer(playerId: String): Flow<List<DeliveryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDelivery(delivery: DeliveryEntity)

  @Query("DELETE FROM players")
  suspend fun clearPlayers()

  @Query("DELETE FROM sessions")
  suspend fun clearSessions()

  @Query("DELETE FROM deliveries")
  suspend fun clearDeliveries()
}
