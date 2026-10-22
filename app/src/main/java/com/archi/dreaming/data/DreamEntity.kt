package com.archi.dreaming.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val imagePath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val position: Int = 0,
    val fulfilledAt: Long? = null,
)
