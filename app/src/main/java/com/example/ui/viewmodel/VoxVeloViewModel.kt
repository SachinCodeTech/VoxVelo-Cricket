package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.data.repository.VoxVeloRepository
import com.example.ui.components.NavScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class SubScreen {
    data class SessionDetail(val sessionId: String) : SubScreen()
    data class PlayerDetail(val playerId: String) : SubScreen()
    object TeamDashboard : SubScreen()
    object Compare : SubScreen()
}

data class MotionSuggestion(
    val releaseSec: Double,
    val arrivalSec: Double
)

class VoxVeloViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VoxVeloRepository(AppDatabase.getDatabase(application))

    val players: StateFlow<List<Player>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<Session>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveries: StateFlow<List<Delivery>> = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlayerId: StateFlow<String?> = repository.activePlayerId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePlayer: StateFlow<Player?> = combine(players, activePlayerId) { pList, aId ->
        pList.find { it.id == aId } ?: pList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation state
    private val _currentNav = MutableStateFlow(NavScreen.HOME)
    val currentNav = _currentNav.asStateFlow()

    private val _currentSubScreen = MutableStateFlow<SubScreen?>(null)
    val currentSubScreen = _currentSubScreen.asStateFlow()

    // Analyze state
    private val _analyzeTab = MutableStateFlow("import") // "import" or "result"
    val analyzeTab = _analyzeTab.asStateFlow()

    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri = _videoUri.asStateFlow()

    private val _videoDurationSec = MutableStateFlow(0.0)
    val videoDurationSec = _videoDurationSec.asStateFlow()

    private val _currentTimeSec = MutableStateFlow(0.0)
    val currentTimeSec = _currentTimeSec.asStateFlow()

    private val _fps = MutableStateFlow("30")
    val fps = _fps.asStateFlow()

    private val _isFpsDetected = MutableStateFlow(false)
    val isFpsDetected = _isFpsDetected.asStateFlow()

    private val _distanceM = MutableStateFlow("20.12")
    val distanceM = _distanceM.asStateFlow()

    private val _bounceDistanceM = MutableStateFlow("")
    val bounceDistanceM = _bounceDistanceM.asStateFlow()

    private val _cameraAngle = MutableStateFlow("side")
    val cameraAngle = _cameraAngle.asStateFlow()

    private val _startMark = MutableStateFlow<Double?>(null)
    val startMark = _startMark.asStateFlow()

    private val _bounceMark = MutableStateFlow<Double?>(null)
    val bounceMark = _bounceMark.asStateFlow()

    private val _endMark = MutableStateFlow<Double?>(null)
    val endMark = _endMark.asStateFlow()

    private val _isAnalyzingMotion = MutableStateFlow(false)
    val isAnalyzingMotion = _isAnalyzingMotion.asStateFlow()

    private val _motionSuggestion = MutableStateFlow<MotionSuggestion?>(null)
    val motionSuggestion = _motionSuggestion.asStateFlow()

    private val _lastResult = MutableStateFlow<Delivery?>(null)
    val lastResult = _lastResult.asStateFlow()

    // Modals
    private val _showPlayerModal = MutableStateFlow(false)
    val showPlayerModal = _showPlayerModal.asStateFlow()

    private val _showSessionModal = MutableStateFlow(false)
    val showSessionModal = _showSessionModal.asStateFlow()

    private val _pendingSaveDelivery = MutableStateFlow<Delivery?>(null)
    val pendingSaveDelivery = _pendingSaveDelivery.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        // Seed initial player if totally empty, so user can test right away
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            if (players.value.isEmpty()) {
                val demoPlayer = Player(
                    id = "p_demo",
                    name = "Virat K.",
                    hand = "Right-handed",
                    style = "Fast",
                    team = "VoxVelo Strikers"
                )
                repository.addPlayer(demoPlayer)
                repository.setActivePlayerId(demoPlayer.id)
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: NavScreen) {
        _currentNav.value = screen
        _currentSubScreen.value = null
    }

    fun navigateToSubScreen(subScreen: SubScreen) {
        _currentSubScreen.value = subScreen
    }

    fun navigateBack() {
        if (_currentSubScreen.value != null) {
            _currentSubScreen.value = null
        }
    }

    fun setAnalyzeTab(tab: String) {
        _analyzeTab.value = tab
    }

    fun setVideoUri(uri: Uri?) {
        _videoUri.value = uri
        _startMark.value = null
        _bounceMark.value = null
        _endMark.value = null
        _motionSuggestion.value = null
        _currentTimeSec.value = 0.0

        if (uri != null) {
            detectVideoProperties(uri)
        }
    }

    private fun detectVideoProperties(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L
                _videoDurationSec.value = durationMs / 1000.0

                // Try detecting frame rate using MediaExtractor
                var detectedFpsVal: Int? = null
                try {
                    val extractor = MediaExtractor()
                    extractor.setDataSource(context, uri, null)
                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME)
                        if (mime?.startsWith("video/") == true) {
                            if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                                detectedFpsVal = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                            }
                            break
                        }
                    }
                    extractor.release()
                } catch (_: Exception) {}

                if (detectedFpsVal != null && detectedFpsVal > 0) {
                    _fps.value = detectedFpsVal.toString()
                    _isFpsDetected.value = true
                } else {
                    _isFpsDetected.value = false
                }
                retriever.release()
            } catch (e: Exception) {
                _isFpsDetected.value = false
            }
        }
    }

    fun setFps(value: String) {
        _fps.value = value
    }

    fun setDistance(value: String) {
        _distanceM.value = value
    }

    fun setBounceDistance(value: String) {
        _bounceDistanceM.value = value
    }

    fun setCameraAngle(value: String) {
        _cameraAngle.value = value
    }

    fun setCurrentTimeSec(sec: Double) {
        val dur = _videoDurationSec.value
        val clamped = if (dur > 0) Math.max(0.0, Math.min(dur, sec)) else Math.max(0.0, sec)
        _currentTimeSec.value = clamped
    }

    fun stepFrames(frameCount: Int) {
        val currentFps = _fps.value.toDoubleOrNull() ?: 30.0
        val dt = frameCount / currentFps
        setCurrentTimeSec(_currentTimeSec.value + dt)
    }

    fun markRelease() {
        _startMark.value = _currentTimeSec.value
    }

    fun markBounce() {
        _bounceMark.value = _currentTimeSec.value
    }

    fun markArrival() {
        _endMark.value = _currentTimeSec.value
    }

    fun suggestFramesFromMotion() {
        val uri = _videoUri.value ?: return
        viewModelScope.launch {
            _isAnalyzingMotion.value = true
            try {
                val suggestion = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 3000L
                    val durationSec = durationMs / 1000.0

                    val sampleCount = 24
                    val frames = mutableListOf<Pair<Double, FloatArray>>()
                    val targetW = 48
                    val targetH = 27

                    for (i in 0..sampleCount) {
                        val tSec = (i.toDouble() / sampleCount) * durationSec
                        val timeUs = (tSec * 1_000_000).toLong()
                        val bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        if (bmp != null) {
                            val scaled = Bitmap.createScaledBitmap(bmp, targetW, targetH, false)
                            val pixels = IntArray(targetW * targetH)
                            scaled.getPixels(pixels, 0, targetW, 0, 0, targetW, targetH)
                            val gray = FloatArray(pixels.size)
                            for (p in pixels.indices) {
                                val c = pixels[p]
                                val r = (c shr 16) and 0xFF
                                val g = (c shr 8) and 0xFF
                                val b = c and 0xFF
                                gray[p] = (r + g + b) / 3f
                            }
                            frames.add(Pair(tSec, gray))
                            if (scaled != bmp) scaled.recycle()
                            bmp.recycle()
                        }
                    }
                    retriever.release()

                    if (frames.size >= 4) {
                        val diffs = mutableListOf<Pair<Double, Float>>()
                        for (i in 1 until frames.size) {
                            var diff = 0f
                            val gCurr = frames[i].second
                            val gPrev = frames[i - 1].second
                            val sz = Math.min(gCurr.size, gPrev.size)
                            for (p in 0 until sz) {
                                diff += Math.abs(gCurr[p] - gPrev[p])
                            }
                            diffs.add(Pair(frames[i].first, diff))
                        }
                        val mid = Math.max(1, diffs.size / 2)
                        val firstHalf = diffs.subList(0, mid)
                        val secondHalf = diffs.subList(mid, diffs.size)

                        val relPeak = firstHalf.maxByOrNull { it.second }?.first ?: (durationSec * 0.25)
                        val arrPeak = secondHalf.maxByOrNull { it.second }?.first ?: (durationSec * 0.75)
                        MotionSuggestion(releaseSec = relPeak, arrivalSec = arrPeak)
                    } else {
                        MotionSuggestion(releaseSec = durationSec * 0.25, arrivalSec = durationSec * 0.75)
                    }
                }
                _motionSuggestion.value = suggestion
            } catch (e: Exception) {
                _toastMessage.value = "Motion analysis unavailable for this video format."
            } finally {
                _isAnalyzingMotion.value = false
            }
        }
    }

    fun jumpToSuggestion(which: String) {
        val s = _motionSuggestion.value ?: return
        if (which == "release") {
            setCurrentTimeSec(s.releaseSec)
        } else {
            setCurrentTimeSec(s.arrivalSec)
        }
    }

    fun calculateSpeed(): Boolean {
        val start = _startMark.value
        val end = _endMark.value
        if (start == null || end == null) {
            _toastMessage.value = "Mark both the release frame and the arrival frame first."
            return false
        }
        val dist = _distanceM.value.toDoubleOrNull()
        if (dist == null || dist <= 0.0) {
            _toastMessage.value = "Enter a valid distance in meters."
            return false
        }
        val fpsVal = _fps.value.toDoubleOrNull()
        if (fpsVal == null || fpsVal <= 0.0) {
            _toastMessage.value = "Enter a valid FPS."
            return false
        }
        val time = Math.abs(end - start)
        if (time <= 0.001) {
            _toastMessage.value = "Start and end frames must differ."
            return false
        }

        val mps = dist / time
        val kmh = mps * 3.6
        val mph = kmh * 0.621371

        var conf = "low"
        if (fpsVal >= 120 && time >= 0.05) conf = "high"
        else if (fpsVal >= 60 && time >= 0.04) conf = "high"
        else if (fpsVal >= 30 && time >= 0.03) conf = "medium"

        if (kmh > 180.0 || kmh < 40.0) conf = "low"

        val bounceT = _bounceMark.value
        val bounceDistVal = _bounceDistanceM.value.toDoubleOrNull()
        val relT = Math.min(start, end)
        val arrT = Math.max(start, end)

        val hasBounce = (bounceT != null && bounceDistVal != null && bounceDistVal > 0 && bounceT > relT && bounceT < arrT)

        val delivery = Delivery(
            id = "d_" + System.currentTimeMillis(),
            sessionId = _currentSessionId.value ?: "",
            kmh = kmh,
            mph = mph,
            flightTime = time,
            distance = dist,
            fps = fpsVal,
            confidence = conf,
            cameraAngle = _cameraAngle.value,
            hasBounce = hasBounce,
            releaseToBounceTime = if (hasBounce) Math.abs(bounceT!! - relT) else null,
            bounceToArrivalTime = if (hasBounce) Math.abs(arrT - bounceT!!) else null,
            bounceDistance = if (hasBounce) bounceDistVal else null,
            stumpsDistance = if (hasBounce) Math.max(0.0, dist - bounceDistVal!!) else null,
            timestamp = System.currentTimeMillis()
        )

        _lastResult.value = delivery
        _analyzeTab.value = "result"
        return true
    }

    fun initiateSaveDelivery() {
        val result = _lastResult.value ?: return
        val currentSid = _currentSessionId.value
        val currentSession = sessions.value.find { it.id == currentSid }
        if (currentSession != null) {
            viewModelScope.launch {
                val deliveryWithSession = result.copy(sessionId = currentSession.id)
                repository.addDelivery(deliveryWithSession)
                _toastMessage.value = "Delivery saved to \"${currentSession.name}\"."
                navigateTo(NavScreen.HOME)
            }
        } else {
            _pendingSaveDelivery.value = result
            _showSessionModal.value = true
        }
    }

    fun confirmSaveDelivery(sessionName: String, playerId: String) {
        viewModelScope.launch {
            val session = repository.createSession(sessionName, playerId)
            _currentSessionId.value = session.id
            val pending = _pendingSaveDelivery.value
            if (pending != null) {
                repository.addDelivery(pending.copy(sessionId = session.id))
                _toastMessage.value = "Session \"$sessionName\" created and delivery saved."
            }
            _pendingSaveDelivery.value = null
            _showSessionModal.value = false
            navigateTo(NavScreen.HOME)
        }
    }

    fun startNewSessionFlow() {
        _pendingSaveDelivery.value = null
        _showSessionModal.value = true
    }

    fun closeSessionModal() {
        _showSessionModal.value = false
        _pendingSaveDelivery.value = null
    }

    fun openPlayerModal() {
        _showPlayerModal.value = true
    }

    fun closePlayerModal() {
        _showPlayerModal.value = false
    }

    fun savePlayer(name: String, hand: String, style: String, team: String) {
        viewModelScope.launch {
            val player = Player(
                id = "p_" + System.currentTimeMillis(),
                name = name,
                hand = hand,
                style = style,
                team = team.trim()
            )
            repository.addPlayer(player)
            _showPlayerModal.value = false
            _toastMessage.value = "Player \"$name\" added."
        }
    }

    fun setActivePlayer(id: String) {
        viewModelScope.launch {
            repository.setActivePlayerId(id)
            _toastMessage.value = "Active player updated."
        }
    }

    fun deletePlayer(id: String) {
        viewModelScope.launch {
            repository.deletePlayer(id)
            _toastMessage.value = "Player deleted."
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            repository.deleteSession(id)
            if (_currentSessionId.value == id) {
                _currentSessionId.value = null
            }
            _toastMessage.value = "Session deleted."
        }
    }

    fun deleteDelivery(id: String) {
        viewModelScope.launch {
            repository.deleteDelivery(id)
            _toastMessage.value = "Delivery deleted."
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentSessionId.value = null
            _lastResult.value = null
            _toastMessage.value = "Local data cleared."
        }
    }

    fun exportDataJson(context: Context) {
        viewModelScope.launch {
            try {
                val json = repository.exportJson(
                    players = players.value,
                    sessions = sessions.value,
                    deliveries = deliveries.value,
                    activeId = activePlayerId.value
                )
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, json)
                    type = "application/json"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export VoxVelo Data")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                _toastMessage.value = "Failed to export data: ${e.message}"
            }
        }
    }
}
