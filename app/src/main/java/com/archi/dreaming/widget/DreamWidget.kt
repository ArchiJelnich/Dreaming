package com.archi.dreaming.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.archi.dreaming.MainActivity
import com.archi.dreaming.R
import com.archi.dreaming.data.DreamDatabase
import com.archi.dreaming.data.DreamEntity
import com.archi.dreaming.ui.dreams.todayUtcMillis
import com.archi.dreaming.ui.theme.Cocoa600
import com.archi.dreaming.ui.theme.Cocoa900
import com.archi.dreaming.ui.theme.Peach100
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import kotlin.random.Random

class DreamWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dreamOfTheDay = DreamDatabase.get(context).dreamDao().observeActiveWithPhoto()
            .map { dreams -> pickForToday(dreams)?.let { it to it.imagePath?.let { path -> decodeScaled(path, 800) } } }
            .distinctUntilChanged { old, new -> old?.first == new?.first }
            .flowOn(Dispatchers.IO)
        val initial = dreamOfTheDay.first()
        provideContent {
            val shown by dreamOfTheDay.collectAsState(initial)
            DreamWidgetContent(context, shown?.first, shown?.second)
        }
    }

    private fun pickForToday(dreams: List<DreamEntity>): DreamEntity? {
        if (dreams.isEmpty()) return null
        val day = todayUtcMillis() / 86_400_000L
        return dreams[Random(day).nextInt(dreams.size)]
    }

    private fun decodeScaled(path: String, maxSide: Int): Bitmap? {
        if (!File(path).exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}

@Composable
private fun DreamWidgetContent(context: Context, dream: DreamEntity?, photo: Bitmap?) {
    Box(
        contentAlignment = if (photo != null) Alignment.BottomStart else Alignment.Center,
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(Peach100))
            .clickable(actionStartActivity(MainActivity.openDreamIntent(context, dream?.id))),
    ) {
        if (dream != null && photo != null) {
            Image(
                provider = ImageProvider(photo),
                contentDescription = dream.title,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize(),
            )
            Box(modifier = GlanceModifier.padding(10.dp)) {
                Text(
                    text = dream.title,
                    maxLines = 2,
                    style = TextStyle(
                        color = ColorProvider(Cocoa900),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .background(ColorProvider(Peach100.copy(alpha = 0.9f)))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        } else {
            Text(
                text = context.getString(R.string.widget_empty),
                style = TextStyle(
                    color = ColorProvider(Cocoa600),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = GlanceModifier.padding(16.dp),
            )
        }
    }
}
