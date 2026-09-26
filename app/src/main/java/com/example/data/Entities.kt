package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val hand: String, // Right-handed, Left-handed
  val style: String, // Fast, Medium, Spin
  val team: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
  @PrimaryKey val id: String,
  val name: String,
  val playerId: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deliveries")
data class DeliveryEntity(
  @PrimaryKey val id: String,
  val sessionId: String,
  val playerId: String,
  val kmh: Double,
  val mph: Double,
  val flightTime: Double,
  val distance: Double,
  val fps: Double,
  val confidence: String, // "high", "medium", "low"
  val angle: String, // "side", "behind", "other"
  val hasBounce: Boolean = false,
  val releaseToBounceTime: Double? = null,
  val bounceToArrivalTime: Double? = null,
  val bounceDistance: Double? = null,
  val stumpsDistance: Double? = null,
  val timestamp: Long = System.currentTimeMillis()
)
