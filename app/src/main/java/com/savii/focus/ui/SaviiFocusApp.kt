package com.savii.focus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.savii.focus.util.FocusTimerMath
import kotlinx.coroutines.delay

private enum class SaviiTab(val label: String, val symbol: String) {
    HOME("Home", "⌂"),
    LEARNING("Learn", "▤"),
    PLANNER("Plan", "▦"),
    FOCUS("Focus", "◷"),
    INSIGHTS("Stats", "◈"),
    SETTINGS("Settings", "⚙")
}

@Composable
fun SaviiFocusApp(viewModel: FocusViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabName by rememberSaveable { mutableStateOf(SaviiTab.HOME.name) }
    val selectedTab = SaviiTab.valueOf(selectedTabName)
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> isResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isResumed, state.activeTimer?.startedAtMillis) {
        val timer = state.activeTimer
        if (isResumed && timer != null) {
            while (true) {
                val now = System.currentTimeMillis()
                viewModel.refreshClock(now)
                if (FocusTimerMath.remainingSeconds(timer.durationSeconds, timer.startedAtMillis, now) == 0) {
                    viewModel.completeFocusIfReady(now)
                    delay(1_000L)
                    if (viewModel.uiState.value.activeTimer?.startedAtMillis != timer.startedAtMillis) break
                } else {
                    delay(1_000L)
                }
            }
        }
    }

    LaunchedEffect(state.message) {
        val message = state.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .statusBarsPadding().padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SAVII FOCUS", color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(selectedTab.label, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.TextButton(
                    onClick = { selectedTabName = if (selectedTab == SaviiTab.SETTINGS) SaviiTab.HOME.name else SaviiTab.SETTINGS.name }
                ) {
                    Text(if (selectedTab == SaviiTab.SETTINGS) "HOME" else "SETTINGS", fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
                SaviiTab.entries.filter { it != SaviiTab.SETTINGS }.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTabName = tab.name },
                        icon = {
                            Text(
                                tab.symbol,
                                fontSize = 19.sp,
                                color = if (selectedTab == tab) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(tab.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                SaviiTab.HOME -> HomeScreen(state, viewModel, onOpenFocus = { selectedTabName = SaviiTab.FOCUS.name }, onOpenPlanner = { selectedTabName = SaviiTab.PLANNER.name })
                SaviiTab.LEARNING -> LearningScreen(state, viewModel)
                SaviiTab.PLANNER -> PlannerScreen(state, viewModel)
                SaviiTab.FOCUS -> FocusScreen(state, viewModel)
                SaviiTab.INSIGHTS -> InsightsScreen(state)
                SaviiTab.SETTINGS -> SettingsScreen(onBack = { selectedTabName = SaviiTab.HOME.name })
            }
        }
    }
}
