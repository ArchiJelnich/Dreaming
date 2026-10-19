package com.archi.dreaming.ui.dreams

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.archi.dreaming.R
import com.archi.dreaming.data.StepProgress

@Composable
fun ProgressPie(progress: StepProgress, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = progress.done.toFloat() / progress.total,
        label = "progress",
    )
    val color = MaterialTheme.colorScheme.primary
    val description = stringResource(R.string.steps_progress, progress.done, progress.total)

    Canvas(
        modifier = modifier
            .size(32.dp)
            .semantics { contentDescription = description },
    ) {
        val strokeWidth = 4.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = (size.minDimension - strokeWidth) / 2,
            style = Stroke(width = strokeWidth),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * fraction,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
