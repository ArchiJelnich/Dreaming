package com.archi.dreaming.data

/** Сколько шагов у мечты всего и сколько из них выполнено. */
data class StepProgress(
    val dreamId: Long,
    val total: Int,
    val done: Int,
)
