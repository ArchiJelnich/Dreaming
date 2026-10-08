package com.archi.dreaming.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {

    @Query("SELECT * FROM dreams ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DreamEntity>>

    @Insert
    suspend fun insert(dream: DreamEntity): Long
}
