package com.archi.dreaming.ui.dreams

data class Dream(
    val title: String,
    val photoIndex: Int,
    val heightDp: Int,
    val tiltDegrees: Float,
)

// Временные данные, пока нет настоящих фотографий
object SampleDreams {
    val items = listOf(
        Dream("Море на закате", 0, 220, -2.5f),
        Dream("Свой дом", 1, 160, 1.5f),
        Dream("Путешествие в Японию", 2, 190, 2f),
        Dream("Научиться рисовать", 3, 250, -1.5f),
        Dream("Сад с пионами", 4, 150, -3f),
        Dream("Собака", 5, 210, 2.5f),
        Dream("Горы и тишина", 1, 240, 1f),
        Dream("Мастерская", 0, 170, -2f),
        Dream("Северное сияние", 4, 200, 3f),
        Dream("Свой бизнес", 2, 160, -1f),
    )
}
