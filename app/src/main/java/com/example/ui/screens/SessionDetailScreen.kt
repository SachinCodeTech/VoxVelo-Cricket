package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.data.model.calculateSessionStats
import com.example.data.model.generateCoachInsights
import com.example.ui.components.CoachInsightsBox
import com.example.ui.components.DeliveryLineChart
import com.example.ui.components.ProgressFatigueChart
import com.example.ui.components.SectionLabel
import com.example.ui.components.SpeedHistogram
import com.example.ui.components.StatRowData
import com.example.ui.components.StatStrip
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    session: Session,
    deliveries: List<Delivery>,
    players: List<Player>,
    onBackClick: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onDeleteDelivery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val player = players.find { it.id == session.playerId }
    val sessionDeliveries = deliveries.filter { it.sessionId == session.id }
    val stats = calculateSessionStats(sessionDeliveries)
    val coach = generateCoachInsights(sessionDeliveries)
    val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("session_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDim
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sessions", color = TextDim, fontSize = 13.5.sp)
            }

            IconButton(
                onClick = { onDeleteSession(session.id) },
                modifier = Modifier.testTag("delete_session_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = TextFaint
                )
            }
        }

        Text(
            text = session.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "${player?.name ?: "—"} · ${dateFormat.format(Date(session.createdAt))}",
            fontSize = 13.5.sp,
            color = TextDim,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Stat strip
        StatStrip(
            stats = listOf(
                StatRowData("Fastest", if (stats.fastest != null) "${String.format("%.1f", stats.fastest)} km/h" else "—"),
                StatRowData("Slowest", if (stats.slowest != null) "${String.format("%.1f", stats.slowest)} km/h" else "—"),
                StatRowData("Average", if (stats.avg != null) "${String.format("%.1f", stats.avg)} km/h" else "—"),
                StatRowData("Median", if (stats.median != null) "${String.format("%.1f", stats.median)} km/h" else "—"),
                StatRowData("Consistency", if (stats.consistency != null) "${String.format("%.0f", stats.consistency)}%" else "—"),
                StatRowData("Valid / low-confidence", "${stats.valid} / ${stats.low}")
            )
        )

        // Chart 1: Speed by delivery
        SectionLabel(text = "Speed by delivery")
        DeliveryLineChart(speeds = sessionDeliveries.map { it.kmh })

        // Chart 2: Distribution & session progress
        SectionLabel(text = "Distribution & session progress")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Speed distribution",
                    fontSize = 10.5.sp,
                    color = TextFaint,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                SpeedHistogram(speeds = sessionDeliveries.map { it.kmh })
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Progress (fatigue trend)",
                    fontSize = 10.5.sp,
                    color = TextFaint,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ProgressFatigueChart(speeds = sessionDeliveries.map { it.kmh })
            }
        }

        // Deliveries list
        SectionLabel(text = "Deliveries")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(14.dp)
        ) {
            if (sessionDeliveries.isEmpty()) {
                Text("No deliveries saved yet.", fontSize = 13.sp, color = TextFaint)
            } else {
                Column {
                    sessionDeliveries.forEachIndexed { index, d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                color = TextFaint,
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = "${timeFormat.format(Date(d.timestamp))} · ${d.confidence}",
                                fontSize = 12.5.sp,
                                color = TextDim,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format("%.1f", d.kmh)} km/h",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteDelivery(d.id) },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete delivery",
                                        tint = TextFaint,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }
                        if (index < sessionDeliveries.size - 1) {
                            HorizontalDivider(color = DarkLine, thickness = 1.dp)
                        }
                    }
                }
            }
        }

        // AI Cricket Coach
        SectionLabel(text = "AI Cricket Coach")
        CoachInsightsBox(insights = coach)

        Spacer(modifier = Modifier.height(80.dp))
    }
}
