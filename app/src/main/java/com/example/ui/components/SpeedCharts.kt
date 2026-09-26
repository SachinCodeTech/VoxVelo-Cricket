package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoachInsights
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkLine
import com.example.ui.theme.DarkLineStrong
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextPrimary

@Composable
fun DeliveryLineChart(
    speeds: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = AccentTeal,
    dotColor: Color = AccentRed
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (speeds.isEmpty()) {
            Text("No data yet", fontSize = 11.sp, color = TextFaint)
        } else if (speeds.size == 1) {
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val cy = size.height / 2f
                val cx = size.width / 2f
                drawCircle(color = dotColor, radius = 6f, center = Offset(cx, cy))
            }
        } else {
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val pad = 12f
                val w = size.width
                val h = size.height
                val min = speeds.minOrNull() ?: 0.0
                val max = speeds.maxOrNull() ?: 0.0
                val range = if (max == min) 1.0 else max - min

                val points = speeds.mapIndexed { index, speed ->
                    val x = pad + (index.toFloat() / (speeds.size - 1)) * (w - pad * 2)
                    val y = (h - pad) - ((speed - min) / range).toFloat() * (h - pad * 2)
                    Offset(x, y)
                }

                val path = Path()
                points.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.5f)
                )

                points.forEach { pt ->
                    drawCircle(color = dotColor, radius = 5.5f, center = pt)
                }
            }
        }
    }
}

@Composable
fun SpeedHistogram(
    speeds: List<Double>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (speeds.size < 2) {
            Text("Needs 2+ deliveries", fontSize = 9.sp, color = TextFaint)
        } else {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val pad = 8f
                val w = size.width
                val h = size.height
                val min = speeds.minOrNull() ?: 0.0
                val max = speeds.maxOrNull() ?: 0.0
                val range = if (max == min) 1.0 else max - min

                val bucketCount = 5
                val buckets = IntArray(bucketCount)
                speeds.forEach { v ->
                    var idx = ((v - min) / range * bucketCount).toInt()
                    if (idx >= bucketCount) idx = bucketCount - 1
                    if (idx < 0) idx = 0
                    buckets[idx]++
                }

                val maxCount = buckets.maxOrNull() ?: 1
                val bw = (w - pad * 2) / bucketCount

                drawLine(
                    color = DarkLineStrong,
                    start = Offset(pad, h - pad),
                    end = Offset(w - pad, h - pad),
                    strokeWidth = 2f
                )

                buckets.forEachIndexed { i, c ->
                    val barHeight = if (maxCount > 0) (c.toFloat() / maxCount.toFloat()) * (h - pad * 2 - 10f) else 0f
                    val x = pad + i * bw + 2f
                    val y = (h - pad) - barHeight
                    drawRect(
                        color = AccentTeal,
                        topLeft = Offset(x, y),
                        size = Size(bw - 4f, barHeight)
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressFatigueChart(
    speeds: List<Double>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (speeds.isEmpty()) {
            Text("No data yet", fontSize = 9.sp, color = TextFaint)
        } else {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val pad = 8f
                val w = size.width
                val h = size.height

                val cumAvgs = mutableListOf<Double>()
                var sum = 0.0
                speeds.forEachIndexed { i, v ->
                    sum += v
                    cumAvgs.add(sum / (i + 1))
                }

                val min = cumAvgs.minOrNull() ?: 0.0
                val max = cumAvgs.maxOrNull() ?: 0.0
                val range = if (max == min) 1.0 else max - min

                val points = cumAvgs.mapIndexed { index, avg ->
                    val denom = if (cumAvgs.size > 1) (cumAvgs.size - 1) else 1
                    val x = pad + (index.toFloat() / denom) * (w - pad * 2)
                    val y = (h - pad) - ((avg - min) / range).toFloat() * (h - pad * 2)
                    Offset(x, y)
                }

                val path = Path()
                points.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = AccentAmber,
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }
}

@Composable
fun PitchOverlayView(
    bounceDistance: Double?,
    totalDistance: Double = 20.12,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "Pitch overlay — release / bounce / arrival, to scale on measured distance",
                fontSize = 11.sp,
                color = TextFaint,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                val pad = 36f
                val w = size.width
                val h = size.height
                val y = h / 2f

                val releaseX = pad
                val arrivalX = w - pad
                val frac = if (bounceDistance != null && bounceDistance > 0 && totalDistance > 0) {
                    Math.max(0.0, Math.min(1.0, bounceDistance / totalDistance)).toFloat()
                } else null

                val bounceX = if (frac != null) releaseX + frac * (arrivalX - releaseX) else null

                // Base pitch line
                drawLine(
                    color = DarkLineStrong,
                    start = Offset(releaseX, y),
                    end = Offset(arrivalX, y),
                    strokeWidth = 3f
                )

                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                if (bounceX != null) {
                    // Arc from release to bounce
                    val arc1 = Path().apply {
                        moveTo(releaseX, y)
                        quadraticTo((releaseX + bounceX) / 2f, y - 36f, bounceX, y)
                    }
                    drawPath(
                        path = arc1,
                        color = AccentAmber,
                        style = Stroke(width = 3.5f, pathEffect = dashedEffect)
                    )

                    // Arc from bounce to arrival
                    val arc2 = Path().apply {
                        moveTo(bounceX, y)
                        quadraticTo((bounceX + arrivalX) / 2f, y - 24f, arrivalX, y)
                    }
                    drawPath(
                        path = arc2,
                        color = AccentRed,
                        style = Stroke(width = 3.5f, pathEffect = dashedEffect)
                    )

                    // Bounce circle
                    drawCircle(color = AccentAmber, radius = 9f, center = Offset(bounceX, y))
                } else {
                    // Full arc from release to arrival
                    val fullArc = Path().apply {
                        moveTo(releaseX, y)
                        quadraticTo((releaseX + arrivalX) / 2f, y - 36f, arrivalX, y)
                    }
                    drawPath(
                        path = fullArc,
                        color = AccentRed,
                        style = Stroke(width = 3.5f, pathEffect = dashedEffect)
                    )
                }

                // Release circle
                drawCircle(color = AccentTeal, radius = 9f, center = Offset(releaseX, y))

                // Arrival circle
                drawCircle(color = AccentRed, radius = 9f, center = Offset(arrivalX, y))
            }

            // Legend labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentTeal))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Release", fontSize = 10.sp, color = TextDim)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (bounceDistance != null && bounceDistance > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentAmber))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bounce (${String.format("%.1f", bounceDistance)}m)", fontSize = 10.sp, color = TextDim)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Arrival (${String.format("%.1f", totalDistance)}m)", fontSize = 10.sp, color = TextDim)
                }
            }
        }
    }
}

@Composable
fun CoachInsightsBox(
    insights: CoachInsights,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkLineStrong, RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Text(
            text = insights.summary,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = TextPrimary
        )

        if (insights.observations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            insights.observations.forEach { obs ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = obs,
                        fontSize = 12.5.sp,
                        color = TextDim,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (insights.drills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DarkLineStrong)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SUGGESTED DRILLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextFaint,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            insights.drills.forEach { drill ->
                Text(
                    text = "• $drill",
                    fontSize = 12.5.sp,
                    color = TextDim,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}
