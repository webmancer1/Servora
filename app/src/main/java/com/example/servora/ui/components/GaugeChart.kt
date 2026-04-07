package com.example.servora.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CoralRed
import com.example.servora.ui.theme.MetricUnitStyle
import com.example.servora.ui.theme.MetricValueSmallStyle
import com.example.servora.ui.theme.MetricValueStyle
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextSecondary

@Composable
fun GaugeChart(
    value: Float,
    maxValue: Float = 100f,
    label: String,
    unit: String = "%",
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    startColor: Color = NeonCyan,
    endColor: Color = NeonCyan,
    isLarge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val percentage = (value / maxValue).coerceIn(0f, 1f)
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val gaugeColor = when {
        percentage > 0.85f -> CoralRed
        percentage > 0.7f -> AmberWarning
        else -> startColor
    }

    val gaugeEndColor = when {
        percentage > 0.85f -> CoralRed
        percentage > 0.7f -> AmberWarning
        else -> endColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size
            val arcSize = Size(
                canvasSize.width - strokeWidth.toPx(),
                canvasSize.height - strokeWidth.toPx()
            )
            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

            drawArc(
                color = CardBorder,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(gaugeColor, gaugeEndColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedValue.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}",
                style = if (isLarge) MetricValueStyle else MetricValueSmallStyle,
                color = gaugeColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = unit,
                style = MetricUnitStyle,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (isLarge) {
                Text(
                    text = label,
                    style = MetricUnitStyle,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
