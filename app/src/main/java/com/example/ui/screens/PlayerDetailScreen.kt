package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.data.model.calculateSessionStats
import com.example.data.model.generateCoachInsights
import com.example.ui.components.CardListItem
import com.example.ui.components.CoachInsightsBox
import com.example.ui.components.DeliveryLineChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatRowData
import com.example.ui.components.StatStrip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@Composable
fun PlayerDetailScreen(
    player: Player,
    sessions: List<Session>,
    deliveries: List<Delivery>,
    isActive: Boolean,
    onBackClick: () -> Unit,
    onSetActivePlayer: (String) -> Unit,
    onSessionSelected: (String) -> Unit,
    onDeletePlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val pSessions = sessions.filter { it.playerId == player.id }.sortedBy { it.createdAt }
    val pDeliveries = deliveries.filter { d -> pSessions.any { it.id == d.sessionId } }
    val speeds = pDeliveries.map { it.kmh }.filter { it > 0.0 }
    val best = speeds.maxOrNull()
    val avg = if (speeds.isNotEmpty()) speeds.average() else null

    // Session averages for trend chart
    val sessionAvgs = pSessions.mapNotNull { s ->
        val sDels = deliveries.filter { it.sessionId == s.id }
        val sSpeeds = sDels.map { it.kmh }.filter { it > 0.0 }
        if (sSpeeds.isNotEmpty()) sSpeeds.average() else null
    }

    val coach = generateCoachInsights(pDeliveries)

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
                modifier = Modifier.testTag("player_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDim
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Players", color = TextDim, fontSize = 13.5.sp)
            }

            IconButton(
                onClick = { onDeletePlayer(player.id) },
                modifier = Modifier.testTag("delete_player_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Player",
                    tint = TextFaint
                )
            }
        }

        Text(
            text = player.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        val sub = buildString {
            append("${player.style} · ${player.hand}")
            if (player.team.isNotEmpty()) {
                append(" · ${player.team}")
            }
        }
        Text(
            text = sub,
            fontSize = 13.5.sp,
            color = TextDim,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Stat strip
        StatStrip(
            stats = listOf(
                StatRowData("Best delivery", if (best != null) "${String.format("%.1f", best)} km/h" else "—"),
                StatRowData("Overall average", if (avg != null) "${String.format("%.1f", avg)} km/h" else "—"),
                StatRowData("Sessions", pSessions.size.toString()),
                StatRowData("Total deliveries", pDeliveries.size.toString())
            )
        )

        // Performance trend chart
        SectionLabel(text = "Performance trend — average speed by session")
        DeliveryLineChart(
            speeds = sessionAvgs,
            lineColor = AccentAmber,
            dotColor = AccentAmber
        )

        // AI Cricket Coach — career summary
        SectionLabel(text = "AI Cricket Coach — career summary")
        CoachInsightsBox(insights = coach)

        // Sessions
        SectionLabel(text = "Sessions")
        if (pSessions.isEmpty()) {
            EmptyStateCard(
                title = "No sessions for this player yet",
                description = "Start a session to log deliveries for ${player.name}."
            )
        } else {
            pSessions.reversed().forEach { session ->
                val sDels = deliveries.filter { it.sessionId == session.id }
                val stats = calculateSessionStats(sDels)
                CardListItem(
                    title = session.name,
                    subtitle = "${sDels.size} deliveries · ${formatTimeAgo(session.createdAt)}",
                    value = if (stats.avg != null) String.format("%.1f", stats.avg) else "—",
                    valueLabel = "avg km/h",
                    onClick = { onSessionSelected(session.id) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = { onSetActivePlayer(player.id) },
            enabled = !isActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurface,
                contentColor = if (isActive) AccentTeal else TextPrimary
            ),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                .testTag("set_active_player_button")
        ) {
            Text(
                text = if (isActive) "Current active player" else "Set as active player",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
