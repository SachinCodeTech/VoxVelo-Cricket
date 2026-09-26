package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.data.CoachInsights
import com.example.ui.theme.*

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 20.dp, bottom = 10.dp)
  ) {
    Text(
      text = text,
      color = VeloTextFaint,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(VeloLineStrong)
    )
  }
}

@Composable
fun StatStrip(
  rows: List<Pair<String, String>>,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, VeloLineStrong, RoundedCornerShape(16.dp))
      .background(VeloSurface)
  ) {
    rows.forEachIndexed { index, (key, value) ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = key, color = VeloTextDim, fontSize = 13.sp)
        Text(
          text = value,
          color = VeloText,
          fontSize = 16.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium
        )
      }
      if (index < rows.size - 1) {
        HorizontalDivider(color = VeloLineStrong.copy(alpha = 0.5f), thickness = 1.dp)
      }
    }
  }
}

@Composable
fun CardListItem(
  title: String,
  subtitle: String,
  value: String,
  valueSub: String? = null,
  activeBadge: Boolean = false,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 10.dp)
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, VeloLineStrong, RoundedCornerShape(14.dp))
      .background(VeloSurface)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          color = VeloText,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp
        )
        if (activeBadge) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "·", color = VeloTextFaint, fontSize = 12.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "active",
            color = VeloTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        color = VeloTextFaint,
        fontSize = 12.sp
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = value,
        color = VeloText,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
      )
      if (valueSub != null) {
        Text(
          text = valueSub,
          color = VeloTextFaint,
          fontSize = 10.5.sp
        )
      }
    }
  }
}

@Composable
fun EmptyState(
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .border(1.dp, VeloLineStrong, RoundedCornerShape(14.dp))
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = title,
      color = VeloTextDim,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = description,
      color = VeloTextFaint,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
  }
}

@Composable
fun ConfidenceChip(confidence: String) {
  val (bgColor, textColor) = when (confidence.lowercase()) {
    "high" -> Color(0xFF123B31) to Color(0xFF5EEAC4)
    "medium" -> Color(0xFF3B3312) to VeloAmber
    else -> Color(0xFF3B1414) to Color(0xFFF28B82)
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(CircleShape)
      .background(bgColor)
      .padding(horizontal = 10.dp, vertical = 5.dp)
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(textColor)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = "Confidence: ${confidence.uppercase()}",
      color = textColor,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun CoachBox(coach: CoachInsights, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, VeloLineStrong, RoundedCornerShape(14.dp))
      .background(VeloSurface)
      .padding(16.dp)
  ) {
    Text(
      text = coach.summary,
      color = VeloText,
      fontSize = 13.5.sp,
      lineHeight = 20.sp,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    coach.observations.forEach { obs ->
      HorizontalDivider(color = VeloLineStrong.copy(alpha = 0.5f), thickness = 1.dp)
      Row(
        modifier = Modifier.padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .padding(top = 6.dp, end = 8.dp)
            .size(5.dp)
            .clip(CircleShape)
            .background(VeloTeal)
        )
        Text(
          text = obs,
          color = VeloTextDim,
          fontSize = 12.5.sp,
          lineHeight = 18.sp
        )
      }
    }

    if (coach.drills.isNotEmpty()) {
      HorizontalDivider(color = VeloLineStrong, thickness = 1.dp, modifier = Modifier.padding(top = 6.dp))
      Text(
        text = "SUGGESTED DRILLS",
        color = VeloTextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
      )
      coach.drills.forEach { drill ->
        Text(
          text = "• $drill",
          color = VeloTextDim,
          fontSize = 12.5.sp,
          lineHeight = 18.sp,
          modifier = Modifier.padding(vertical = 4.dp)
        )
      }
    }
  }
}
