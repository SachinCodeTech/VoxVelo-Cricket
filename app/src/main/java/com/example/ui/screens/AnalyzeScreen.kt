package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.data.model.Delivery
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PitchOverlayView
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatRowData
import com.example.ui.components.StatStrip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedDim
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentTealDim
import com.example.ui.theme.ConfidHighBg
import com.example.ui.theme.ConfidHighFg
import com.example.ui.theme.ConfidLowBg
import com.example.ui.theme.ConfidLowFg
import com.example.ui.theme.ConfidMedBg
import com.example.ui.theme.ConfidMedFg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurface2
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MotionSuggestion
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    currentTab: String, // "import" or "result"
    onTabSelected: (String) -> Unit,
    videoUri: Uri?,
    videoDurationSec: Double,
    currentTimeSec: Double,
    fps: String,
    isFpsDetected: Boolean,
    distanceM: String,
    bounceDistanceM: String,
    cameraAngle: String,
    startMark: Double?,
    bounceMark: Double?,
    endMark: Double?,
    isAnalyzingMotion: Boolean,
    motionSuggestion: MotionSuggestion?,
    lastResult: Delivery?,
    onVideoSelected: (Uri?) -> Unit,
    onFpsChanged: (String) -> Unit,
    onDistanceChanged: (String) -> Unit,
    onBounceDistanceChanged: (String) -> Unit,
    onCameraAngleChanged: (String) -> Unit,
    onCurrentTimeChanged: (Double) -> Unit,
    onStepFrames: (Int) -> Unit,
    onMarkRelease: () -> Unit,
    onMarkBounce: () -> Unit,
    onMarkArrival: () -> Unit,
    onSuggestMotion: () -> Unit,
    onJumpSuggestion: (String) -> Unit,
    onCalculateSpeed: () -> Unit,
    onSaveDelivery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Video Pickers
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onVideoSelected(uri)
        }
    }

    var tempVideoFile by remember { mutableStateOf<File?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && tempVideoUri != null) {
            onVideoSelected(tempVideoUri)
        }
    }

    // VideoView instance holder for seeking
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    LaunchedEffect(currentTimeSec) {
        val targetMs = (currentTimeSec * 1000).toInt()
        videoViewInstance?.let { vv ->
            if (Math.abs(vv.currentPosition - targetMs) > 100) {
                vv.seekTo(targetMs)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Analyze delivery",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Import or record a bowling delivery, mark release, bounce and arrival, and get an estimated speed with a pitch overlay.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Tabbar: Import / Record vs Result
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .border(
                        width = 1.dp,
                        color = if (currentTab == "import") AccentTeal else DarkLineStrong,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .background(if (currentTab == "import") DarkSurface2 else DarkSurface)
                    .clickable { onTabSelected("import") }
                    .padding(vertical = 10.dp)
                    .testTag("tab_import"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Import / Record",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (currentTab == "import") TextPrimary else TextDim
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .border(
                        width = 1.dp,
                        color = if (currentTab == "result") AccentTeal else DarkLineStrong,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .background(if (currentTab == "result") DarkSurface2 else DarkSurface)
                    .clickable { onTabSelected("result") }
                    .padding(vertical = 10.dp)
                    .testTag("tab_result"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Result",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (currentTab == "result") TextPrimary else TextDim
                )
            }
        }

        if (currentTab == "import") {
            // Import / Record pane
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                        .testTag("import_video_button")
                ) {
                    Text("Import video", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        try {
                            val f = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp4")
                            tempVideoFile = f
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                f
                            )
                            tempVideoUri = uri
                            videoCaptureLauncher.launch(uri)
                        } catch (e: Exception) {
                            // fallback video capture without fileprovider if not registered
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                        .testTag("record_now_button")
                ) {
                    Text("Record now", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (videoUri != null) {
                // Video Player Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(videoUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    seekTo((currentTimeSec * 1000).toInt())
                                }
                                videoViewInstance = this
                            }
                        },
                        update = { vv ->
                            videoViewInstance = vv
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // FPS badge (top left)
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isFpsDetected) "FPS $fps (detected)" else "FPS $fps",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    // Marker badges (top right)
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // S badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xAA000000))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (startMark != null) "S ${String.format("%.2f", startMark)}s" else "S —",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (startMark != null) AccentTeal else TextDim
                            )
                        }
                        // B badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xAA000000))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (bounceMark != null) "B ${String.format("%.2f", bounceMark)}s" else "B —",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (bounceMark != null) AccentAmber else TextDim
                            )
                        }
                        // E badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xAA000000))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (endMark != null) "E ${String.format("%.2f", endMark)}s" else "E —",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (endMark != null) AccentRed else TextDim
                            )
                        }
                    }
                }

                // Scrub Slider
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = currentTimeSec.toFloat(),
                    onValueChange = { onCurrentTimeChanged(it.toDouble()) },
                    valueRange = 0f..Math.max(0.1f, videoDurationSec.toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentTeal,
                        activeTrackColor = AccentTeal,
                        inactiveTrackColor = DarkSurface2
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("video_scrub_slider")
                )

                // Frame step buttons: -5f, -1f, curTime, +1f, +5f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrameStepButton("-5f") { onStepFrames(-5) }
                    Spacer(modifier = Modifier.width(6.dp))
                    FrameStepButton("-1f") { onStepFrames(-1) }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${String.format("%.3f", currentTimeSec)}s",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDim,
                        modifier = Modifier.width(80.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    FrameStepButton("+1f") { onStepFrames(1) }
                    Spacer(modifier = Modifier.width(6.dp))
                    FrameStepButton("+5f") { onStepFrames(5) }
                }

                // 3 Mark Buttons
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarkButton(
                        label = "RELEASE (start)",
                        value = if (startMark != null) "${String.format("%.3f", startMark)}s" else "not set",
                        borderColor = AccentTealDim,
                        onClick = onMarkRelease,
                        modifier = Modifier.weight(1f).testTag("mark_release_button")
                    )
                    MarkButton(
                        label = "BOUNCE (optional)",
                        value = if (bounceMark != null) "${String.format("%.3f", bounceMark)}s" else "not set",
                        borderColor = Color(0xFF4A3C12),
                        onClick = onMarkBounce,
                        modifier = Modifier.weight(1f).testTag("mark_bounce_button")
                    )
                    MarkButton(
                        label = "ARRIVAL (end)",
                        value = if (endMark != null) "${String.format("%.3f", endMark)}s" else "not set",
                        borderColor = AccentRedDim,
                        onClick = onMarkArrival,
                        modifier = Modifier.weight(1f).testTag("mark_arrival_button")
                    )
                }

                // Suggest frames from motion
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSuggestMotion,
                    enabled = !isAnalyzingMotion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                        .testTag("suggest_frames_button")
                ) {
                    Text(
                        text = if (isAnalyzingMotion) "Analyzing motion…" else "Suggest frames from motion",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (motionSuggestion != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggested from frame-to-frame motion, not a confirmed ball track — scrub and check before marking.",
                        fontSize = 12.sp,
                        color = TextFaint,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onJumpSuggestion("release") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurface2,
                                contentColor = TextDim
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Jump to suggested release (${String.format("%.2f", motionSuggestion.releaseSec)}s)", fontSize = 11.5.sp)
                        }
                        Button(
                            onClick = { onJumpSuggestion("arrival") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurface2,
                                contentColor = TextDim
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Jump to suggested arrival (${String.format("%.2f", motionSuggestion.arrivalSec)}s)", fontSize = 11.5.sp)
                        }
                    }
                }

                // Measurement inputs
                SectionLabel(text = "Measurement inputs")

                // Distance
                Text(
                    text = "Distance (m) — release point to arrival/reference point",
                    fontSize = 12.sp,
                    color = TextDim,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = distanceM,
                    onValueChange = onDistanceChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2,
                        unfocusedContainerColor = DarkSurface2,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = DarkLineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_distance")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bounce distance
                Text(
                    text = "Bounce distance (m) — release point to bounce, only if bounce marked",
                    fontSize = 12.sp,
                    color = TextDim,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = bounceDistanceM,
                    onValueChange = onBounceDistanceChanged,
                    placeholder = { Text("e.g. 15.5", color = TextFaint) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2,
                        unfocusedContainerColor = DarkSurface2,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = DarkLineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_bounce_distance")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Grid for FPS & Camera Angle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FPS",
                            fontSize = 12.sp,
                            color = TextDim,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = fps,
                            onValueChange = onFpsChanged,
                            placeholder = { Text("30", color = TextFaint) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface2,
                                unfocusedContainerColor = DarkSurface2,
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = DarkLineStrong,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_fps")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Camera angle",
                            fontSize = 12.sp,
                            color = TextDim,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        var expanded by remember { mutableStateOf(false) }
                        val options = listOf("side" to "Side-on (recommended)", "behind" to "Behind bowler", "other" to "Other")
                        val currentLabel = options.find { it.first == cameraAngle }?.second ?: "Side-on"

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = currentLabel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface2,
                                    unfocusedContainerColor = DarkSurface2,
                                    focusedBorderColor = AccentTeal,
                                    unfocusedBorderColor = DarkLineStrong,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("select_camera_angle")
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(DarkSurface2)
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.second, color = TextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            onCameraAngleChanged(option.first)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Calculate speed button
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onCalculateSpeed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("calculate_speed_button")
                ) {
                    Text(
                        text = "Calculate speed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Estimated Speed — not a certified radar measurement. Accuracy depends on FPS, camera angle, calibration and ball visibility. Bounce marking is manual — the ball is never auto-tracked or fabricated.",
                    fontSize = 12.sp,
                    color = TextFaint,
                    lineHeight = 17.sp
                )
            } else {
                EmptyStateCard(
                    title = "No video loaded",
                    description = "Import a clip from your gallery or record a new delivery to begin."
                )
            }
        } else {
            // Result pane
            if (lastResult == null) {
                EmptyStateCard(
                    title = "No result yet",
                    description = "Calculate a delivery in the Import tab first."
                )
            } else {
                // Speedboard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(18.dp))
                        .background(DarkSurface)
                        .padding(vertical = 30.dp, horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Confidence chip
                        val (cBg, cFg) = when (lastResult.confidence) {
                            "high" -> Pair(ConfidHighBg, ConfidHighFg)
                            "medium" -> Pair(ConfidMedBg, ConfidMedFg)
                            else -> Pair(ConfidLowBg, ConfidLowFg)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(cBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(cFg))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Confidence: ${lastResult.confidence.uppercase()}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cFg
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = String.format("%.1f", lastResult.kmh),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            letterSpacing = (-1.5).sp
                        )

                        Text(
                            text = "km/h · ${String.format("%.1f", lastResult.mph)} mph",
                            fontSize = 14.sp,
                            color = TextDim,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "ESTIMATED BALL SPEED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextFaint,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )

                        // 3 details
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .border(width = 1.dp, color = DarkLineStrong, shape = RoundedCornerShape(10.dp))
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Flight time", fontSize = 10.5.sp, color = TextFaint)
                                Text(
                                    text = "${String.format("%.3f", lastResult.flightTime)}s",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Distance", fontSize = 10.5.sp, color = TextFaint)
                                Text(
                                    text = "${String.format("%.2f", lastResult.distance)}m",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FPS", fontSize = 10.5.sp, color = TextFaint)
                                Text(
                                    text = "${lastResult.fps.toInt()}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (lastResult.confidence == "low") {
                        "Low confidence — reposition camera, improve lighting, or use a higher-FPS recording for a more reliable estimate."
                    } else {
                        "Method: Video Frame Analysis (Distance ÷ Flight Time). Estimated speed, not a certified radar measurement."
                    },
                    fontSize = 12.sp,
                    color = if (lastResult.confidence == "low") AccentAmber else TextFaint,
                    lineHeight = 17.sp
                )

                // Bounce / pitch analysis if bounce was recorded
                if (lastResult.hasBounce) {
                    SectionLabel(text = "Bounce / pitch analysis")
                    StatStrip(
                        stats = listOf(
                            StatRowData("Release → bounce time", if (lastResult.releaseToBounceTime != null) "${String.format("%.3f", lastResult.releaseToBounceTime)}s" else "—"),
                            StatRowData("Bounce → arrival time", if (lastResult.bounceToArrivalTime != null) "${String.format("%.3f", lastResult.bounceToArrivalTime)}s" else "—"),
                            StatRowData("Bounce distance", if (lastResult.bounceDistance != null) "${String.format("%.2f", lastResult.bounceDistance)}m" else "—"),
                            StatRowData("Bounce → stumps (est.)", if (lastResult.stumpsDistance != null) "${String.format("%.2f", lastResult.stumpsDistance)}m" else "—")
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PitchOverlayView(
                        bounceDistance = lastResult.bounceDistance,
                        totalDistance = lastResult.distance
                    )
                }

                // Action buttons: Save to session | Re-mark frames
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSaveDelivery,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentTeal,
                            contentColor = Color(0xFF052824)
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("save_to_session_button")
                    ) {
                        Text("Save to session", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onTabSelected("import") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                            .testTag("remark_frames_button")
                    ) {
                        Text("Re-mark frames", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun FrameStepButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 38.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun MarkButton(
    label: String,
    value: String,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextFaint,
                letterSpacing = 0.2.sp
            )
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}
