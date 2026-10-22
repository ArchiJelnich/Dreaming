package com.archi.dreaming.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {

    @Query("SELECT * FROM dreams ORDER BY position ASC, createdAt DESC")
    fun observeAll(): Flow<List<DreamEntity>>

    @Insert
    suspend fun insert(dream: DreamEntity): Long

    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun getById(id: Long): DreamEntity?

    @Update
    suspend fun update(dream: DreamEntity)

    @Delete
    suspend fun delete(dream: DreamEntity)

    @Query("SELECT * FROM dreams WHERE fulfilledAt IS NULL AND imagePath IS NOT NULL ORDER BY id")
    fun observeActiveWithPhoto(): Flow<List<DreamEntity>>

    @Query("SELECT MIN(position) FROM dreams")
    suspend fun minPosition(): Int?

    @Query("UPDATE dreams SET fulfilledAt = :fulfilledAt, position = :position WHERE id = :id")
    suspend fun setFulfilled(id: Long, fulfilledAt: Long?, position: Int)

    @Query("UPDATE dreams SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    @Transaction
    suspend fun reorder(ids: List<Long>) {
        ids.forEachIndexed { index, id -> setPosition(id, index) }
    }
}
