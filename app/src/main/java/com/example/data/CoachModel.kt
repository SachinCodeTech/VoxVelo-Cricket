package com.example.data

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class CoachInsights(
  val summary: String,
  val observations: List<String>,
  val drills: List<String>
)

data class SessionCalculatedStats(
  val fastest: Double?,
  val slowest: Double?,
  val avg: Double?,
  val median: Double?,
  val valid: Int,
  val low: Int,
  val consistency: Double?
)

object CoachModel {
  fun generateCoachInsights(deliveries: List<DeliveryEntity>): CoachInsights {
    val valid = deliveries.filter { it.kmh > 0 }
    if (valid.size < 3) {
      return CoachInsights(
        summary = "Record at least 3 deliveries in this session for the coach to analyze pace and consistency.",
        observations = emptyList(),
        drills = emptyList()
      )
    }

    val speeds = valid.map { it.kmh }
    val min = speeds.minOrNull() ?: 0.0
    val max = speeds.maxOrNull() ?: 0.0
    val avg = speeds.average()
    val variance = speeds.map { (it - avg) * (it - avg) }.average()
    val sd = sqrt(variance)
    val consistency = if (avg > 0) (100.0 - (sd / avg * 100.0)).coerceAtLeast(0.0) else 0.0
    val lowCount = valid.count { it.confidence.equals("low", ignoreCase = true) }
    val lowRatio = lowCount.toDouble() / valid.size

    val half = valid.size / 2
    val firstHalf = speeds.take(if (half > 0) half else 1)
    val secondHalf = speeds.takeLast(if (half > 0) half else 1)
    val firstAvg = firstHalf.average()
    val secondAvg = secondHalf.average()
    val drop = firstAvg - secondAvg

    val summary = "Recorded ${valid.size} deliveries ranging from ${fmt(min, 1)}–${fmt(max, 1)} km/h, averaging ${fmt(avg, 1)} km/h at ${consistency.roundToInt()}% consistency."

    val observations = mutableListOf<String>()
    val drills = mutableListOf<String>()

    if (abs(drop) >= 2.0) {
      if (drop > 0) {
        observations.add("Your final deliveries averaged ${fmt(drop, 1)} km/h slower than your first ones — a fatigue pattern across the session.")
        drills.add("Fatigue management — split long spells into shorter high-intensity sets with full recovery, and re-check pace in the back half of practice.")
      } else {
        observations.add("Your later deliveries were ${fmt(abs(drop), 1)} km/h faster on average than your first ones — pace built through the session.")
      }
    } else {
      observations.add("Pace held steady from the start of the session to the end — no meaningful fatigue drop detected.")
    }

    if (consistency >= 90.0) {
      observations.add("Delivery-to-delivery consistency is excellent, within a tight speed band.")
    } else if (consistency >= 75.0) {
      observations.add("Consistency is solid, with some natural variation delivery to delivery.")
      drills.add("Run-up consistency — bowl to a fixed run-up mark and focus on repeating release timing each delivery.")
    } else {
      observations.add("Consistency is lower than typical (${consistency.roundToInt()}%) — speed varied noticeably between deliveries.")
      drills.add("Run-up consistency — bowl to a fixed run-up mark and focus on repeating release timing each delivery.")
      drills.add("Rhythm drill — bowl 6-ball overs at 70% effort focusing purely on a repeatable action before building back to full pace.")
    }

    if (lowRatio > 0.2) {
      observations.add("$lowCount of ${valid.size} deliveries were low-confidence readings — camera setup is limiting measurement reliability.")
      drills.add("Recording setup — use a side-on angle, 60fps or higher, even lighting and a stable phone position for more reliable readings.")
    }

    if (drills.isEmpty()) {
      drills.add("No corrective drills flagged from this data — maintain the current run-up and recording setup.")
    }

    return CoachInsights(summary, observations, drills)
  }

  fun calculateStats(deliveries: List<DeliveryEntity>): SessionCalculatedStats {
    val speeds = deliveries.map { it.kmh }.filter { it > 0 }
    if (speeds.isEmpty()) {
      return SessionCalculatedStats(
        fastest = null,
        slowest = null,
        avg = null,
        median = null,
        valid = 0,
        low = 0,
        consistency = null
      )
    }

    val sorted = speeds.sorted()
    val avg = speeds.average()
    val median = if (sorted.size % 2 == 1) {
      sorted[sorted.size / 2]
    } else {
      (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    }
    val variance = speeds.map { (it - avg) * (it - avg) }.average()
    val sd = sqrt(variance)
    val consistency = if (avg > 0) (100.0 - (sd / avg * 100.0)).coerceAtLeast(0.0) else 0.0

    return SessionCalculatedStats(
      fastest = speeds.maxOrNull(),
      slowest = speeds.minOrNull(),
      avg = avg,
      median = median,
      valid = speeds.size,
      low = deliveries.count { it.confidence.equals("low", ignoreCase = true) },
      consistency = consistency
    )
  }

  fun fmt(n: Double?, decimals: Int = 1): String {
    if (n == null || n.isNaN()) return "—"
    return "%.${decimals}f".format(n)
  }

  fun timeAgo(timestamp: Long): String {
    val sec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
      sec < 3600 -> "${(sec / 60).coerceAtLeast(1)}m ago"
      sec < 86400 -> "${sec / 3600}h ago"
      else -> "${sec / 86400}d ago"
    }
  }
}
