package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurface2
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamDashboardScreen(
    players: List<Player>,
    sessions: List<Session>,
    deliveries: List<Delivery>,
    onBackClick: () -> Unit,
    onPlayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Group players by team (empty team -> "Unassigned")
    val grouped = players.groupBy {
        if (it.team.isBlank()) "Unassigned" else it.team.trim()
    }.toList().sortedBy { (team, _) ->
        if (team == "Unassigned") "zzzz" else team
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("team_dashboard_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextDim
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Players", color = TextDim, fontSize = 13.5.sp)
        }

        Text(
            text = "Team & academy view",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Players grouped by the team/academy on their profile, with squad-level speed stats.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        if (grouped.isEmpty()) {
            EmptyStateCard(
                title = "No players yet",
                description = "Add players with a team/academy to see squad stats here."
            )
        } else {
            grouped.forEach { (teamName, teamPlayers) ->
                // Calculate squad stats
                val squadDeliveries = teamPlayers.flatMap { p ->
                    val pSessions = sessions.filter { it.playerId == p.id }
                    deliveries.filter { d -> pSessions.any { it.id == d.sessionId } }
                }
                val squadSpeeds = squadDeliveries.map { it.kmh }.filter { it > 0.0 }
                val fastest = squadSpeeds.maxOrNull()
                val avg = if (squadSpeeds.isNotEmpty()) squadSpeeds.average() else null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .padding(16.dp)
                        .testTag("team_card_$teamName")
                ) {
                    Column {
                        Text(
                            text = teamName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        val sub = buildString {
                            append("${teamPlayers.size} player${if (teamPlayers.size == 1) "" else "s"}")
                            append(" · fastest ${if (fastest != null) String.format("%.1f", fastest) else "—"} km/h")
                            append(" · avg ${if (avg != null) String.format("%.1f", avg) else "—"} km/h")
                        }
                        Text(
                            text = sub,
                            fontSize = 11.5.sp,
                            color = TextFaint,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        // Roster pill chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            teamPlayers.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .border(1.dp, DarkLineStrong, RoundedCornerShape(100.dp))
                                        .background(DarkSurface2)
                                        .clickable { onPlayerSelected(p.id) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = p.name,
                                        fontSize = 11.5.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
