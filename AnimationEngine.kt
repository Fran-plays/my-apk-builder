package com.petmorph.ai.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Practical hybrid animator for single-image companions:
 * squash & stretch, rotation, offsets, pseudo-blink and talking mouth are
 * layered on top of the bitmap. No fake skeletal rigging.
 */
class CompanionAnimator {
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)
    val rotation = Animatable(0f)
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)
    val alpha = Animatable(1f)
    val blink = Animatable(1f)     // 1 = eyes open, 0 = closed
    val mouthOpen = Animatable(0f) // 0 = closed, 1 = wide
    val wobble = Animatable(0f)

    private suspend fun Animatable<Float, AnimationVector1D>.to(
        v: Float, ms: Int = 200
    ) = animateTo(v, tween(ms))

    private suspend fun snap(
        sx: Float = 1f, sy: Float = 1f, rot: Float = 0f, dx: Float = 0f,
        dy: Float = 0f, al: Float = 1f, mo: Float = 0f, wo: Float = 0f,
    ) {
        scaleX.snapTo(sx); scaleY.snapTo(sy); rotation.snapTo(rot)
        offsetX.snapTo(dx); offsetY.snapTo(dy); alpha.snapTo(al)
        mouthOpen.snapTo(mo); wobble.snapTo(wo)
    }

    fun play(state: AnimationState, talking: Boolean) = kotlinx.coroutines.coroutineScope {
        // Gentle idle breath always underneath
        launch {
            while (isActive) {
                if (!talking) scaleY.animateTo(1.03f, tween(900))
                if (!talking) scaleY.animateTo(0.99f, tween(900))
                if (isActive) kotlinx.coroutines.delay(60)
            }
        }
        // Pseudo-blink loop
        launch {
            while (isActive) {
                kotlinx.coroutines.delay(2600 + (0..1800).random().toLong())
                blink.animateTo(0.05f, tween(70))
                blink.animateTo(1f, tween(110))
            }
        }
        // Talking mouth synced to speech flag
        launch {
            while (isActive) {
                if (talking) {
                    mouthOpen.animateTo(0.6f + (0..40).random() / 100f, tween(90))
                    mouthOpen.animateTo(0.1f, tween(80))
                } else {
                    if (mouthOpen.value > 0.01f) mouthOpen.animateTo(0f, tween(120))
                    else kotlinx.coroutines.delay(100)
                }
            }
        }

        when (state) {
            AnimationState.IDLE -> loop {
                snap()
                scaleX.to(1.02f, 900); scaleX.to(1f, 900)
            }
            AnimationState.HAPPY -> loop {
                snap()
                repeat(2) { scaleY.to(0.85f, 120); scaleY.to(1.12f, 160) }
                scaleY.to(1f, 200)
            }
            AnimationState.SAD -> loop {
                snap(rot = -4f, dy = 6f, al = 0.92f, wo = -0.06f)
                offsetY.to(10f, 700); offsetY.to(6f, 700)
            }
            AnimationState.ANGRY -> loop {
                snap(rot = -3f, sx = 1.05f, sy = 0.95f)
                repeat(3) {
                    offsetX.to(-8f, 60); offsetX.to(8f, 60)
                }
                offsetX.to(0f, 80)
            }
            AnimationState.SURPRISED -> loop {
                snap(sy = 1.1f, sx = 0.95f, mo = 0.4f)
                kotlinx.coroutines.delay(650)
            }
            AnimationState.SLEEP -> loop {
                snap(rot = 8f, dy = 8f, sx = 1.06f, sy = 0.92f, mo = 0.05f)
                scaleY.to(0.90f, 1400); scaleY.to(0.92f, 1400)
            }
            AnimationState.WAVE -> loop {
                snap()
                rotation.to(-6f, 150)
                repeat(3) { rotation.to(14f, 160); rotation.to(-2f, 160) }
                rotation.to(0f, 180)
            }
            AnimationState.JUMP -> loop {
                snap(sy = 0.8f, sx = 1.1f)
                offsetY.to(-60f, 240); scaleY.snapTo(1.1f); scaleX.snapTo(0.95f)
                offsetY.to(0f, 280); scaleY.snapTo(0.9f); scaleX.snapTo(1.05f)
                scaleX.to(1f, 120); scaleY.to(1f, 120)
                kotlinx.coroutines.delay(400)
            }
            AnimationState.BOUNCE -> loop {
                snap(sy = 0.85f, sx = 1.1f)
                offsetY.to(-28f, 170); offsetY.to(0f, 170)
                scaleX.snapTo(1f); scaleY.snapTo(1f)
                kotlinx.coroutines.delay(120)
            }
            AnimationState.DANCE -> loop {
                snap(rot = -10f)
                rotation.to(10f, 220); rotation.to(-10f, 220)
                scaleY.to(1.08f, 220); scaleY.to(0.98f, 220)
            }
            AnimationState.TALK -> loop {
                snap(rot = (0..4).random() - 2f)
                kotlinx.coroutines.delay(400)
            }
            AnimationState.EXCITED -> loop {
                snap(sx = 1.08f, sy = 1.08f)
                repeat(3) {
                    scaleY.to(1.18f, 130); scaleY.to(1.0f, 130)
                    offsetY.to(-12f, 130); offsetY.to(0f, 130)
                }
                scaleX.to(1f, 160); scaleY.to(1f, 160)
            }
            AnimationState.CLICK -> {
                snap(sy = 0.8f, sx = 1.15f)
                scaleY.to(1.1f, 90); scaleX.to(0.95f, 90)
                scaleY.to(1f, 120); scaleX.to(1f, 120)
                play(AnimationState.IDLE, talking)
            }
        }
    }

    private suspend fun loop(block: suspend () -> Unit) {
        while (kotlinx.coroutines.coroutineContext.isActive) {
            block()
        }
    }
}

@Composable
fun rememberCompanionAnimator(): CompanionAnimator {
    val animator = remember { CompanionAnimator() }
    return animator
}
