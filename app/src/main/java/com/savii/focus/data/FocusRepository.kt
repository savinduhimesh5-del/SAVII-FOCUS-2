package com.savii.focus.data

import com.savii.focus.data.local.FocusDao
import com.savii.focus.data.local.FocusSessionEntity
import com.savii.focus.data.local.SubjectEntity
import com.savii.focus.data.local.TaskEntity

class FocusRepository(private val dao: FocusDao) {
    fun observeSubjects() = dao.observeSubjects()
    fun observeTasks() = dao.observeTasks()
    fun observeRecentSessions() = dao.observeRecentSessions()
    fun observeTotalFocusSeconds() = dao.observeTotalFocusSeconds()
    fun observeFocusSecondsSince(startAt: Long) = dao.observeFocusSecondsSince(startAt)

    suspend fun getSubject(id: Long) = dao.getSubject(id)
    suspend fun addSubject(name: String): Long = dao.insertSubject(SubjectEntity(name = name))
    suspend fun deleteSubject(id: Long) = dao.deleteSubject(id)
    suspend fun addTask(title: String): Long = dao.insertTask(TaskEntity(title = title))
    suspend fun setTaskCompleted(id: Long, isDone: Boolean) = dao.setTaskCompleted(id, isDone)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)
    suspend fun addFocusSession(session: FocusSessionEntity): Long = dao.insertFocusSession(session)
}
