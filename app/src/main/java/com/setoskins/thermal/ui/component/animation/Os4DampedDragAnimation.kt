// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 btm_m
//
// 从 HyperChanger（https://github.com/ColdP/HyperChanger，Apache-2.0）移植：
// app/src/main/java/btm/m/liquidglass/hook/LiquidInteraction.kt
// 原注释：Adapted from the Backdrop catalog's Apache-2.0 LiquidBottomTabs interaction helpers.
package com.setoskins.thermal.ui.component.animation

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class Os4DampedDragAnimation(
    private val animationScope: CoroutineScope,
    initialValue: Float,
    private val valueRange: ClosedRange<Float>,
    visibilityThreshold: Float,
    private val initialScale: Float,
    private val pressedScale: Float,
    private val onDragStopped: Os4DampedDragAnimation.() -> Unit,
    private val onDrag: Os4DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
    private val awaitTargetBeforeRelease: Boolean = true,
) {
    private val valueAnimationSpec = spring(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec = spring(0.5f, 300f, visibilityThreshold * 10f)

    private val pressProgressAnimationSpec = spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec = spring(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec = spring(0.7f, 250f, 0.001f)
    private val valueAnimation = Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation = Animatable(0f, 5f)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val scaleXAnimation = Animatable(initialScale, 0.001f)
    private val scaleYAnimation = Animatable(initialScale, 0.001f)
    private val mutatorMutex = MutatorMutex()
    private val velocityTracker = VelocityTracker()

    val value: Float get() = valueAnimation.value
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value

    var isDragging: Boolean = false
        private set

    var hasDragged: Boolean = false
        private set

    private var dragAccum = 0f

    fun resetDragState() {
        hasDragged = false
        dragAccum = 0f
    }

    val modifier: Modifier = Modifier.pointerInput(Unit) {

        val dragSlop = viewConfiguration.touchSlop
        os4InspectDragGestures(
            onDragStart = {
                isDragging = true
                hasDragged = false
                dragAccum = 0f
                velocityTracker.resetTracking()
                press()
            },
            onDragEnd = {
                isDragging = false
                onDragStopped()
                release()
            },
            onDragCancel = {
                isDragging = false
                onDragStopped()
                release()
            },
            onDrag = { _, dragAmount ->

                if (hasDragged) {
                    onDrag(size, dragAmount)
                } else {
                    dragAccum += dragAmount.x
                    if (abs(dragAccum) > dragSlop) {
                        hasDragged = true
                        onDrag(size, Offset(dragAccum, 0f))
                        dragAccum = 0f
                    }
                }
            },
        )
    }

    private fun press() {
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    private fun release() {
        animationScope.launch {
            withFrameNanos { }
            if (awaitTargetBeforeRelease && value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun updateValue(value: Float) {
        val target = value.coerceIn(valueRange)
        animationScope.launch {
            valueAnimation.animateTo(target, valueAnimationSpec) {
                velocityTracker.addPosition(SystemClock.uptimeMillis(), Offset(this.value, 0f))
                val range = valueRange.endInclusive - valueRange.start
                val targetVelocity = velocityTracker.calculateVelocity().x / range
                animationScope.launch {
                    velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec)
                }
            }
        }
    }

    fun animateToValue(value: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                press()
                launch { valueAnimation.animateTo(value.coerceIn(valueRange), valueAnimationSpec) }
                if (velocity != 0f) launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                release()
            }
        }
    }

    suspend fun animateValueTo(target: Float, spec: AnimationSpec<Float>) {
        mutatorMutex.mutate {
            valueAnimation.animateTo(target.coerceIn(valueRange), spec)
        }
    }

    suspend fun glideTo(target: Float) = animateValueTo(target, valueAnimationSpec)

    fun pressFromOutside() = press()

    fun releaseFromOutside() = release()
}

private suspend fun PointerInputScope.os4InspectDragGestures(
    onDragStart: (PointerInputChange) -> Unit,
    onDragEnd: (PointerInputChange) -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (PointerInputChange, Offset) -> Unit,
) {
    awaitEachGesture {
        val initialDown = awaitFirstDown(false, PointerEventPass.Initial)
        val down = awaitFirstDown(false)
        onDragStart(down)
        onDrag(initialDown, Offset.Zero)
        val up = drag(initialDown.id) { onDrag(it, it.positionChange()) }
        if (up == null) onDragCancel() else onDragEnd(up)
    }
}

private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    if (currentEvent.changes.firstOrNull { it.id == pointerId }?.pressed != true) return null
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == pointer } ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) {
            val other = event.changes.firstOrNull { it.pressed }
            if (other == null) return change
            pointer = other.id
        } else if (change.previousPosition != change.position) {
            onDrag(change)
            pointer = change.id
        }
    }
}
