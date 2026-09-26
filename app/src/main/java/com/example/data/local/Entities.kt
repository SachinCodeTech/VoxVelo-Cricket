package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hand: String,
    val style: String,
    val team: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId")]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val playerId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val kmh: Double,
    val mph: Double,
    val flightTime: Double,
    val distance: Double,
    val fps: Double,
    val confidence: String,
    val cameraAngle: String,
    val hasBounce: Boolean,
    val releaseToBounceTime: Double?,
    val bounceToArrivalTime: Double?,
    val bounceDistance: Double?,
    val stumpsDistance: Double?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
