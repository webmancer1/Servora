package com.example.servora.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.servora.ui.theme.NeonCyan

@Composable
fun MiniLineChart(
    data: List<Float>,
    lineColor: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxVal = data.max()
        val minVal = data.min()
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val stepX = width / (data.size - 1).toFloat()

        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range) * height * 0.85f - height * 0.05f

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.2f),
                    lineColor.copy(alpha = 0.0f)
                )
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        val lastX = (data.size - 1) * stepX
        val lastY = height - ((data.last() - minVal) / range) * height * 0.85f - height * 0.05f
        drawCircle(
            color = lineColor,
            radius = 3f,
            center = Offset(lastX, lastY)
        )
    }
}
