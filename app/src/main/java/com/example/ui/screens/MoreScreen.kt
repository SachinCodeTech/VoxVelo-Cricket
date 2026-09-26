package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionLabel
import com.example.ui.theme.DarkBgRaised
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@Composable
fun MoreScreen(
    onExportDataClick: () -> Unit,
    onClearDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = "More",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "VoxVelo Cricket — your cricket performance lab.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        SectionLabel(text = "Coming soon")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkLineStrong, RoundedCornerShape(16.dp))
                .background(DarkSurface)
        ) {
            Column {
                ComingSoonRow(
                    title = "Automatic ball tracking (needs a CV/pose model)",
                    badge = "Phase 3"
                )
                HorizontalDivider(color = DarkLine, thickness = 1.dp)
                ComingSoonRow(
                    title = "Advanced biomechanics (needs a pose model)",
                    badge = "Phase 3"
                )
                HorizontalDivider(color = DarkLine, thickness = 1.dp)
                ComingSoonRow(
                    title = "Cloud backup & billing",
                    badge = "Future"
                )
            }
        }

        SectionLabel(text = "Data")

        Button(
            onClick = onExportDataClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                .testTag("export_data_button")
        ) {
            Text("Export data (JSON)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { showConfirmClearDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurface,
                contentColor = Color(0xFFF28B82)
            ),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                .testTag("clear_data_button")
        ) {
            Text("Clear all local data", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All videos and measurements stay on this device by default. Nothing is uploaded unless you explicitly enable cloud AI analysis in a future update. The AI Coach runs entirely on your recorded measurements, on-device — it never invents numbers.",
            fontSize = 12.sp,
            color = TextFaint,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Clear all local data?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This deletes all players, sessions and deliveries stored on this device. Continue?",
                    color = TextDim,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClearDialog = false
                        onClearDataClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2520))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = DarkBgRaised,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ComingSoonRow(
    title: String,
    badge: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = TextDim,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .border(1.dp, DarkLineStrong, RoundedCornerShape(100.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = badge,
                fontSize = 10.5.sp,
                color = TextFaint
            )
        }
    }
}
