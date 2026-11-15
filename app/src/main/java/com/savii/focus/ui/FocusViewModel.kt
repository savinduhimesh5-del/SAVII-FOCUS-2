package com.savii.focus.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.savii.focus.data.FocusRepository
import com.savii.focus.data.local.FocusSessionEntity
import com.savii.focus.data.local.SubjectEntity
import com.savii.focus.data.local.TaskEntity
import com.savii.focus.model.ActiveFocusTimer
import com.savii.focus.util.FocusTimerMath
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val recentSessions: List<FocusSessionEntity> = emptyList(),
    val totalFocusSeconds: Long = 0L,
    val todayFocusSeconds: Long = 0L,
    val lastSevenDaysFocusSeconds: Long = 0L,
    val activeTimer: ActiveFocusTimer? = null,
    val nowMillis: Long = System.currentTimeMillis(),
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModel(
    private val repository: FocusRepository,
    private val preferences: SharedPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private val dayStartMillis = MutableStateFlow(
        FocusTimerMath.startOfTodayMillis(System.currentTimeMillis())
    )
    private val sevenDayStartMillis = MutableStateFlow(
        FocusTimerMath.startOfLastSevenDaysMillis(System.currentTimeMillis())
    )
    private val finishingSessionStartedAt = AtomicLong(-1L)

    init {
        viewModelScope.launch {
            combine(
                repository.observeSubjects(),
                repository.observeTasks(),
                repository.observeRecentSessions(),
                repository.observeTotalFocusSeconds(),
                combine(
                    dayStartMillis.flatMapLatest(repository::observeFocusSecondsSince),
                    sevenDayStartMillis.flatMapLatest(repository::observeFocusSecondsSince)
                ) { todaySeconds, sevenDaySeconds -> todaySeconds to sevenDaySeconds }
            ) { subjects, tasks, sessions, totalSeconds, periodTotals ->
                _uiState.update {
                    it.copy(
                        subjects = subjects,
                        tasks = tasks,
                        recentSessions = sessions,
                        totalFocusSeconds = totalSeconds,
                        todayFocusSeconds = periodTotals.first,
                        lastSevenDaysFocusSeconds = periodTotals.second
                    )
                }
            }.collect { }
        }
        restoreActiveTimer()
    }

    fun refreshClock(nowMillis: Long = System.currentTimeMillis()) {
        _uiState.update { it.copy(nowMillis = nowMillis) }
        val todayStart = FocusTimerMath.startOfTodayMillis(nowMillis)
        if (dayStartMillis.value != todayStart) dayStartMillis.value = todayStart
        val sevenDayStart = FocusTimerMath.startOfLastSevenDaysMillis(nowMillis)
        if (sevenDayStartMillis.value != sevenDayStart) sevenDayStartMillis.value = sevenDayStart
        completeFocusIfReady(nowMillis)
    }

    fun addSubject(rawName: String) {
        val name = rawName.trim().take(32)
        if (name.isBlank()) {
            showMessage("Enter a subject name")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = repository.addSubject(name)
                showMessage(if (id == -1L) "That subject already exists" else "Subject added")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not add the subject. Please try again.")
            }
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteSubject(id)
                showMessage("Subject removed")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not remove the subject. Please try again.")
            }
        }
    }

    fun addTask(rawTitle: String) {
        val title = rawTitle.trim().take(100)
        if (title.isBlank()) {
            showMessage("Enter a task name")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addTask(title)
                showMessage("Task added")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not add the task. Please try again.")
            }
        }
    }

    fun setTaskCompleted(task: TaskEntity, isDone: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setTaskCompleted(task.id, isDone)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not update the task. Please try again.")
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTask(id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not delete the task. Please try again.")
            }
        }
    }

    fun startFocus(durationSeconds: Int, subjectId: Long?) {
        if (durationSeconds !in FocusTimerMath.presetsSeconds) return
        if (_uiState.value.activeTimer != null) return

        val startedAt = System.currentTimeMillis()
        val timer = ActiveFocusTimer(startedAt, durationSeconds, subjectId)
        preferences.edit()
            .putLong(KEY_STARTED_AT, startedAt)
            .putInt(KEY_DURATION, durationSeconds)
            .putLong(KEY_SUBJECT_ID, subjectId ?: NO_SUBJECT)
            .apply()
        _uiState.update { it.copy(activeTimer = timer, nowMillis = startedAt) }
    }

    fun cancelFocus() {
        val timer = _uiState.value.activeTimer ?: return
        if (finishingSessionStartedAt.get() == timer.startedAtMillis) return
        clearTimerPreferences()
        _uiState.update { it.copy(activeTimer = null, nowMillis = System.currentTimeMillis()) }
        showMessage("Focus timer cancelled")
    }

    fun completeFocusIfReady(nowMillis: Long = System.currentTimeMillis()) {
        val timer = _uiState.value.activeTimer ?: return
        val endAt = timer.startedAtMillis + timer.durationSeconds * 1_000L
        if (nowMillis < endAt) return
        if (!finishingSessionStartedAt.compareAndSet(-1L, timer.startedAtMillis)) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val subjectName = timer.subjectId?.let { repository.getSubject(it)?.name }
                repository.addFocusSession(
                    FocusSessionEntity(
                        durationSeconds = timer.durationSeconds,
                        startedAt = timer.startedAtMillis,
                        completedAt = endAt,
                        subjectName = subjectName
                    )
                )
                clearTimerPreferences()
                _uiState.update {
                    if (it.activeTimer?.startedAtMillis == timer.startedAtMillis) {
                        it.copy(activeTimer = null, nowMillis = nowMillis, message = "Focus session saved")
                    } else it
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage("Could not save this session. Open Focus and try again.")
            } finally {
                finishingSessionStartedAt.compareAndSet(timer.startedAtMillis, -1L)
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun restoreActiveTimer() {
        val startedAt = preferences.getLong(KEY_STARTED_AT, 0L)
        val duration = preferences.getInt(KEY_DURATION, 0)
        if (startedAt <= 0L || duration !in FocusTimerMath.presetsSeconds) {
            clearTimerPreferences()
            return
        }
        val storedSubjectId = preferences.getLong(KEY_SUBJECT_ID, NO_SUBJECT)
        val timer = ActiveFocusTimer(
            startedAtMillis = startedAt,
            durationSeconds = duration,
            subjectId = storedSubjectId.takeIf { it != NO_SUBJECT }
        )
        _uiState.update { it.copy(activeTimer = timer) }
        completeFocusIfReady()
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    private fun clearTimerPreferences() {
        preferences.edit()
            .remove(KEY_STARTED_AT)
            .remove(KEY_DURATION)
            .remove(KEY_SUBJECT_ID)
            .apply()
    }

    class Factory(
        private val repository: FocusRepository,
        private val preferences: SharedPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FocusViewModel::class.java)) {
                return FocusViewModel(repository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val KEY_STARTED_AT = "started_at"
        const val KEY_DURATION = "duration_seconds"
        const val KEY_SUBJECT_ID = "subject_id"
        const val NO_SUBJECT = -1L
    }
}
