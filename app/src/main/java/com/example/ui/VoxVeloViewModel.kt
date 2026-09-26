package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CoachModel
import com.example.data.DeliveryEntity
import com.example.data.PlayerEntity
import com.example.data.SessionEntity
import com.example.data.VideoMotionAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.abs

sealed class Screen {
  object Home : Screen()
  object Sessions : Screen()
  data class SessionDetail(val sessionId: String) : Screen()
  object Analyze : Screen()
  object Players : Screen()
  data class PlayerDetail(val playerId: String) : Screen()
  object TeamDashboard : Screen()
  object Compare : Screen()
  object More : Screen()
}

enum class AnalyzeTab { IMPORT, RESULT }
enum class CompareMode { PLAYERS, SESSIONS }

class VoxVeloViewModel(application: Application) : AndroidViewModel(application) {
  private val dao = AppDatabase.getDatabase(application).appDao()
  private val prefs = application.getSharedPreferences("voxvelo_prefs", Context.MODE_PRIVATE)

  val players: StateFlow<List<PlayerEntity>> = dao.getAllPlayers()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val sessions: StateFlow<List<SessionEntity>> = dao.getAllSessions()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val deliveries: StateFlow<List<DeliveryEntity>> = dao.getAllDeliveries()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  private val _activePlayerId = MutableStateFlow<String?>(prefs.getString("active_player_id", "p_mitchell"))
  val activePlayerId: StateFlow<String?> = _activePlayerId.asStateFlow()

  private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Modals
  val showPlayerModal = MutableStateFlow(false)
  val showSessionModal = MutableStateFlow(false)
  val showClearConfirmDialog = MutableStateFlow(false)
  var pendingSaveDelivery: DeliveryEntity? = null

  // Analyze state
  val analyzeTab = MutableStateFlow(AnalyzeTab.IMPORT)
  val videoUri = MutableStateFlow<Uri?>(null)
  val videoDurationMs = MutableStateFlow(0L)
  val currentPositionMs = MutableStateFlow(0L)
  val isPlaying = MutableStateFlow(false)
  val detectedFps = MutableStateFlow<Double?>(null)
  val fpsInput = MutableStateFlow("30")
  val distanceInput = MutableStateFlow("20.12")
  val bounceDistInput = MutableStateFlow("")
  val cameraAngle = MutableStateFlow("side")

  val startMarkSec = MutableStateFlow<Double?>(null)
  val bounceMarkSec = MutableStateFlow<Double?>(null)
  val endMarkSec = MutableStateFlow<Double?>(null)

  val isAnalyzingMotion = MutableStateFlow(false)
  val suggestedReleaseSec = MutableStateFlow<Double?>(null)
  val suggestedArrivalSec = MutableStateFlow<Double?>(null)

  val lastResult = MutableStateFlow<DeliveryEntity?>(null)

  // Compare state
  val compareMode = MutableStateFlow(CompareMode.PLAYERS)
  val compareIdA = MutableStateFlow("")
  val compareIdB = MutableStateFlow("")

  init {
    viewModelScope.launch {
      players.collect { list ->
        if (list.isNotEmpty() && (_activePlayerId.value == null || list.none { it.id == _activePlayerId.value })) {
          setActivePlayer(list.first().id)
        }
      }
    }
  }

  fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
  }

  fun setActivePlayer(id: String) {
    _activePlayerId.value = id
    prefs.edit().putString("active_player_id", id).apply()
  }

  fun addPlayer(name: String, hand: String, style: String, team: String) {
    viewModelScope.launch {
      val player = PlayerEntity(
        id = "p_${System.currentTimeMillis()}",
        name = name.trim(),
        hand = hand,
        style = style,
        team = team.trim(),
        createdAt = System.currentTimeMillis()
      )
      dao.insertPlayer(player)
      if (_activePlayerId.value == null) {
        setActivePlayer(player.id)
      }
      showPlayerModal.value = false
    }
  }

  fun onVideoLoaded(uri: Uri, duration: Long) {
    videoUri.value = uri
    videoDurationMs.value = duration
    currentPositionMs.value = 0L
    startMarkSec.value = null
    bounceMarkSec.value = null
    endMarkSec.value = null
    suggestedReleaseSec.value = null
    suggestedArrivalSec.value = null

    // Detect FPS
    val fps = VideoMotionAnalyzer.detectFps(getApplication(), uri)
    detectedFps.value = fps
    if (fps != null && fps > 0) {
      fpsInput.value = fps.toInt().toString()
    } else {
      fpsInput.value = "30"
    }
  }

  fun markPoint(which: String) {
    val sec = currentPositionMs.value / 1000.0
    when (which) {
      "start" -> startMarkSec.value = sec
      "bounce" -> bounceMarkSec.value = sec
      "end" -> endMarkSec.value = sec
    }
  }

  fun stepFrames(frameCount: Int) {
    val fps = fpsInput.value.toDoubleOrNull() ?: 30.0
    val deltaMs = ((frameCount / fps) * 1000).toLong()
    val dur = videoDurationMs.value.coerceAtLeast(1L)
    val newPos = (currentPositionMs.value + deltaMs).coerceIn(0L, dur)
    currentPositionMs.value = newPos
  }

  fun suggestFramesFromMotion() {
    val uri = videoUri.value ?: return
    viewModelScope.launch {
      isAnalyzingMotion.value = true
      val result = VideoMotionAnalyzer.suggestFrames(getApplication(), uri)
      isAnalyzingMotion.value = false
      if (result != null) {
        suggestedReleaseSec.value = result.first
        suggestedArrivalSec.value = result.second
      }
    }
  }

  fun calculateSpeed(): Boolean {
    val s = startMarkSec.value
    val e = endMarkSec.value
    if (s == null || e == null) return false

    val dist = distanceInput.value.toDoubleOrNull() ?: 20.12
    val fps = fpsInput.value.toDoubleOrNull() ?: 30.0
    val time = abs(e - s)
    if (time <= 0.001 || dist <= 0 || fps <= 0) return false

    val mps = dist / time
    val kmh = mps * 3.6
    val mph = kmh * 0.621371

    val confidence = when {
      kmh !in 35.0..185.0 -> "low"
      fps >= 120 && time >= 0.05 -> "high"
      fps >= 60 && time >= 0.04 -> "high"
      fps >= 30 && time >= 0.03 -> "medium"
      else -> "low"
    }

    val b = bounceMarkSec.value
    val bDist = bounceDistInput.value.toDoubleOrNull()
    val hasBounce = b != null && bDist != null && bDist > 0 && b > minOf(s, e) && b < maxOf(s, e)

    val releaseToBounce = if (hasBounce) abs(b!! - s) else null
    val bounceToArrival = if (hasBounce) abs(e - b!!) else null
    val stumpsDist = if (hasBounce) (dist - bDist!!).coerceAtLeast(0.0) else null

    val delivery = DeliveryEntity(
      id = "d_${System.currentTimeMillis()}",
      sessionId = "",
      playerId = _activePlayerId.value ?: (players.value.firstOrNull()?.id ?: "p_unknown"),
      kmh = kmh,
      mph = mph,
      flightTime = time,
      distance = dist,
      fps = fps,
      confidence = confidence,
      angle = cameraAngle.value,
      hasBounce = hasBounce,
      releaseToBounceTime = releaseToBounce,
      bounceToArrivalTime = bounceToArrival,
      bounceDistance = bDist,
      stumpsDistance = stumpsDist,
      timestamp = System.currentTimeMillis()
    )

    lastResult.value = delivery
    analyzeTab.value = AnalyzeTab.RESULT
    return true
  }

  fun saveDeliveryToSession(sessionId: String?, newSessionName: String? = null, playerId: String? = null) {
    val delivery = lastResult.value ?: return
    viewModelScope.launch {
      val targetSessionId = if (!sessionId.isNullOrEmpty()) {
        sessionId
      } else {
        val sId = "s_${System.currentTimeMillis()}"
        val s = SessionEntity(
          id = sId,
          name = newSessionName?.ifBlank { "Session" } ?: "Session",
          playerId = playerId ?: (_activePlayerId.value ?: (players.value.firstOrNull()?.id ?: "p_unknown")),
          createdAt = System.currentTimeMillis()
        )
        dao.insertSession(s)
        sId
      }

      val toSave = delivery.copy(
        id = "d_${UUID.randomUUID()}",
        sessionId = targetSessionId,
        playerId = playerId ?: (_activePlayerId.value ?: delivery.playerId),
        timestamp = System.currentTimeMillis()
      )
      dao.insertDelivery(toSave)
      showSessionModal.value = false
      pendingSaveDelivery = null
    }
  }

  fun createSession(name: String, playerId: String, onCreated: (String) -> Unit) {
    viewModelScope.launch {
      val sId = "s_${System.currentTimeMillis()}"
      val s = SessionEntity(
        id = sId,
        name = name.ifBlank { "Session" },
        playerId = playerId,
        createdAt = System.currentTimeMillis()
      )
      dao.insertSession(s)
      onCreated(sId)
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      dao.clearDeliveries()
      dao.clearSessions()
      dao.clearPlayers()
      _activePlayerId.value = null
      prefs.edit().remove("active_player_id").apply()
      showClearConfirmDialog.value = false
      navigateTo(Screen.Home)
    }
  }

  fun exportJson(): String {
    val root = JSONObject()
    val pArr = JSONArray()
    players.value.forEach { p ->
      val pObj = JSONObject()
      pObj.put("id", p.id)
      pObj.put("name", p.name)
      pObj.put("hand", p.hand)
      pObj.put("style", p.style)
      pObj.put("team", p.team)
      pObj.put("createdAt", p.createdAt)
      pArr.put(pObj)
    }
    root.put("players", pArr)

    val sArr = JSONArray()
    sessions.value.forEach { s ->
      val sObj = JSONObject()
      sObj.put("id", s.id)
      sObj.put("name", s.name)
      sObj.put("playerId", s.playerId)
      sObj.put("createdAt", s.createdAt)
      sArr.put(sObj)
    }
    root.put("sessions", sArr)

    val dArr = JSONArray()
    deliveries.value.forEach { d ->
      val dObj = JSONObject()
      dObj.put("id", d.id)
      dObj.put("sessionId", d.sessionId)
      dObj.put("playerId", d.playerId)
      dObj.put("kmh", d.kmh)
      dObj.put("mph", d.mph)
      dObj.put("flightTime", d.flightTime)
      dObj.put("distance", d.distance)
      dObj.put("fps", d.fps)
      dObj.put("confidence", d.confidence)
      dObj.put("timestamp", d.timestamp)
      dArr.put(dObj)
    }
    root.put("deliveries", dArr)
    root.put("activePlayerId", _activePlayerId.value)
    return root.toString(2)
  }
}
