package com.savii.focus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savii.focus.model.ActiveFocusTimer
import com.savii.focus.util.FocusTimerMath

@Composable
fun FocusScreen(state: FocusUiState, viewModel: FocusViewModel) {
    var selectedDuration by rememberSaveable { mutableStateOf(25 * 60) }
    var selectedSubjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    val timer = state.activeTimer
    val remaining = timer?.let {
        FocusTimerMath.remainingSeconds(it.durationSeconds, it.startedAtMillis, state.nowMillis)
    } ?: selectedDuration
    val progress = timer?.let {
        FocusTimerMath.progress(it.durationSeconds, it.startedAtMillis, state.nowMillis)
    } ?: 0f
    val selectedSubject = state.subjects.firstOrNull { it.id == selectedSubjectId }
    val selectedSubjectName = selectedSubject?.name ?: "No subject"

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Protect a little time for one thing.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (timer == null) "READY WHEN YOU ARE" else "FOCUS SESSION", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(15.dp))
                Text(
                    text = formatTimer(remaining),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(15.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(7.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (timer == null) "Choose a session length" else "Stay with it. You are doing well.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        if (timer == null) {
            Text("SESSION LENGTH", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 60).forEach { minutes ->
                    FilterChip(
                        selected = selectedDuration == minutes * 60,
                        onClick = { selectedDuration = minutes * 60 },
                        label = { Text("${minutes}m") }
                    )
                }
            }
            Box {
                OutlinedButton(onClick = { subjectMenuExpanded = true }, shape = RoundedCornerShape(14.dp)) {
                    Text("SUBJECT: ${selectedSubjectName.uppercase()}")
                }
                DropdownMenu(expanded = subjectMenuExpanded, onDismissRequest = { subjectMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("No subject") }, onClick = { selectedSubjectId = null; subjectMenuExpanded = false })
                    state.subjects.forEach { subject ->
                        DropdownMenuItem(text = { Text(subject.name) }, onClick = { selectedSubjectId = subject.id; subjectMenuExpanded = false })
                    }
                }
            }
            PrimaryAction("START FOCUS", onClick = { viewModel.startFocus(selectedDuration, selectedSubject?.id) }, modifier = Modifier.fillMaxWidth())
        } else {
            Text(
                text = timer.subjectId?.let { id -> state.subjects.firstOrNull { it.id == id }?.name } ?: "Personal focus session",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            QuietAction("CANCEL SESSION", onClick = { viewModel.cancelFocus() })
        }

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("HOW THIS TIMER WORKS", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(7.dp))
                Text(
                    "The timer uses the device clock, so elapsed time is recovered when you return to the app. It does not send a background alarm or notification yet.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
