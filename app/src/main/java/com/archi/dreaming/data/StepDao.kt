package com.archi.dreaming.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM steps WHERE dreamId = :dreamId ORDER BY position ASC, id ASC")
    fun observeForDream(dreamId: Long): Flow<List<StepEntity>>

    @Query("SELECT dreamId, COUNT(*) AS total, SUM(done) AS done FROM steps GROUP BY dreamId")
    fun observeProgress(): Flow<List<StepProgress>>

    @Query("SELECT MAX(position) FROM steps WHERE dreamId = :dreamId")
    suspend fun maxPosition(dreamId: Long): Int?

    @Insert
    suspend fun insert(step: StepEntity): Long

    @Update
    suspend fun update(step: StepEntity)

    @Delete
    suspend fun delete(step: StepEntity)
}
