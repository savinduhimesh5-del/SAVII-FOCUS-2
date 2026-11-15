package com.savii.focus.model

data class ActiveFocusTimer(
    val startedAtMillis: Long,
    val durationSeconds: Int,
    val subjectId: Long? = null
)
