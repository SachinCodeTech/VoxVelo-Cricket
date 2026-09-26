package com.example.ui.screens

import androidx.compose.foundation.layout.Column
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
import com.example.data.model.calculateSessionStats
import com.example.ui.components.CardListItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionLabel
import com.example.ui.theme.AccentRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary

@Composable
fun SessionsScreen(
    sessions: List<Session>,
    deliveries: List<Delivery>,
    players: List<Player>,
    onStartNewSessionClick: () -> Unit,
    onSessionSelected: (String) -> Unit,
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
            text = "Sessions",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Every bowling session you've recorded, with speed and consistency at a glance.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        Button(
            onClick = onStartNewSessionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("start_new_session_button")
        ) {
            Text(
                text = "Start new session",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        SectionLabel(text = "History")

        if (sessions.isEmpty()) {
            EmptyStateCard(
                title = "No sessions yet",
                description = "Start a session and save a few deliveries to see them here."
            )
        } else {
            sessions.forEach { session ->
                val sessionDeliveries = deliveries.filter { it.sessionId == session.id }
                val player = players.find { it.id == session.playerId }
                val stats = calculateSessionStats(sessionDeliveries)

                CardListItem(
                    title = session.name,
                    subtitle = "${player?.name ?: "—"} · ${sessionDeliveries.size} deliveries · ${formatTimeAgo(session.createdAt)}",
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
