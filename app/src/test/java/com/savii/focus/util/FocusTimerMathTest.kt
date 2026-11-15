package com.savii.focus.util

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimerMathTest {
    @Test
    fun remainingTimeRoundsUpToWholeSecond() {
        assertEquals(60, FocusTimerMath.remainingSeconds(60, 1_000L, 1_001L))
        assertEquals(59, FocusTimerMath.remainingSeconds(60, 1_000L, 2_000L))
    }

    @Test
    fun remainingTimeNeverBecomesNegative() {
        assertEquals(0, FocusTimerMath.remainingSeconds(60, 1_000L, 90_000L))
    }

    @Test
    fun clockMovingBackwardsDoesNotCreateExtraElapsedTime() {
        assertEquals(60, FocusTimerMath.remainingSeconds(60, 2_000L, 1_000L))
    }

    @Test
    fun progressIsClampedToZeroAndOne() {
        assertEquals(0f, FocusTimerMath.progress(60, 2_000L, 1_000L), 0.0001f)
        assertEquals(1f, FocusTimerMath.progress(60, 2_000L, 100_000L), 0.0001f)
    }

    @Test
    fun startOfDayUsesTheSuppliedTimeZone() {
        val zone = ZoneId.of("Asia/Colombo")
        val now = Instant.parse("2026-11-15T09:00:00Z").toEpochMilli()
        val actual = Instant.ofEpochMilli(FocusTimerMath.startOfTodayMillis(now, zone))
            .atZone(zone)
        assertEquals(0, actual.hour)
        assertEquals(0, actual.minute)
        assertEquals(15, actual.dayOfMonth)
    }

    @Test
    fun lastSevenDaysStartsAtTheLocalDaySixDaysEarlier() {
        val zone = ZoneId.of("Asia/Colombo")
        val now = Instant.parse("2026-11-15T09:00:00Z").toEpochMilli()
        val actual = Instant.ofEpochMilli(FocusTimerMath.startOfLastSevenDaysMillis(now, zone))
            .atZone(zone)
        assertEquals(9, actual.dayOfMonth)
        assertEquals(0, actual.hour)
        assertEquals(0, actual.minute)
    }
}
