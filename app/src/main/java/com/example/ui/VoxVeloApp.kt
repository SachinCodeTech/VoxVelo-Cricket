package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddPlayerDialog
import com.example.ui.components.NavScreen
import com.example.ui.components.SessionDetailsDialog
import com.example.ui.components.VoxVeloBottomNav
import com.example.ui.components.VoxVeloTopBar
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PlayerDetailScreen
import com.example.ui.screens.PlayersScreen
import com.example.ui.screens.SessionDetailScreen
import com.example.ui.screens.SessionsScreen
import com.example.ui.screens.TeamDashboardScreen
import com.example.ui.theme.DarkBg
import com.example.ui.viewmodel.SubScreen
import com.example.ui.viewmodel.VoxVeloViewModel

@Composable
fun VoxVeloApp(
    viewModel: VoxVeloViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val currentSubScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()
    val activePlayer by viewModel.activePlayer.collectAsStateWithLifecycle()
    val activePlayerId by viewModel.activePlayerId.collectAsStateWithLifecycle()

    val players by viewModel.players.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()

    // Analyze states
    val analyzeTab by viewModel.analyzeTab.collectAsStateWithLifecycle()
    val videoUri by viewModel.videoUri.collectAsStateWithLifecycle()
    val videoDurationSec by viewModel.videoDurationSec.collectAsStateWithLifecycle()
    val currentTimeSec by viewModel.currentTimeSec.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    val isFpsDetected by viewModel.isFpsDetected.collectAsStateWithLifecycle()
    val distanceM by viewModel.distanceM.collectAsStateWithLifecycle()
    val bounceDistanceM by viewModel.bounceDistanceM.collectAsStateWithLifecycle()
    val cameraAngle by viewModel.cameraAngle.collectAsStateWithLifecycle()
    val startMark by viewModel.startMark.collectAsStateWithLifecycle()
    val bounceMark by viewModel.bounceMark.collectAsStateWithLifecycle()
    val endMark by viewModel.endMark.collectAsStateWithLifecycle()
    val isAnalyzingMotion by viewModel.isAnalyzingMotion.collectAsStateWithLifecycle()
    val motionSuggestion by viewModel.motionSuggestion.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()

    // Modals
    val showPlayerModal by viewModel.showPlayerModal.collectAsStateWithLifecycle()
    val showSessionModal by viewModel.showSessionModal.collectAsStateWithLifecycle()
    val pendingSaveDelivery by viewModel.pendingSaveDelivery.collectAsStateWithLifecycle()

    // Toast
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Hardware Back Button handling
    BackHandler(enabled = currentSubScreen != null) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            VoxVeloTopBar(
                activePlayerName = activePlayer?.name,
                onPlayerChipClick = {
                    viewModel.navigateTo(NavScreen.PLAYERS)
                }
            )
        },
        bottomBar = {
            VoxVeloBottomNav(
                currentScreen = currentNav,
                onScreenSelected = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            when (val sub = currentSubScreen) {
                is SubScreen.SessionDetail -> {
                    val session = sessions.find { it.id == sub.sessionId }
                    if (session != null) {
                        SessionDetailScreen(
                            session = session,
                            deliveries = deliveries,
                            players = players,
                            onBackClick = { viewModel.navigateBack() },
                            onDeleteSession = { sid ->
                                viewModel.deleteSession(sid)
                                viewModel.navigateBack()
                            },
                            onDeleteDelivery = { did ->
                                viewModel.deleteDelivery(did)
                            }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }
                is SubScreen.PlayerDetail -> {
                    val player = players.find { it.id == sub.playerId }
                    if (player != null) {
                        PlayerDetailScreen(
                            player = player,
                            sessions = sessions,
                            deliveries = deliveries,
                            isActive = player.id == activePlayerId,
                            onBackClick = { viewModel.navigateBack() },
                            onSetActivePlayer = { pid ->
                                viewModel.setActivePlayer(pid)
                            },
                            onSessionSelected = { sid ->
                                viewModel.navigateToSubScreen(SubScreen.SessionDetail(sid))
                            },
                            onDeletePlayer = { pid ->
                                viewModel.deletePlayer(pid)
                                viewModel.navigateBack()
                            }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }
                is SubScreen.TeamDashboard -> {
                    TeamDashboardScreen(
                        players = players,
                        sessions = sessions,
                        deliveries = deliveries,
                        onBackClick = { viewModel.navigateBack() },
                        onPlayerSelected = { pid ->
                            viewModel.navigateToSubScreen(SubScreen.PlayerDetail(pid))
                        }
                    )
                }
                is SubScreen.Compare -> {
                    CompareScreen(
                        players = players,
                        sessions = sessions,
                        deliveries = deliveries,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }
                null -> {
                    when (currentNav) {
                        NavScreen.HOME -> {
                            HomeScreen(
                                deliveries = deliveries,
                                sessions = sessions,
                                players = players,
                                onAnalyzeClick = {
                                    viewModel.navigateTo(NavScreen.ANALYZE)
                                    viewModel.setAnalyzeTab("import")
                                },
                                onSpeedBoardClick = {
                                    viewModel.navigateTo(NavScreen.ANALYZE)
                                    viewModel.setAnalyzeTab("result")
                                },
                                onSessionsClick = {
                                    viewModel.navigateTo(NavScreen.SESSIONS)
                                },
                                onSessionSelected = { sid ->
                                    viewModel.navigateToSubScreen(SubScreen.SessionDetail(sid))
                                }
                            )
                        }
                        NavScreen.SESSIONS -> {
                            SessionsScreen(
                                sessions = sessions,
                                deliveries = deliveries,
                                players = players,
                                onStartNewSessionClick = {
                                    if (players.isEmpty()) {
                                        viewModel.openPlayerModal()
                                    } else {
                                        viewModel.startNewSessionFlow()
                                    }
                                },
                                onSessionSelected = { sid ->
                                    viewModel.navigateToSubScreen(SubScreen.SessionDetail(sid))
                                }
                            )
                        }
                        NavScreen.ANALYZE -> {
                            AnalyzeScreen(
                                currentTab = analyzeTab,
                                onTabSelected = { viewModel.setAnalyzeTab(it) },
                                videoUri = videoUri,
                                videoDurationSec = videoDurationSec,
                                currentTimeSec = currentTimeSec,
                                fps = fps,
                                isFpsDetected = isFpsDetected,
                                distanceM = distanceM,
                                bounceDistanceM = bounceDistanceM,
                                cameraAngle = cameraAngle,
                                startMark = startMark,
                                bounceMark = bounceMark,
                                endMark = endMark,
                                isAnalyzingMotion = isAnalyzingMotion,
                                motionSuggestion = motionSuggestion,
                                lastResult = lastResult,
                                onVideoSelected = { viewModel.setVideoUri(it) },
                                onFpsChanged = { viewModel.setFps(it) },
                                onDistanceChanged = { viewModel.setDistance(it) },
                                onBounceDistanceChanged = { viewModel.setBounceDistance(it) },
                                onCameraAngleChanged = { viewModel.setCameraAngle(it) },
                                onCurrentTimeChanged = { viewModel.setCurrentTimeSec(it) },
                                onStepFrames = { viewModel.stepFrames(it) },
                                onMarkRelease = { viewModel.markRelease() },
                                onMarkBounce = { viewModel.markBounce() },
                                onMarkArrival = { viewModel.markArrival() },
                                onSuggestMotion = { viewModel.suggestFramesFromMotion() },
                                onJumpSuggestion = { viewModel.jumpToSuggestion(it) },
                                onCalculateSpeed = { viewModel.calculateSpeed() },
                                onSaveDelivery = { viewModel.initiateSaveDelivery() }
                            )
                        }
                        NavScreen.PLAYERS -> {
                            PlayersScreen(
                                players = players,
                                sessions = sessions,
                                deliveries = deliveries,
                                activePlayerId = activePlayerId,
                                onAddPlayerClick = { viewModel.openPlayerModal() },
                                onTeamDashboardClick = {
                                    viewModel.navigateToSubScreen(SubScreen.TeamDashboard)
                                },
                                onCompareClick = {
                                    viewModel.navigateToSubScreen(SubScreen.Compare)
                                },
                                onPlayerSelected = { pid ->
                                    viewModel.navigateToSubScreen(SubScreen.PlayerDetail(pid))
                                }
                            )
                        }
                        NavScreen.MORE -> {
                            MoreScreen(
                                onExportDataClick = { viewModel.exportDataJson(context) },
                                onClearDataClick = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modals
    if (showPlayerModal) {
        AddPlayerDialog(
            onDismiss = { viewModel.closePlayerModal() },
            onSave = { name, hand, style, team ->
                viewModel.savePlayer(name, hand, style, team)
            }
        )
    }

    if (showSessionModal) {
        SessionDetailsDialog(
            players = players,
            activePlayerId = activePlayerId,
            isSavingDelivery = pendingSaveDelivery != null,
            onDismiss = { viewModel.closeSessionModal() },
            onConfirm = { name, playerId ->
                viewModel.confirmSaveDelivery(name, playerId)
            }
        )
    }
}
