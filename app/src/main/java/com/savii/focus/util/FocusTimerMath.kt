package com.savii.focus.util

import java.time.Instant
import java.time.ZoneId

object FocusTimerMath {
    val presetsSeconds = listOf(15, 25, 45, 60).map { it * 60 }

    fun remainingSeconds(durationSeconds: Int, startedAtMillis: Long, nowMillis: Long): Int {
        if (durationSeconds <= 0) return 0
        val totalMillis = durationSeconds * 1_000L
        val elapsedMillis = (nowMillis - startedAtMillis).coerceAtLeast(0L)
        val remainingMillis = (totalMillis - elapsedMillis).coerceAtLeast(0L)
        return ((remainingMillis + 999L) / 1_000L).toInt()
    }

    fun progress(durationSeconds: Int, startedAtMillis: Long, nowMillis: Long): Float {
        if (durationSeconds <= 0) return 1f
        val totalMillis = durationSeconds * 1_000L
        val elapsedMillis = (nowMillis - startedAtMillis).coerceIn(0L, totalMillis)
        return elapsedMillis.toFloat() / totalMillis.toFloat()
    }

    fun startOfTodayMillis(nowMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    /** Start of the local calendar day six days before today (seven dates including today). */
    fun startOfLastSevenDaysMillis(nowMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()
            .minusDays(6)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }
}
