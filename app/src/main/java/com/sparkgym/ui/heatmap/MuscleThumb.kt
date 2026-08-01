package com.sparkgym.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sparkgym.core.design.SparkColors
import com.sparkgym.domain.model.Muscle

/**
 * A small anatomical diagram of one exercise: the same body the heat map uses,
 * with this movement's prime movers filled in hot and its synergists warm.
 *
 * This is the app's answer to "show me the exercise". It is drawn from the
 * shared [BodyGeometry] polygons, so every one of the bundled exercises gets a
 * picture that is genuinely specific to it — no photo library to ship, nothing
 * to download, and it stays sharp at any size.
 */
@Composable
fun MuscleThumb(
    primary: Set<Muscle>,
    secondary: Set<Muscle>,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    primaryColor: Color = SparkColors.Danger,
    secondaryColor: Color = SparkColors.Amber,
    bodyColor: Color = SparkColors.Divider
) {
    // Only bother with the posterior view when the movement actually trains it.
    val needsBack = showBack && (primary + secondary).any {
        BodyGeometry.polysFor(it, isFront = false).isNotEmpty()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        BodyView(primary, secondary, true, primaryColor, secondaryColor, bodyColor, Modifier.fillMaxHeight())
        if (needsBack) {
            BodyView(primary, secondary, false, primaryColor, secondaryColor, bodyColor, Modifier.fillMaxHeight())
        }
    }
}

@Composable
private fun BodyView(
    primary: Set<Muscle>,
    secondary: Set<Muscle>,
    isFront: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    bodyColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.aspectRatio(BodyGeometry.VIEW_WIDTH / BodyGeometry.VIEW_HEIGHT)) {
        val scale = size.height / BodyGeometry.VIEW_HEIGHT
        val originX = (size.width - BodyGeometry.VIEW_WIDTH * scale) / 2f

        fun path(poly: BodyGeometry.Poly): Path = Path().apply {
            poly.points.forEachIndexed { i, (px, py) ->
                val x = originX + px * scale
                val y = py * scale
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawBody(isFront, bodyColor, ::path)
        drawHighlights(secondary - primary, isFront, secondaryColor, ::path)
        drawHighlights(primary, isFront, primaryColor, ::path)
    }
}

private fun DrawScope.drawBody(
    isFront: Boolean,
    bodyColor: Color,
    path: (BodyGeometry.Poly) -> Path
) {
    val silhouette = if (isFront) BodyGeometry.silhouetteFront else BodyGeometry.silhouetteBack
    silhouette.forEach { drawPath(path(it), bodyColor.copy(alpha = 0.30f)) }
}

private fun DrawScope.drawHighlights(
    muscles: Set<Muscle>,
    isFront: Boolean,
    color: Color,
    path: (BodyGeometry.Poly) -> Path
) {
    muscles.forEach { muscle ->
        BodyGeometry.polysFor(muscle, isFront).forEach { poly ->
            val p = path(poly)
            drawPath(p, color.copy(alpha = 0.85f))
            drawPath(p, color, style = Stroke(width = 0.8f))
        }
    }
}
