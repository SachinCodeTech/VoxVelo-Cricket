package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@Composable
fun VoxVeloTopBar(
    activePlayerName: String?,
    onPlayerChipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(DarkBg)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand logo & title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("brand_header")
        ) {
            // Brand mark: 30x30 rounded rect with red background & center ring
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(AccentRed),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF2EE))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "VoxVelo",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = "Measure. Analyze. Improve.",
                    fontSize = 10.5.sp,
                    color = TextFaint
                )
            }
        }

        // Player chip on top right
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(DarkSurface)
                .border(1.dp, DarkLineStrong, RoundedCornerShape(100.dp))
                .clickable { onPlayerChipClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("player_chip_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentTeal)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = activePlayerName ?: "No player",
                fontSize = 12.sp,
                color = TextDim,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
