package com.archi.dreaming.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    // Шаги удаляются вместе с мечтой
    foreignKeys = [
        ForeignKey(
            entity = DreamEntity::class,
            parentColumns = ["id"],
            childColumns = ["dreamId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dreamId")],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dreamId: Long,
    val text: String,
    val done: Boolean = false,
    val position: Int = 0,
)
