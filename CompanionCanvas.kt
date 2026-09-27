package com.petmorph.ai.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Renders the processed companion bitmap with all animation layers applied:
 * squash/stretch, rotation, offsets, pseudo-blink (vertical eye squish region),
 * talking mouth overlay and simple particle sparkles for happy states.
 */
@Composable
fun CompanionCanvas(
    animator: CompanionAnimator,
    bitmap: android.graphics.Bitmap?,
    sizeDp: Dp = 220.dp,
    particles: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
) {
    if (bitmap == null) return
    val density = LocalDensity.current
    val img = remember(bitmap) { bitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .size(sizeDp)
            .then(
                if (onClick != null || onDoubleClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onClick?.invoke() },
                            onDoubleTap = { onDoubleClick?.invoke() },
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = img,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(animator.alpha.value)
                .graphicsLayer {
                    val blinkS = animator.blink.value
                    scaleX = animator.scaleX.value
                    scaleY = animator.scaleY.value * (0.35f + 0.65f * blinkS)
                    rotationZ = animator.rotation.value + animator.wobble.value * 10f
                    translationX = animator.offsetX.value * density.density
                    translationY = animator.offsetY.value * density.density
                }
        )
        // Talking mouth overlay (bottom-center ellipse)
        val mo = animator.mouthOpen.value
        if (mo > 0.02f) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.72f
                drawOval(
                    color = Color(0xFF5B2333),
                    topLeft = Offset(cx - 16f * mo * density.density, cy - 10f * mo * density.density),
                    size = androidx.compose.ui.geometry.Size(
                        32f * mo * density.density, 20f * mo * density.density
                    ),
                )
            }
        }
        // Sparkle particles for EXCITED / HAPPY
        if (particles) {
            Canvas(Modifier.fillMaxSize()) {
                val t = (animator.offsetY.value + animator.rotation.value) * 3f
                repeat(6) { i ->
                    val phase = (t * 0.05f + i * 1.047f).toFloat()
                    val r = (size.minDimension * 0.42f) * (0.85f + 0.15f * sin(phase).toFloat())
                    val x = size.width / 2f + r * kotlin.math.cos(phase).toFloat()
                    val y = size.height / 2f + r * sin(phase).toFloat()
                    drawCircle(
                        color = Color(0xFFFFD166).copy(alpha = 0.55f + 0.3f * sin(phase + 1f).toFloat()),
                        radius = 4f + 3f * sin(phase * 2f).toFloat(),
                        center = Offset(x, y),
                    )
                }
            }
        }
        overlay?.invoke(this)
    }
}
