package com.example

import com.example.data.model.Delivery
import com.example.data.model.calculateSessionStats
import com.example.data.model.generateCoachInsights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testSpeedCalculation() {
    // 20.12m in 0.55s => mps = 20.12/0.55 = 36.5818 m/s => km/h = 131.69 km/h
    val dist = 20.12
    val time = 0.55
    val mps = dist / time
    val kmh = mps * 3.6
    val mph = kmh * 0.621371

    assertTrue(kmh > 130.0 && kmh < 133.0)
    assertTrue(mph > 80.0 && mph < 83.0)
  }

  @Test
  fun testSessionStatsAndConsistency() {
    val deliveries = listOf(
      createMockDelivery(130.0),
      createMockDelivery(132.0),
      createMockDelivery(131.0),
      createMockDelivery(129.0)
    )

    val stats = calculateSessionStats(deliveries)
    assertEquals(132.0, stats.fastest ?: 0.0, 0.01)
    assertEquals(129.0, stats.slowest ?: 0.0, 0.01)
    assertEquals(130.5, stats.avg ?: 0.0, 0.01)
    assertTrue((stats.consistency ?: 0.0) > 95.0)
  }

  @Test
  fun testCoachInsightsFatigueDetection() {
    // First half average 140, second half average 130 -> 10 km/h drop (fatigue)
    val deliveries = listOf(
      createMockDelivery(140.0),
      createMockDelivery(140.0),
      createMockDelivery(130.0),
      createMockDelivery(130.0)
    )

    val insights = generateCoachInsights(deliveries)
    assertNotNull(insights.summary)
    assertTrue(insights.observations.any { it.contains("slower than your first ones") })
    assertTrue(insights.drills.any { it.contains("Fatigue management") })
  }

  private fun createMockDelivery(kmh: Double, confidence: String = "high"): Delivery {
    return Delivery(
      id = "mock_" + System.nanoTime(),
      sessionId = "mock_session",
      kmh = kmh,
      mph = kmh * 0.621371,
      flightTime = 20.12 / (kmh / 3.6),
      distance = 20.12,
      fps = 60.0,
      confidence = confidence,
      cameraAngle = "side"
    )
  }
}

