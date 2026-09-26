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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Delivery
import com.example.data.model.Player
import com.example.data.model.Session
import com.example.data.model.SessionStats
import com.example.data.model.calculateSessionStats
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurface2
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    players: List<Player>,
    sessions: List<Session>,
    deliveries: List<Delivery>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var compareMode by remember { mutableStateOf("players") } // "players" or "sessions"

    var selectedPlayerAId by remember(players) { mutableStateOf(players.getOrNull(0)?.id ?: "") }
    var selectedPlayerBId by remember(players) { mutableStateOf(players.getOrNull(1)?.id ?: players.getOrNull(0)?.id ?: "") }

    var selectedSessionAId by remember(sessions) { mutableStateOf(sessions.getOrNull(0)?.id ?: "") }
    var selectedSessionBId by remember(sessions) { mutableStateOf(sessions.getOrNull(1)?.id ?: sessions.getOrNull(0)?.id ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("compare_back_button")
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
            text = "Compare",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Player vs player, or session vs session.",
            fontSize = 13.5.sp,
            color = TextDim,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Segmented toggle: Players vs Sessions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, DarkLineStrong, RoundedCornerShape(10.dp))
                .background(DarkSurface2)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (compareMode == "players") AccentTeal else DarkSurface2)
                    .clickable { compareMode = "players" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Players",
                    fontSize = 12.5.sp,
                    fontWeight = if (compareMode == "players") FontWeight.Bold else FontWeight.Normal,
                    color = if (compareMode == "players") com.example.ui.theme.DarkBg else TextDim
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (compareMode == "sessions") AccentTeal else DarkSurface2)
                    .clickable { compareMode = "sessions" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sessions",
                    fontSize = 12.5.sp,
                    fontWeight = if (compareMode == "sessions") FontWeight.Bold else FontWeight.Normal,
                    color = if (compareMode == "sessions") com.example.ui.theme.DarkBg else TextDim
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selectors for A and B
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (compareMode == "players") {
                Column(modifier = Modifier.weight(1f)) {
                    Text("A", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 4.dp))
                    PlayerDropdown(
                        selectedId = selectedPlayerAId,
                        players = players,
                        onSelect = { selectedPlayerAId = it }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("B", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 4.dp))
                    PlayerDropdown(
                        selectedId = selectedPlayerBId,
                        players = players,
                        onSelect = { selectedPlayerBId = it }
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text("A", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 4.dp))
                    SessionDropdown(
                        selectedId = selectedSessionAId,
                        sessions = sessions,
                        onSelect = { selectedSessionAId = it }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("B", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 4.dp))
                    SessionDropdown(
                        selectedId = selectedSessionBId,
                        sessions = sessions,
                        onSelect = { selectedSessionBId = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Render Compare Results
        val (nameA, statsA) = if (compareMode == "players") {
            val p = players.find { it.id == selectedPlayerAId }
            val pSessions = sessions.filter { it.playerId == selectedPlayerAId }
            val pDeliveries = deliveries.filter { d -> pSessions.any { it.id == d.sessionId } }
            Pair(p?.name ?: "—", calculateSessionStats(pDeliveries))
        } else {
            val s = sessions.find { it.id == selectedSessionAId }
            val sDeliveries = deliveries.filter { it.sessionId == selectedSessionAId }
            Pair(s?.name ?: "—", calculateSessionStats(sDeliveries))
        }

        val (nameB, statsB) = if (compareMode == "players") {
            val p = players.find { it.id == selectedPlayerBId }
            val pSessions = sessions.filter { it.playerId == selectedPlayerBId }
            val pDeliveries = deliveries.filter { d -> pSessions.any { it.id == d.sessionId } }
            Pair(p?.name ?: "—", calculateSessionStats(pDeliveries))
        } else {
            val s = sessions.find { it.id == selectedSessionBId }
            val sDeliveries = deliveries.filter { it.sessionId == selectedSessionBId }
            Pair(s?.name ?: "—", calculateSessionStats(sDeliveries))
        }

        val itemCount = if (compareMode == "players") players.size else sessions.size
        if (itemCount < 2) {
            EmptyStateCard(
                title = "Not enough items to compare",
                description = "Add at least two $compareMode with saved deliveries to compare."
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .padding(14.dp)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nameA,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "METRIC",
                            fontSize = 10.5.sp,
                            color = TextFaint,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.width(70.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = nameB,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }

                    HorizontalDivider(color = DarkLine, thickness = 1.dp)

                    // Metric Rows
                    CompareMetricRow(
                        label = "Fastest",
                        valA = statsA.fastest,
                        valB = statsB.fastest,
                        unit = "km/h"
                    )
                    HorizontalDivider(color = DarkLine, thickness = 1.dp)

                    CompareMetricRow(
                        label = "Average",
                        valA = statsA.avg,
                        valB = statsB.avg,
                        unit = "km/h"
                    )
                    HorizontalDivider(color = DarkLine, thickness = 1.dp)

                    CompareMetricRow(
                        label = "Consistency",
                        valA = statsA.consistency,
                        valB = statsB.consistency,
                        unit = "%",
                        formatInt = true
                    )
                    HorizontalDivider(color = DarkLine, thickness = 1.dp)

                    CompareMetricRow(
                        label = "Deliveries",
                        valA = statsA.valid.toDouble(),
                        valB = statsB.valid.toDouble(),
                        unit = "",
                        formatInt = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun CompareMetricRow(
    label: String,
    valA: Double?,
    valB: Double?,
    unit: String,
    formatInt: Boolean = false
) {
    val aWin = valA != null && valB != null && valA > valB
    val bWin = valA != null && valB != null && valB > valA

    val strA = when {
        valA == null -> "—"
        formatInt -> "${valA.toInt()}${if (unit.isNotEmpty()) " $unit" else ""}"
        else -> "${String.format("%.1f", valA)}${if (unit.isNotEmpty()) " $unit" else ""}"
    }

    val strB = when {
        valB == null -> "—"
        formatInt -> "${valB.toInt()}${if (unit.isNotEmpty()) " $unit" else ""}"
        else -> "${String.format("%.1f", valB)}${if (unit.isNotEmpty()) " $unit" else ""}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strA,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.5.sp,
            fontWeight = if (aWin) FontWeight.Bold else FontWeight.Normal,
            color = if (aWin) AccentTeal else TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextFaint,
            modifier = Modifier.width(70.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = strB,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.5.sp,
            fontWeight = if (bWin) FontWeight.Bold else FontWeight.Normal,
            color = if (bWin) AccentTeal else TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDropdown(
    selectedId: String,
    players: List<Player>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = players.find { it.id == selectedId }?.name ?: "Select"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = current,
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurface2)
        ) {
            players.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name, color = TextPrimary, fontSize = 13.sp) },
                    onClick = {
                        onSelect(p.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDropdown(
    selectedId: String,
    sessions: List<Session>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = sessions.find { it.id == selectedId }?.name ?: "Select"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = current,
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurface2)
        ) {
            sessions.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.name, color = TextPrimary, fontSize = 13.sp) },
                    onClick = {
                        onSelect(s.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
