package com.savii.focus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM subjects ORDER BY createdAt DESC, id DESC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubject(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Long)

    @Query("SELECT * FROM study_tasks ORDER BY isDone ASC, createdAt DESC, id DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("UPDATE study_tasks SET isDone = :isDone WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isDone: Boolean)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC LIMIT 100")
    fun observeRecentSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM focus_sessions")
    fun observeTotalFocusSeconds(): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM focus_sessions WHERE startedAt >= :startAt")
    fun observeFocusSecondsSince(startAt: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long
}
