package com.savii.focus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Column {
                Text("Simple by design", color = MaterialTheme.colorScheme.onBackground, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("SAVII FOCUS · Version 1.1.0", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        item {
            SettingsCard(
                title = "PRIVACY FIRST",
                body = "Your subjects, tasks and completed focus sessions are stored locally on this device. This build does not request internet access and does not create an online account. App backup is disabled, so uninstalling the app or clearing its storage removes this data."
            )
        }
        item {
            SettingsCard(
                title = "WHAT WORKS NOW",
                body = "Subject list · Task planner · Focus timer presets · Saved focus sessions · Basic progress totals"
            )
        }
        item {
            SettingsCard(
                title = "NOT CONNECTED YET",
                body = "AI assistant · Sign-in · Cloud synchronisation · Cross-device sync · Background timer notifications · Due-date reminders"
            )
        }
        item {
            SettingsCard(
                title = "PERFORMANCE",
                body = "The app is offline-first, uses a small local database, and keeps the main screen focused on short lists. Real device testing is still needed to measure RAM use, battery impact and scrolling performance on your Redmi Note 14."
            )
        }
        item {
            androidx.compose.material3.OutlinedButton(onClick = onBack, shape = RoundedCornerShape(14.dp)) { Text("BACK TO HOME") }
        }
    }
}

@Composable
private fun SettingsCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(17.dp)) {
            Text(title, color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
            Text(body, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 19.sp)
        }
    }
}
