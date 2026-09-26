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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.ui.components.CardListItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary

@Composable
fun PlayersScreen(
    players: List<Player>,
    sessions: List<Session>,
    deliveries: List<Delivery>,
    activePlayerId: String?,
    onAddPlayerClick: () -> Unit,
    onTeamDashboardClick: () -> Unit,
    onCompareClick: () -> Unit,
    onPlayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Players",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Create a profile for each bowler you track.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        Button(
            onClick = onAddPlayerClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_player_button")
        ) {
            Text(
                text = "Add player",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onTeamDashboardClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                    .testTag("team_dashboard_button")
            ) {
                Text("Team / academy view", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onCompareClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, DarkLineStrong, RoundedCornerShape(11.dp))
                    .testTag("compare_button")
            ) {
                Text("Compare", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        SectionLabel(text = "All players")

        if (players.isEmpty()) {
            EmptyStateCard(
                title = "No players yet",
                description = "Add a player to start tracking their sessions."
            )
        } else {
            players.forEach { p ->
                val pSessions = sessions.filter { it.playerId == p.id }
                val pDeliveries = deliveries.filter { d -> pSessions.any { it.id == d.sessionId } }
                val speeds = pDeliveries.map { it.kmh }.filter { it > 0.0 }
                val best = speeds.maxOrNull()

                val subtitle = buildString {
                    append("${p.style} · ${p.hand}")
                    if (p.team.isNotEmpty()) {
                        append(" · ${p.team}")
                    }
                }

                CardListItem(
                    title = p.name,
                    subtitle = subtitle,
                    value = if (best != null) String.format("%.1f", best) else "—",
                    valueLabel = "best km/h",
                    titleTrailing = {
                        if (p.id == activePlayerId) {
                            Text(
                                text = " · active",
                                fontSize = 11.sp,
                                color = AccentTeal,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    },
                    onClick = { onPlayerSelected(p.id) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
