package com.sparkgym.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.util.S
import com.sparkgym.domain.model.Muscle
import com.sparkgym.ui.common.MuscleDot
import com.sparkgym.ui.common.muscleGroupColor
import com.sparkgym.ui.heatmap.BodyGeometry

/**
 * Pick a muscle by pointing at it.
 *
 * The whole problem with an exercise library is that it is indexed by names you
 * only know once you no longer need the library. Tapping the part of the body
 * you want to train needs no vocabulary at all, in any language.
 *
 * Front and back are both shown at once — a beginner should not have to know
 * that lats are on the back before they can find them.
 */
@Composable
fun BodyPartPicker(
    selected: Muscle?,
    onSelect: (Muscle?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(S.tapBodyPart, style = SystemLabel.copy(color = SparkColors.TextSecondary))
            AnimatedVisibility(selected != null) {
                Text(
                    S.clear,
                    style = SystemLabel.copy(color = SparkColors.Danger),
                    modifier = Modifier.clickable { onSelect(null) }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SelectableBody(selected, isFront = true, onSelect = onSelect)
            Spacer(Modifier.width(20.dp))
            SelectableBody(selected, isFront = false, onSelect = onSelect)
        }

        selected?.let {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MuscleDot(it)
            }
        }
    }
}

@Composable
private fun SelectableBody(
    selected: Muscle?,
    isFront: Boolean,
    onSelect: (Muscle?) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth(0.42f)
            .aspectRatio(BodyGeometry.VIEW_WIDTH / BodyGeometry.VIEW_HEIGHT)
            .pointerInput(isFront) {
                detectTapGestures { offset ->
                    val scale = size.height / BodyGeometry.VIEW_HEIGHT
                    val originX = (size.width - BodyGeometry.VIEW_WIDTH * scale) / 2f
                    val tapped = BodyGeometry.muscleAt(
                        (offset.x - originX) / scale,
                        offset.y / scale,
                        isFront
                    )
                    // Tapping the same region again clears it, so the control is
                    // its own undo.
                    onSelect(if (tapped == selected) null else tapped)
                }
            }
    ) {
        Canvas(Modifier.matchParentSize()) {
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

            val silhouette = if (isFront) BodyGeometry.silhouetteFront else BodyGeometry.silhouetteBack
            silhouette.forEach { drawPath(path(it), SparkColors.Divider.copy(alpha = 0.35f)) }

            val regions = if (isFront) BodyGeometry.front else BodyGeometry.back
            regions.forEach { (muscle, polys) ->
                val isSelected = muscle == selected
                val fill: Color = if (isSelected) {
                    muscleGroupColor(muscle.group)
                } else {
                    SparkColors.Divider.copy(alpha = 0.55f)
                }
                polys.forEach { poly ->
                    val p = path(poly)
                    drawPath(p, fill)
                    drawPath(
                        p,
                        if (isSelected) SparkColors.TextPrimary else SparkColors.Panel,
                        style = Stroke(width = if (isSelected) 1.6f else 0.8f)
                    )
                }
            }
        }
    }
}
