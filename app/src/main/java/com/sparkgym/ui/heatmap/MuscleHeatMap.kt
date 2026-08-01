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
@Composable
fun MuscleHeatMap(
    heat: Map<Muscle, HeatmapEngine.MuscleHeat>,
    isFront: Boolean,
    modifier: Modifier = Modifier,
    selected: Muscle? = null,
    onMuscleTap: (Muscle?) -> Unit = {}
) {
    val flip by animateFloatAsState(
        targetValue = if (isFront) 0f else 1f,
        animationSpec = tween(350),
        label = "body-flip"
    )

    Box(
        modifier = modifier
            .aspectRatio(BodyGeometry.VIEW_WIDTH / BodyGeometry.VIEW_HEIGHT)
            .pointerInput(isFront) {
                detectTapGestures { offset ->
                    val scale = size.height / BodyGeometry.VIEW_HEIGHT
                    val drawnWidth = BodyGeometry.VIEW_WIDTH * scale
                    val originX = (size.width - drawnWidth) / 2f
                    val vx = (offset.x - originX) / scale
                    val vy = offset.y / scale
                    onMuscleTap(BodyGeometry.muscleAt(vx, vy, isFront))
                }
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

            // 1. Body base.
            val silhouette = if (isFront) BodyGeometry.silhouetteFront else BodyGeometry.silhouetteBack
            silhouette.forEach { p ->
                drawPath(path(p), color = SparkColors.HeatCold.copy(alpha = 0.85f))
                drawPath(path(p), color = SparkColors.Divider.copy(alpha = 0.6f), style = Stroke(width = 1f))
            }

            // 2. Muscles, shaded by intensity.
            val source = if (isFront) BodyGeometry.front else BodyGeometry.back
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
                        color = if (isSelected) SparkColors.TextPrimary else SparkColors.Divider.copy(alpha = 0.7f),
                        style = Stroke(width = if (isSelected) 2.5f else 1f)
                    )
                    if (intensity > 1.15f) {
                        // Overreaching regions get a pulse ring so they stand out
                        // from "just trained a lot".
                        drawPath(
                            pathObj,
                            color = SparkColors.Danger.copy(alpha = 0.6f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // 3. Centre line, purely cosmetic scanline flavour.
            drawLine(
                color = SparkColors.Cyan.copy(alpha = 0.08f + 0.05f * flip),
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
