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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlannerScreen(state: FocusUiState, viewModel: FocusViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf("ALL") }
    val visibleTasks = remember(state.tasks, filter) {
        when (filter) {
            "OPEN" -> state.tasks.filterNot { it.isDone }
            "DONE" -> state.tasks.filter { it.isDone }
            else -> state.tasks
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Plan with purpose", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Small steps make progress.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                PrimaryAction("+ TASK", onClick = { showAddDialog = true })
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "OPEN", "DONE").forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option) })
                }
            }
        }
        item { SectionHeader("TASKS · ${visibleTasks.size}") }
        if (visibleTasks.isEmpty()) {
            item { EmptyPanel("No tasks here", "Add a task or switch the filter to see another list.") }
        } else {
            items(visibleTasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onCheckedChange = { viewModel.setTaskCompleted(task, it) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(15.dp)) {
                    Text("A helpful habit", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Choose a few clear tasks for today. You can add due dates and reminders in a future update.", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }

    if (showAddDialog) {
        var title by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create a task") },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(100) },
                    label = { Text("What needs to be done?") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addTask(title); showAddDialog = false }) { Text("ADD TASK") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") } }
        )
    }
}
