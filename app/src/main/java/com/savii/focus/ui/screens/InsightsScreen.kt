package com.savii.focus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(state: FocusUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Progress you can see", color = MaterialTheme.colorScheme.onBackground, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Your focus sessions are saved on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("TODAY", readableDuration(state.todayFocusSeconds), true, Modifier.weight(1f))
                StatCard("LAST 7 DAYS", readableDuration(state.lastSevenDaysFocusSeconds), false, Modifier.weight(1f))
            }
        }
        item {
            StatCard("ALL-TIME FOCUS", readableDuration(state.totalFocusSeconds), true, Modifier.fillMaxWidth())
        }
        item { SectionHeader("RECENT SESSIONS") }
        if (state.recentSessions.isEmpty()) {
            item { EmptyPanel("Your progress starts here", "Complete a focus timer to see your first saved session.") }
        } else {
            items(state.recentSessions, key = { it.id }) { session ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(session.subjectName ?: "Focus session", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(formatDate(session.completedAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                        Text("${session.durationSeconds / 60} min", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            Text("The 7-day total is calculated from all saved sessions in the local database. Detailed charts and longer-term reports can be added later.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, lineHeight = 16.sp)
        }
    }
}

private fun readableDuration(seconds: Long): String {
    val totalMinutes = seconds / 60L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()).format(Date(epochMillis))
}
