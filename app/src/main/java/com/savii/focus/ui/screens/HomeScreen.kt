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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    state: FocusUiState,
    viewModel: FocusViewModel,
    onOpenFocus: () -> Unit,
    onOpenPlanner: () -> Unit
) {
    val pendingTasks = remember(state.tasks) { state.tasks.filterNot { it.isDone }.take(3) }
    val completedCount = remember(state.tasks) { state.tasks.count { it.isDone } }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("YOUR LIFE. IN FOCUS.", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text("Make today count.", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("A calm space for learning and getting things done.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("FOCUS TODAY", formatMinutes(state.todayFocusSeconds), accent = true, modifier = Modifier.weight(1f))
                StatCard("TASKS DONE", "${completedCount}/${state.tasks.size}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                onClick = onOpenFocus,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(if (state.activeTimer == null) "READY TO FOCUS?" else "FOCUS SESSION IN PROGRESS", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(if (state.activeTimer == null) "Start a focused study session." else "Your timer is saved. Tap to continue.", color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("OPEN FOCUS  →", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            SectionHeader("YOUR SUBJECTS")
        }
        if (state.subjects.isEmpty()) {
            item { EmptyPanel("Start with one subject", "Add the subjects you are learning. They will stay saved on this device.") }
        } else {
            items(state.subjects.take(4), key = { it.id }) { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(subject.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("SUBJECT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
        item { SectionHeader("NEXT TASKS", trailing = { androidx.compose.material3.TextButton(onClick = onOpenPlanner) { Text("VIEW ALL") } }) }
        if (pendingTasks.isEmpty()) {
            item { EmptyPanel("Nothing waiting", "Add a task to your planner and it will appear here.") }
        } else {
            items(pendingTasks, key = { it.id }) { task ->
                TaskRow(
                    task,
                    onCheckedChange = { viewModel.setTaskCompleted(task, it) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }
        }
        item {
            Text("Your data stays on this device.", modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

private fun formatMinutes(seconds: Long): String {
    val minutes = seconds / 60L
    return if (minutes >= 60L) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
}
