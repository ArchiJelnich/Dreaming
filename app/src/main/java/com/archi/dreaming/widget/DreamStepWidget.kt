package com.archi.dreaming.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
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
import com.archi.dreaming.data.StepEntity
import com.archi.dreaming.ui.dreams.todayUtcMillis
import com.archi.dreaming.ui.theme.Cocoa600
import com.archi.dreaming.ui.theme.Cocoa900
import com.archi.dreaming.ui.theme.Peach100
import com.archi.dreaming.ui.theme.Rose500
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

class DreamStepWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = DreamDatabase.get(context)
        val stepOfTheDay = combine(
            db.dreamDao().observeAll(),
            db.stepDao().observeOpenStepsOfActiveDreams(),
        ) { dreams, openSteps -> pickForToday(dreams, openSteps) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
        val initial = stepOfTheDay.first()
        provideContent {
            val pick by stepOfTheDay.collectAsState(initial)
            DreamStepWidgetContent(context, pick)
        }
    }

    private fun pickForToday(
        dreams: List<DreamEntity>,
        openSteps: List<StepEntity>,
    ): Pair<DreamEntity, StepEntity>? {
        val firstOpenStep = openSteps.groupBy { it.dreamId }.mapValues { it.value.first() }
        if (firstOpenStep.isEmpty()) return null
        val dreamIds = firstOpenStep.keys.sorted()
        val day = todayUtcMillis() / 86_400_000L
        val dreamId = dreamIds[Random(day).nextInt(dreamIds.size)]
        val dream = dreams.find { it.id == dreamId } ?: return null
        return dream to firstOpenStep.getValue(dreamId)
    }
}

@Composable
private fun DreamStepWidgetContent(context: Context, pick: Pair<DreamEntity, StepEntity>?) {
    Column(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = if (pick != null) Alignment.Start else Alignment.CenterHorizontally,
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(Peach100))
            .padding(16.dp)
            .clickable(actionStartActivity(MainActivity.openDreamIntent(context, pick?.first?.id))),
    ) {
        if (pick != null) {
            val (dream, step) = pick
            Text(
                text = context.getString(R.string.step_widget_label),
                style = TextStyle(color = ColorProvider(Rose500), fontSize = 12.sp, fontWeight = FontWeight.Medium),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = step.text,
                maxLines = 3,
                style = TextStyle(color = ColorProvider(Cocoa900), fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = dream.title,
                maxLines = 1,
                style = TextStyle(color = ColorProvider(Cocoa600), fontSize = 13.sp),
            )
        } else {
            Text(
                text = context.getString(R.string.step_widget_empty),
                style = TextStyle(color = ColorProvider(Cocoa600), fontSize = 14.sp, textAlign = TextAlign.Center),
            )
        }
    }
}
