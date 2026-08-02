package com.sparkgym.ui.heatmap

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import com.sparkgym.core.design.SparkColors
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.model.Muscle

/**
 * The heat map itself: a body silhouette with every trained muscle shaded by how
 * much work it has taken relative to its weekly target.
 */
import androidx.compose.foundation.gestures.detectDragGestures

@Composable
fun MuscleHeatMap(
    heat: Map<Muscle, HeatmapEngine.MuscleHeat>,
    isFront: Boolean = true,
    modifier: Modifier = Modifier,
    angle: ViewAngle = if (isFront) ViewAngle.FRONT else ViewAngle.BACK,
    selected: Muscle? = null,
    onAngleChange: ((ViewAngle) -> Unit)? = null,
    onMuscleTap: (Muscle?) -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(BodyGeometry.VIEW_WIDTH / BodyGeometry.VIEW_HEIGHT)
            .pointerInput(angle) {
                detectTapGestures { offset ->
                    val scale = size.height / BodyGeometry.VIEW_HEIGHT
                    val drawnWidth = BodyGeometry.VIEW_WIDTH * scale
                    val originX = (size.width - drawnWidth) / 2f
                    val vx = (offset.x - originX) / scale
                    val vy = offset.y / scale
                    onMuscleTap(BodyGeometry.muscleAtAngle(vx, vy, angle))
                }
            }
            .pointerInput(angle) {
                var totalDrag = 0f
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount.x
                        if (totalDrag > 60f) {
                            onAngleChange?.invoke(angle.previous())
                            totalDrag = 0f
                        } else if (totalDrag < -60f) {
                            onAngleChange?.invoke(angle.next())
                            totalDrag = 0f
                        }
                    }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val scale = size.height / BodyGeometry.VIEW_HEIGHT
            val drawnWidth = BodyGeometry.VIEW_WIDTH * scale
            val originX = (size.width - drawnWidth) / 2f

            fun path(poly: BodyGeometry.Poly): Path = Path().apply {
                poly.points.forEachIndexed { index, (px, py) ->
                    val x = originX + px * scale
                    val y = py * scale
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            // 1. Body base silhouette for current viewing angle.
            val silhouette = BodyGeometry.silhouetteFor(angle)
            silhouette.forEach { p ->
                drawPath(path(p), color = SparkColors.HeatCold)
                drawPath(path(p), color = SparkColors.TextMuted.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
            }

            // 2. Muscles, shaded by intensity.
            val source = BodyGeometry.musclesForAngle(angle)
            source.forEach { (muscle, polys) ->
                val entry = heat[muscle]
                val intensity = entry?.intensity ?: 0f
                val fill = heatColor(intensity)
                val isSelected = muscle == selected
                polys.forEach { p ->
                    val pathObj = path(p)
                    drawPath(pathObj, color = fill)
                    drawPath(
                        pathObj,
                        color = if (isSelected) SparkColors.Cyan else SparkColors.TextSecondary.copy(alpha = 0.5f),
                        style = Stroke(width = if (isSelected) 3f else 1.2f)
                    )
                    if (intensity > 1.15f) {
                        drawPath(
                            pathObj,
                            color = SparkColors.Danger,
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }

            // 3. Rotation axis line.
            drawLine(
                color = SparkColors.Cyan.copy(alpha = 0.12f),
                start = Offset(originX + drawnWidth / 2f, 0f),
                end = Offset(originX + drawnWidth / 2f, size.height),
                strokeWidth = 1f
            )
        }
    }
}

/**
 * Cold → hot ramp. The stops are chosen so "on target" lands in the teal band,
 * which reads as good, and only genuine overreaching goes red.
 */
fun heatColor(intensity: Float): Color = when {
    intensity <= 0.01f -> SparkColors.HeatCold
    intensity < 0.35f -> lerp(SparkColors.HeatCold, SparkColors.HeatLow, intensity / 0.35f)
    intensity < 0.7f -> lerp(SparkColors.HeatLow, SparkColors.HeatMid, (intensity - 0.35f) / 0.35f)
    intensity < 1.0f -> lerp(SparkColors.HeatMid, SparkColors.HeatHigh, (intensity - 0.7f) / 0.3f)
    else -> lerp(SparkColors.HeatHigh, SparkColors.HeatMax, ((intensity - 1.0f) / 0.4f).coerceIn(0f, 1f))
}

/** Small horizontal ramp used as the map's legend. */
@Composable
fun HeatLegend(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val steps = 40
        val w = size.width / steps
        for (i in 0 until steps) {
            drawRect(
                color = heatColor(i / (steps - 1f) * 1.4f),
                topLeft = Offset(i * w, 0f),
                size = androidx.compose.ui.geometry.Size(w + 1f, size.height)
            )
        }
    }
}
