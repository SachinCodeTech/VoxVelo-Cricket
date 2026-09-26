package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBgRaised
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

enum class NavScreen(val label: String, val testTag: String) {
    HOME("Home", "nav_home"),
    SESSIONS("Sessions", "nav_sessions"),
    ANALYZE("Analyze", "nav_analyze"),
    PLAYERS("Players", "nav_players"),
    MORE("More", "nav_more")
}

@Composable
fun VoxVeloBottomNav(
    currentScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBgRaised)
            .border(width = 1.dp, color = DarkLineStrong)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavScreen.entries.forEach { screen ->
                val isSelected = screen == currentScreen
                val icon: ImageVector = when (screen) {
                    NavScreen.HOME -> Icons.Outlined.Home
                    NavScreen.SESSIONS -> Icons.Outlined.FormatListBulleted
                    NavScreen.ANALYZE -> if (isSelected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
                    NavScreen.PLAYERS -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                    NavScreen.MORE -> if (isSelected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz
                }

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false, radius = 24.dp),
                            onClick = { onScreenSelected(screen) }
                        )
                        .padding(vertical = 4.dp)
                        .testTag(screen.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.label,
                        tint = if (isSelected) AccentRed else TextFaint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = screen.label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextFaint,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
