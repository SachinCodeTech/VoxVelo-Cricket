package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [PlayerEntity::class, SessionEntity::class, DeliveryEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "voxvelo_cricket.db"
        )
        .addCallback(object : Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
              populateInitialData(getDatabase(context).appDao())
            }
          }
        })
        .build()
        INSTANCE = instance
        instance
      }
    }

    suspend fun populateInitialData(dao: AppDao) {
      val now = System.currentTimeMillis()
      val p1 = PlayerEntity(
        id = "p_mitchell",
        name = "Mitchell S.",
        hand = "Right-handed",
        style = "Fast",
        team = "Hawks Academy",
        createdAt = now - 86400000L * 7
      )
      val p2 = PlayerEntity(
        id = "p_james",
        name = "James A.",
        hand = "Right-handed",
        style = "Medium",
        team = "Lions Cricket Club",
        createdAt = now - 86400000L * 5
      )
      dao.insertPlayer(p1)
      dao.insertPlayer(p2)

      val s1 = SessionEntity(
        id = "s_morning_nets",
        name = "Morning Nets",
        playerId = "p_mitchell",
        createdAt = now - 3600000L * 3
      )
      val s2 = SessionEntity(
        id = "s_match_prep",
        name = "Match Prep",
        playerId = "p_james",
        createdAt = now - 3600000L * 28
      )
      dao.insertSession(s1)
      dao.insertSession(s2)

      // Mitchell's 6 deliveries in Morning Nets
      val d1 = DeliveryEntity(
        id = "d_1",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 128.4,
        mph = 79.8,
        flightTime = 0.564,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = true,
        releaseToBounceTime = 0.410,
        bounceToArrivalTime = 0.154,
        bounceDistance = 15.2,
        stumpsDistance = 4.92,
        timestamp = s1.createdAt + 120000L
      )
      val d2 = DeliveryEntity(
        id = "d_2",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 131.2,
        mph = 81.5,
        flightTime = 0.552,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = true,
        releaseToBounceTime = 0.400,
        bounceToArrivalTime = 0.152,
        bounceDistance = 15.4,
        stumpsDistance = 4.72,
        timestamp = s1.createdAt + 240000L
      )
      val d3 = DeliveryEntity(
        id = "d_3",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 129.8,
        mph = 80.7,
        flightTime = 0.558,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = false,
        timestamp = s1.createdAt + 360000L
      )
      val d4 = DeliveryEntity(
        id = "d_4",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 133.0,
        mph = 82.6,
        flightTime = 0.544,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = true,
        releaseToBounceTime = 0.395,
        bounceToArrivalTime = 0.149,
        bounceDistance = 15.6,
        stumpsDistance = 4.52,
        timestamp = s1.createdAt + 480000L
      )
      val d5 = DeliveryEntity(
        id = "d_5",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 127.5,
        mph = 79.2,
        flightTime = 0.568,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = false,
        timestamp = s1.createdAt + 600000L
      )
      val d6 = DeliveryEntity(
        id = "d_6",
        sessionId = s1.id,
        playerId = p1.id,
        kmh = 130.6,
        mph = 81.2,
        flightTime = 0.555,
        distance = 20.12,
        fps = 60.0,
        confidence = "high",
        angle = "side",
        hasBounce = true,
        releaseToBounceTime = 0.402,
        bounceToArrivalTime = 0.153,
        bounceDistance = 15.3,
        stumpsDistance = 4.82,
        timestamp = s1.createdAt + 720000L
      )
      listOf(d1, d2, d3, d4, d5, d6).forEach { dao.insertDelivery(it) }

      // James's 4 deliveries
      val jd1 = DeliveryEntity(
        id = "jd_1",
        sessionId = s2.id,
        playerId = p2.id,
        kmh = 114.2,
        mph = 71.0,
        flightTime = 0.634,
        distance = 20.12,
        fps = 30.0,
        confidence = "medium",
        angle = "side",
        timestamp = s2.createdAt + 100000L
      )
      val jd2 = DeliveryEntity(
        id = "jd_2",
        sessionId = s2.id,
        playerId = p2.id,
        kmh = 116.8,
        mph = 72.6,
        flightTime = 0.620,
        distance = 20.12,
        fps = 30.0,
        confidence = "medium",
        angle = "side",
        timestamp = s2.createdAt + 200000L
      )
      val jd3 = DeliveryEntity(
        id = "jd_3",
        sessionId = s2.id,
        playerId = p2.id,
        kmh = 115.0,
        mph = 71.5,
        flightTime = 0.630,
        distance = 20.12,
        fps = 30.0,
        confidence = "medium",
        angle = "side",
        timestamp = s2.createdAt + 300000L
      )
      val jd4 = DeliveryEntity(
        id = "jd_4",
        sessionId = s2.id,
        playerId = p2.id,
        kmh = 118.4,
        mph = 73.6,
        flightTime = 0.612,
        distance = 20.12,
        fps = 30.0,
        confidence = "medium",
        angle = "side",
        timestamp = s2.createdAt + 400000L
      )
      listOf(jd1, jd2, jd3, jd4).forEach { dao.insertDelivery(it) }
    }
  }
}
