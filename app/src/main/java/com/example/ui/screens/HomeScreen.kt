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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.CardListItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatRowData
import com.example.ui.components.StatStrip
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedDim
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    deliveries: List<Delivery>,
    sessions: List<Session>,
    players: List<Player>,
    onAnalyzeClick: () -> Unit,
    onSpeedBoardClick: () -> Unit,
    onSessionsClick: () -> Unit,
    onSessionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Last recorded delivery
    val lastDelivery = deliveries.maxByOrNull { it.timestamp }
    val lastSession = lastDelivery?.let { d -> sessions.find { it.id == d.sessionId } }

    val speeds = deliveries.map { it.kmh }.filter { it > 0.0 }
    val fastest = speeds.maxOrNull()
    val avg = if (speeds.isNotEmpty()) speeds.average() else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // Hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, DarkLineStrong, RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentRedDim, DarkSurface),
                        center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                        radius = 800f
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Text(
                    text = "Last recorded delivery",
                    fontSize = 12.sp,
                    color = TextDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (lastDelivery != null) String.format("%.1f", lastDelivery.kmh) else "—",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "km/h",
                        fontSize = 14.sp,
                        color = TextDim,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (lastDelivery != null && lastSession != null) {
                            "${lastSession.name} · ${formatTimeAgo(lastDelivery.timestamp)}"
                        } else "No deliveries yet",
                        fontSize = 12.sp,
                        color = TextFaint,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    Button(
                        onClick = onAnalyzeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.testTag("hero_analyze_button")
                    ) {
                        Text(
                            text = "Analyze a delivery",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stat strip
        StatStrip(
            stats = listOf(
                StatRowData("Fastest recorded", if (fastest != null) "${String.format("%.1f", fastest)} km/h" else "—"),
                StatRowData("Average speed", if (avg != null) "${String.format("%.1f", avg)} km/h" else "—"),
                StatRowData("Total deliveries", deliveries.size.toString()),
                StatRowData("Sessions logged", sessions.size.toString())
            )
        )

        // Quick actions
        SectionLabel(text = "Quick actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Speed Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .clickable { onSpeedBoardClick() }
                    .padding(16.dp)
                    .testTag("quick_speed_board")
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Speed Board",
                        tint = AccentTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Speed Board",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Live single-delivery readout",
                        fontSize = 11.5.sp,
                        color = TextFaint,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // My Sessions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .clickable { onSessionsClick() }
                    .padding(16.dp)
                    .testTag("quick_my_sessions")
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "My Sessions",
                        tint = AccentTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "My Sessions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Review past bowling sessions",
                        fontSize = 11.5.sp,
                        color = TextFaint,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Recent sessions
        SectionLabel(text = "Recent sessions")
        if (sessions.isEmpty()) {
            EmptyStateCard(
                title = "No sessions yet",
                description = "Analyze your first delivery to start a session."
            )
        } else {
            sessions.take(3).forEach { session ->
                val sessionDeliveries = deliveries.filter { it.sessionId == session.id }
                val stats = calculateSessionStats(sessionDeliveries)
                CardListItem(
                    title = session.name,
                    subtitle = "${sessionDeliveries.size} deliveries · ${formatTimeAgo(session.createdAt)}",
                    value = if (stats.avg != null) String.format("%.1f", stats.avg) else "—",
                    valueLabel = "avg km/h",
                    onClick = { onSessionSelected(session.id) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

fun formatTimeAgo(ts: Long): String {
    val sec = (System.currentTimeMillis() - ts) / 1000
    return when {
        sec < 60 -> "just now"
        sec < 3600 -> "${Math.max(1, sec / 60)}m ago"
        sec < 86400 -> "${sec / 3600}h ago"
        else -> "${sec / 86400}d ago"
    }
}
