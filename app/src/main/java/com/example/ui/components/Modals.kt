package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Player
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkBgRaised
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurface2
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, hand: String, style: String, team: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hand by remember { mutableStateOf("Right-handed") }
    var style by remember { mutableStateOf("Fast") }
    var team by remember { mutableStateOf("") }

    var handExpanded by remember { mutableStateOf(false) }
    var styleExpanded by remember { mutableStateOf(false) }

    val handOptions = listOf("Right-handed", "Left-handed")
    val styleOptions = listOf("Fast", "Medium", "Spin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add player",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Name
                Text("Name", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Player name", color = TextFaint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2,
                        unfocusedContainerColor = DarkSurface2,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = DarkLineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_player_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Handedness and Style side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Handedness
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Handedness", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                        ExposedDropdownMenuBox(
                            expanded = handExpanded,
                            onExpandedChange = { handExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = hand,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = handExpanded) },
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
                                expanded = handExpanded,
                                onDismissRequest = { handExpanded = false },
                                modifier = Modifier.background(DarkSurface2)
                            ) {
                                handOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, color = TextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            hand = opt
                                            handExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Style
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Style", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                        ExposedDropdownMenuBox(
                            expanded = styleExpanded,
                            onExpandedChange = { styleExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = style,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded) },
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
                                expanded = styleExpanded,
                                onDismissRequest = { styleExpanded = false },
                                modifier = Modifier.background(DarkSurface2)
                            ) {
                                styleOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, color = TextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            style = opt
                                            styleExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Team / Academy
                Text("Team / Academy (optional)", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = team,
                    onValueChange = { team = it },
                    placeholder = { Text("Optional", color = TextFaint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2,
                        unfocusedContainerColor = DarkSurface2,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = DarkLineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_player_team_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), hand, style, team.trim())
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_player_confirm_button")
            ) {
                Text("Save player", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextDim)
            }
        },
        containerColor = DarkBgRaised,
        shape = RoundedCornerShape(18.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsDialog(
    players: List<Player>,
    activePlayerId: String?,
    isSavingDelivery: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (sessionName: String, playerId: String) -> Unit
) {
    val dateStr = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date()) }
    var sessionName by remember { mutableStateOf("Session $dateStr") }
    var selectedPlayerId by remember(players, activePlayerId) {
        mutableStateOf(activePlayerId ?: players.firstOrNull()?.id ?: "")
    }
    var playerExpanded by remember { mutableStateOf(false) }

    val selectedPlayerName = players.find { it.id == selectedPlayerId }?.name ?: "Select Player"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Session details",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Session name", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    placeholder = { Text("e.g. Morning Nets", color = TextFaint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2,
                        unfocusedContainerColor = DarkSurface2,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = DarkLineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("session_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Player", fontSize = 12.sp, color = TextDim, modifier = Modifier.padding(bottom = 6.dp))
                ExposedDropdownMenuBox(
                    expanded = playerExpanded,
                    onExpandedChange = { playerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPlayerName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = playerExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface2,
                            unfocusedContainerColor = DarkSurface2,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = DarkLineStrong,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("session_player_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = playerExpanded,
                        onDismissRequest = { playerExpanded = false },
                        modifier = Modifier.background(DarkSurface2)
                    ) {
                        players.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name, color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    selectedPlayerId = p.id
                                    playerExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sessionName.isNotBlank() && selectedPlayerId.isNotBlank()) {
                        onConfirm(sessionName.trim(), selectedPlayerId)
                    }
                },
                enabled = sessionName.isNotBlank() && selectedPlayerId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_session_button")
            ) {
                Text(
                    text = if (isSavingDelivery) "Save delivery" else "Create session",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextDim)
            }
        },
        containerColor = DarkBgRaised,
        shape = RoundedCornerShape(18.dp)
    )
}
