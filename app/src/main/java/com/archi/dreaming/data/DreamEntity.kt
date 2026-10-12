package com.archi.dreaming.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    // null — мечта без фото
    val imagePath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    // Место мечты на доске: чем меньше, тем выше
    val position: Int = 0,
)
