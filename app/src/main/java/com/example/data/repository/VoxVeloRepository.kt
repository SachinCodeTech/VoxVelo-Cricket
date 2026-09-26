package com.example.data.repository

import com.example.data.local.AppSettingsEntity
import com.example.data.local.AppDatabase
import com.example.data.local.DeliveryEntity
import com.example.data.local.PlayerEntity
import com.example.data.local.SessionEntity
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class VoxVeloRepository(private val db: AppDatabase) {

    private val playerDao = db.playerDao()
    private val sessionDao = db.sessionDao()
    private val deliveryDao = db.deliveryDao()
    private val settingsDao = db.settingsDao()

    val allPlayers: Flow<List<Player>> = playerDao.getAllPlayers().map { list ->
        list.map { it.toModel() }
    }

    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions().map { list ->
        list.map { it.toModel() }
    }

    val allDeliveries: Flow<List<Delivery>> = deliveryDao.getAllDeliveries().map { list ->
        list.map { it.toModel() }
    }

    val activePlayerId: Flow<String?> = settingsDao.getSettingFlow("active_player_id")

    suspend fun getActivePlayerId(): String? = settingsDao.getSetting("active_player_id")

    suspend fun setActivePlayerId(id: String) {
        settingsDao.setSetting(AppSettingsEntity(key = "active_player_id", value = id))
    }

    suspend fun addPlayer(player: Player) {
        playerDao.insertPlayer(player.toEntity())
        val currentActive = settingsDao.getSetting("active_player_id")
        if (currentActive.isNullOrEmpty()) {
            setActivePlayerId(player.id)
        }
    }

    suspend fun deletePlayer(id: String) {
        playerDao.deletePlayerById(id)
        val currentActive = settingsDao.getSetting("active_player_id")
        if (currentActive == id) {
            settingsDao.setSetting(AppSettingsEntity("active_player_id", ""))
        }
    }

    suspend fun createSession(name: String, playerId: String): Session {
        val session = Session(
            id = "s_" + System.currentTimeMillis(),
            name = name,
            playerId = playerId,
            createdAt = System.currentTimeMillis()
        )
        sessionDao.insertSession(session.toEntity())
        return session
    }

    suspend fun deleteSession(id: String) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun addDelivery(delivery: Delivery) {
        deliveryDao.insertDelivery(delivery.toEntity())
    }

    suspend fun deleteDelivery(id: String) {
        deliveryDao.deleteDeliveryById(id)
    }

    suspend fun clearAllData() {
        deliveryDao.clearAll()
        sessionDao.clearAll()
        playerDao.clearAll()
        settingsDao.clearAll()
    }

    suspend fun exportJson(players: List<Player>, sessions: List<Session>, deliveries: List<Delivery>, activeId: String?): String {
        val root = JSONObject()
        val playersArray = JSONArray()
        for (p in players) {
            val pj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("hand", p.hand)
                put("style", p.style)
                put("team", p.team)
                put("createdAt", p.createdAt)
            }
            playersArray.put(pj)
        }
        root.put("players", playersArray)

        val sessionsArray = JSONArray()
        for (s in sessions) {
            val sj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("playerId", s.playerId)
                put("createdAt", s.createdAt)
                val sDels = JSONArray()
                deliveries.filter { it.sessionId == s.id }.forEach { d ->
                    sDels.put(JSONObject().apply {
                        put("id", d.id)
                        put("kmh", d.kmh)
                        put("mph", d.mph)
                        put("flightTime", d.flightTime)
                        put("distance", d.distance)
                        put("fps", d.fps)
                        put("confidence", d.confidence)
                        put("cameraAngle", d.cameraAngle)
                        put("hasBounce", d.hasBounce)
                        put("releaseToBounceTime", d.releaseToBounceTime ?: JSONObject.NULL)
                        put("bounceToArrivalTime", d.bounceToArrivalTime ?: JSONObject.NULL)
                        put("bounceDistance", d.bounceDistance ?: JSONObject.NULL)
                        put("stumpsDistance", d.stumpsDistance ?: JSONObject.NULL)
                        put("timestamp", d.timestamp)
                    })
                }
                put("deliveries", sDels)
            }
            sessionsArray.put(sj)
        }
        root.put("sessions", sessionsArray)
        root.put("activePlayerId", activeId ?: JSONObject.NULL)
        return root.toString(2)
    }

    private fun PlayerEntity.toModel(): Player = Player(
        id = id,
        name = name,
        hand = hand,
        style = style,
        team = team,
        createdAt = createdAt
    )

    private fun Player.toEntity(): PlayerEntity = PlayerEntity(
        id = id,
        name = name,
        hand = hand,
        style = style,
        team = team,
        createdAt = createdAt
    )

    private fun SessionEntity.toModel(): Session = Session(
        id = id,
        name = name,
        playerId = playerId,
        createdAt = createdAt
    )

    private fun Session.toEntity(): SessionEntity = SessionEntity(
        id = id,
        name = name,
        playerId = playerId,
        createdAt = createdAt
    )

    private fun DeliveryEntity.toModel(): Delivery = Delivery(
        id = id,
        sessionId = sessionId,
        kmh = kmh,
        mph = mph,
        flightTime = flightTime,
        distance = distance,
        fps = fps,
        confidence = confidence,
        cameraAngle = cameraAngle,
        hasBounce = hasBounce,
        releaseToBounceTime = releaseToBounceTime,
        bounceToArrivalTime = bounceToArrivalTime,
        bounceDistance = bounceDistance,
        stumpsDistance = stumpsDistance,
        timestamp = timestamp
    )

    private fun Delivery.toEntity(): DeliveryEntity = DeliveryEntity(
        id = id,
        sessionId = sessionId,
        kmh = kmh,
        mph = mph,
        flightTime = flightTime,
        distance = distance,
        fps = fps,
        confidence = confidence,
        cameraAngle = cameraAngle,
        hasBounce = hasBounce,
        releaseToBounceTime = releaseToBounceTime,
        bounceToArrivalTime = bounceToArrivalTime,
        bounceDistance = bounceDistance,
        stumpsDistance = stumpsDistance,
        timestamp = timestamp
    )
}
