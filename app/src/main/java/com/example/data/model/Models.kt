package com.example.data.model

data class Player(
    val id: String,
    val name: String,
    val hand: String, // "Right-handed", "Left-handed"
    val style: String, // "Fast", "Medium", "Spin"
    val team: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Session(
    val id: String,
    val name: String,
    val playerId: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Delivery(
    val id: String,
    val sessionId: String,
    val kmh: Double,
    val mph: Double,
    val flightTime: Double,
    val distance: Double,
    val fps: Double,
    val confidence: String, // "high", "medium", "low"
    val cameraAngle: String,
    val hasBounce: Boolean = false,
    val releaseToBounceTime: Double? = null,
    val bounceToArrivalTime: Double? = null,
    val bounceDistance: Double? = null,
    val stumpsDistance: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SessionWithDeliveries(
    val session: Session,
    val playerName: String?,
    val deliveries: List<Delivery>
)

data class SessionStats(
    val fastest: Double?,
    val slowest: Double?,
    val avg: Double?,
    val median: Double?,
    val valid: Int,
    val low: Int,
    val consistency: Double?
)

data class CoachInsights(
    val summary: String,
    val observations: List<String>,
    val drills: List<String>
)

fun calculateSessionStats(deliveries: List<Delivery>): SessionStats {
    val speeds = deliveries.map { it.kmh }.filter { it > 0.0 }
    if (speeds.isEmpty()) {
        return SessionStats(
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
    val fastest = sorted.maxOrNull()
    val slowest = sorted.minOrNull()
    val avg = speeds.average()
    val median = if (sorted.size % 2 == 1) {
        sorted[sorted.size / 2]
    } else {
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    }
    val variance = speeds.map { (it - avg) * (it - avg) }.average()
    val sd = Math.sqrt(variance)
    val consistency = if (avg > 0) {
        Math.max(0.0, 100.0 - (sd / avg * 100.0))
    } else 0.0

    val lowCount = deliveries.count { it.confidence.equals("low", ignoreCase = true) }
    return SessionStats(
        fastest = fastest,
        slowest = slowest,
        avg = avg,
        median = median,
        valid = speeds.size,
        low = lowCount,
        consistency = consistency
    )
}

fun generateCoachInsights(deliveries: List<Delivery>): CoachInsights {
    val valid = deliveries.filter { it.kmh > 0.0 }
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
    val sd = Math.sqrt(variance)
    val consistency = if (avg > 0) Math.max(0.0, 100.0 - (sd / avg * 100.0)) else 0.0

    val lowCount = valid.count { it.confidence.equals("low", ignoreCase = true) }
    val lowRatio = lowCount.toDouble() / valid.size.toDouble()

    val half = Math.max(1, speeds.size / 2)
    val firstHalf = speeds.take(half)
    val secondHalf = speeds.takeLast(half)
    val firstAvg = firstHalf.average()
    val secondAvg = secondHalf.average()
    val drop = firstAvg - secondAvg

    val summary = "Recorded ${valid.size} deliveries ranging from ${String.format("%.1f", min)}–${String.format("%.1f", max)} km/h, averaging ${String.format("%.1f", avg)} km/h at ${String.format("%.0f", consistency)}% consistency."

    val observations = mutableListOf<String>()
    val drills = mutableListOf<String>()

    if (Math.abs(drop) >= 2.0) {
        if (drop > 0) {
            observations.add("Your final deliveries averaged ${String.format("%.1f", drop)} km/h slower than your first ones — a fatigue pattern across the session.")
            drills.add("Fatigue management — split long spells into shorter high-intensity sets with full recovery, and re-check pace in the back half of practice.")
        } else {
            observations.add("Your later deliveries were ${String.format("%.1f", Math.abs(drop))} km/h faster on average than your first ones — pace built through the session.")
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
        observations.add("Consistency is lower than typical (${String.format("%.0f", consistency)}%) — speed varied noticeably between deliveries.")
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
