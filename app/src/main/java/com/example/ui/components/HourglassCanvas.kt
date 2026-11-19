package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Ultra-realistic 60fps Synchronized Hourglass rendered in pure Compose Canvas.
 * [remainingFraction] in [0f..1f]: 1f = top bulb full, 0f = drained into bottom conical pile.
 * Uses smooth linear interpolation between ticks for fluid real-time sand depletion.
 */
@Composable
fun SynchronizedHourglass(
    remainingFraction: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infinite = rememberInfiniteTransition(label = "nox_hourglass_stream")
    val streamOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sand_particles"
    )

    val animatedFraction by animateFloatAsState(
        targetValue = remainingFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (isRunning) 920 else 280, easing = LinearEasing),
        label = "hourglass_smooth_fraction"
    )

    val frac = animatedFraction.coerceIn(0f, 1f)
    val filled = 1f - frac

    // Pre-allocated paths and grain seeds for glitch-free 60fps animation
    val glassPath = remember { Path() }
    val topSandPath = remember { Path() }
    val bottomSandPath = remember { Path() }
    val leftSpecularPath = remember { Path() }
    val rightSpecularPath = remember { Path() }
    val grainSeeds = remember {
        floatArrayOf(0.05f, 0.14f, 0.23f, 0.31f, 0.40f, 0.49f, 0.57f, 0.66f, 0.75f, 0.84f, 0.92f)
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val midX = w * 0.50f
        val midY = h * 0.50f

        val capHeight = h * 0.068f
        val frameInsetX = w * 0.14f
        val glassPadX = w * 0.22f
        val topGlassY = capHeight + h * 0.015f
        val botGlassY = h - capHeight - h * 0.015f
        val neckHalf = w * 0.030f
        val pillarXLeft = frameInsetX + w * 0.032f
        val pillarXRight = w - frameInsetX - w * 0.032f

        // 1. Turned wooden/brass side pillars behind glass + delicate brass waist ring
        val brassBrush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF7B5630), Color(0xFFE0B879), Color(0xFF8C6239))
        )
        drawLine(
            brush = brassBrush,
            start = Offset(pillarXLeft, capHeight),
            end = Offset(pillarXLeft, h - capHeight),
            strokeWidth = (w * 0.020f).coerceAtLeast(3f),
            cap = StrokeCap.Round
        )
        drawLine(
            brush = brassBrush,
            start = Offset(pillarXRight, capHeight),
            end = Offset(pillarXRight, h - capHeight),
            strokeWidth = (w * 0.020f).coerceAtLeast(3f),
            cap = StrokeCap.Round
        )

        // 2. Blown-glass silhouette path
        glassPath.reset()
        glassPath.moveTo(glassPadX, topGlassY)
        glassPath.lineTo(w - glassPadX, topGlassY)
        glassPath.cubicTo(
            w - glassPadX + w * 0.02f, h * 0.31f,
            midX + neckHalf * 1.35f, h * 0.43f,
            midX + neckHalf, midY
        )
        glassPath.cubicTo(
            midX + neckHalf * 1.35f, h * 0.57f,
            w - glassPadX + w * 0.02f, h * 0.69f,
            w - glassPadX, botGlassY
        )
        glassPath.lineTo(glassPadX, botGlassY)
        glassPath.cubicTo(
            glassPadX - w * 0.02f, h * 0.69f,
            midX - neckHalf * 1.35f, h * 0.57f,
            midX - neckHalf, midY
        )
        glassPath.cubicTo(
            midX - neckHalf * 1.35f, h * 0.43f,
            glassPadX - w * 0.02f, h * 0.31f,
            glassPadX, topGlassY
        )
        glassPath.close()

        // Translucent blown-glass interior depth
        drawPath(
            path = glassPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFFECDAC5).copy(alpha = 0.09f),
                    Color(0xFF3E271A).copy(alpha = 0.20f)
                ),
                center = Offset(midX, midY),
                radius = h * 0.55f
            )
        )

        val sandBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF6DEB3),
                Color(0xFFD9A462),
                Color(0xFFAD743B)
            )
        )
        val crestBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFCE9C5),
                Color(0xFFE0AC6B),
                Color(0xFF9C6530)
            )
        )

        val topSpan = midY - topGlassY - h * 0.035f
        val botSpan = botGlassY - midY - h * 0.045f
        val bottomApexY = botGlassY - botSpan * filled

        // 3. Sand mass clipped strictly inside glass vessel
        clipPath(glassPath) {
            // Top bulb sand with draining funnel depression
            if (frac > 0.004f) {
                val sandSurfaceY = midY - topSpan * frac
                val dipDepth = if (isRunning && frac < 0.96f) {
                    (h * 0.034f) * (1f - frac * 0.45f)
                } else {
                    h * 0.008f
                }
                val halfSpan = (midX - glassPadX + w * 0.04f)
                topSandPath.reset()
                topSandPath.moveTo(midX - halfSpan, sandSurfaceY)
                topSandPath.quadraticTo(midX, sandSurfaceY + dipDepth, midX + halfSpan, sandSurfaceY)
                topSandPath.lineTo(midX + neckHalf * 1.6f, midY + 2f)
                topSandPath.lineTo(midX - neckHalf * 1.6f, midY + 2f)
                topSandPath.close()
                drawPath(path = topSandPath, brush = sandBrush)

                // Sunlit top sand rim highlight + funnel shadow
                drawLine(
                    color = Color(0xFFFFF2D6).copy(alpha = 0.62f),
                    start = Offset(midX - halfSpan * 0.65f, sandSurfaceY + dipDepth * 0.25f),
                    end = Offset(midX, sandSurfaceY + dipDepth * 0.58f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }

            // Falling continuous stream + fine sand grains from neck to mound apex
            if (isRunning && frac > 0.004f) {
                val streamStartY = midY - 2f
                val streamEndY = bottomApexY + 4f
                if (streamEndY > streamStartY) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFEBC2),
                                Color(0xFFE0AC6B),
                                Color(0xFFF7DCAA)
                            )
                        ),
                        start = Offset(midX, streamStartY),
                        end = Offset(midX, streamEndY),
                        strokeWidth = 1.9.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    val streamLen = streamEndY - streamStartY
                    val dotBaseR = 1.4.dp.toPx()
                    for (i in grainSeeds.indices) {
                        val phase = (grainSeeds[i] + streamOffset) % 1f
                        val py = streamStartY + streamLen * phase
                        val wobble = sin((phase * 14f + i).toDouble()).toFloat() * (1.4.dp.toPx())
                        val alpha = if (phase > 0.9f) (1f - phase) * 10f else 0.92f
                        drawCircle(
                            color = if (i % 2 == 0) Color(0xFFFFF1CE).copy(alpha = alpha) else Color(0xFFD69D57).copy(alpha = alpha),
                            radius = if (i % 3 == 0) dotBaseR * 1.25f else dotBaseR * 0.85f,
                            center = Offset(midX + wobble, py)
                        )
                    }
                }
            }

            // Bottom bulb natural conical mound (grows smoothly with elapsed time)
            if (filled > 0.004f) {
                val coneShoulderWidth = (midX - glassPadX + w * 0.05f) * (0.42f + 0.64f * filled)
                val edgeBaseY = botGlassY - (botSpan * 0.28f * (filled * filled))
                bottomSandPath.reset()
                bottomSandPath.moveTo(midX - coneShoulderWidth, botGlassY + 2f)
                bottomSandPath.lineTo(midX - coneShoulderWidth * 0.88f, edgeBaseY)
                bottomSandPath.quadraticTo(
                    midX - coneShoulderWidth * 0.25f,
                    bottomApexY + (edgeBaseY - bottomApexY) * 0.35f,
                    midX,
                    bottomApexY
                )
                bottomSandPath.quadraticTo(
                    midX + coneShoulderWidth * 0.25f,
                    bottomApexY + (edgeBaseY - bottomApexY) * 0.35f,
                    midX + coneShoulderWidth * 0.88f,
                    edgeBaseY
                )
                bottomSandPath.lineTo(midX + coneShoulderWidth, botGlassY + 2f)
                bottomSandPath.close()
                drawPath(path = bottomSandPath, brush = crestBrush)

                // Sunlit left slope highlight + warm strata band
                drawLine(
                    color = Color(0xFFFFF4DC).copy(alpha = 0.50f),
                    start = Offset(midX, bottomApexY + 2f),
                    end = Offset(midX - coneShoulderWidth * 0.55f, edgeBaseY - 2f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Subtle impact sparkle grains at the apex while sand is flowing
                if (isRunning && frac > 0.01f) {
                    val bounce = sin((streamOffset * 6.283f).toDouble()).toFloat()
                    drawCircle(
                        color = Color(0xFFFFF6DE).copy(alpha = 0.88f),
                        radius = 1.7.dp.toPx(),
                        center = Offset(midX - 3.dp.toPx() * bounce, bottomApexY - 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFE7B370).copy(alpha = 0.82f),
                        radius = 1.4.dp.toPx(),
                        center = Offset(midX + 3.dp.toPx() * bounce, bottomApexY + 1.dp.toPx())
                    )
                }
            }
        }

        // Delicate brass pinch band at hourglass waist
        drawRoundRect(
            brush = brassBrush,
            topLeft = Offset(midX - neckHalf * 1.45f, midY - h * 0.008f),
            size = Size(neckHalf * 2.9f, h * 0.016f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 4. Curved glass specular reflections (left primary glare + right thin rim glare)
        leftSpecularPath.reset()
        leftSpecularPath.moveTo(glassPadX + w * 0.055f, topGlassY + h * 0.03f)
        leftSpecularPath.cubicTo(
            glassPadX + w * 0.035f, h * 0.28f,
            midX - neckHalf * 2.1f, h * 0.42f,
            midX - neckHalf * 1.45f, midY - h * 0.02f
        )
        drawPath(
            path = leftSpecularPath,
            color = Color.White.copy(alpha = 0.34f),
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )

        rightSpecularPath.reset()
        rightSpecularPath.moveTo(midX + neckHalf * 1.55f, midY + h * 0.04f)
        rightSpecularPath.cubicTo(
            midX + neckHalf * 2.2f, h * 0.60f,
            w - glassPadX - w * 0.04f, h * 0.72f,
            w - glassPadX - w * 0.055f, botGlassY - h * 0.035f
        )
        drawPath(
            path = rightSpecularPath,
            color = Color.White.copy(alpha = 0.26f),
            style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
        )

        // Outer glass contour
        drawPath(
            path = glassPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF5E8).copy(alpha = 0.70f),
                    Color(0xFFDAB795).copy(alpha = 0.65f),
                    Color(0xFFFFF5E8).copy(alpha = 0.70f)
                )
            ),
            style = Stroke(width = 1.8.dp.toPx())
        )

        // 5. Top and Bottom Walnut Wood & Brass End Caps
        val woodBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF6B442E), Color(0xFF3E2416), Color(0xFF28160C))
        )
        val capWidth = w - frameInsetX * 2f
        val corner = CornerRadius(capHeight * 0.45f, capHeight * 0.45f)

        // Top cap + brass collar
        drawRoundRect(
            brush = woodBrush,
            topLeft = Offset(frameInsetX, 0f),
            size = Size(capWidth, capHeight),
            cornerRadius = corner
        )
        drawRoundRect(
            brush = brassBrush,
            topLeft = Offset(frameInsetX + w * 0.04f, capHeight - h * 0.012f),
            size = Size(capWidth - w * 0.08f, h * 0.018f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Bottom cap + brass collar
        drawRoundRect(
            brush = woodBrush,
            topLeft = Offset(frameInsetX, h - capHeight),
            size = Size(capWidth, capHeight),
            cornerRadius = corner
        )
        drawRoundRect(
            brush = brassBrush,
            topLeft = Offset(frameInsetX + w * 0.04f, h - capHeight - h * 0.006f),
            size = Size(capWidth - w * 0.08f, h * 0.018f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }
}
