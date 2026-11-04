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

/** 悬浮底栏的拖拽/按压动画：阻尼拖动 + 选中胶囊跟随 + 按压缩放。 */
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
    /**
     * 按压 / 恢复的弹簧（2026-10-05 用户定：**三条全回上游原值**）。
     *
     * 上游（`ColdP/HyperChanger` 的 `hook/LiquidInteraction.kt`）就是这三条，我们移植时逐条一致；
     * 中途被改成过 `k = 2250`（三条统一 0.1s），用户试完觉得"按压太快"，于是整体回退。
     *
     * ⚠️ 上游**三条本来就不同步**，那是设计不是巧合 —— 别再把它们凑成同一个时长：
     * * `pressProgress`（图标/文字弱化 + 底色加深）`ζ = 1`、`k = 1000` → 95% 收敛 ≈ **0.15s**；
     * * `scaleX`（块缩到 `pressedScale`）`ζ = 0.6`、`k = 250` → 按包络 `3/(ζ√k)` ≈ **0.32s**；
     * * `scaleY` 同上、`ζ = 0.7` → ≈ **0.27s**。
     *
     * 即"**颜色先变、块后缩**"，而且**缩放（0.27~0.32s）比飞行（0.24s）还慢** ——
     * 块到位之后还在继续往下沉一点。
     * 收敛时间：临界阻尼 `t95 = 4.74/√k`；欠阻尼按包络 `3/(ζ√k)`。
     * 标签按下时由 [pressFromOutside] 走同一套，所以"按住标签"和"按住拖动层"的压感一致。
     */
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

    /**
     * 手指当前是否按在拖动层上（回弹播放前查一次 —— 用户已经接管拖动时就不弹，
     * 避免回弹的位移/收缩叠在拖动上）。UI 单线程读写，不需要额外的同步。
     */
    var isDragging: Boolean = false
        private set

    /**
     * 本手势里**真的产生过横向位移**（2026-10-05 加）。
     *
     * 为什么不能拿 [isDragging] 当这个判断：拖动层是铺在标签之上的整层透明 Box，
     * 手指按标签时它也会收到 down → [isDragging] 立刻为 true、并一直挂到抬手才复位。
     * 而"按标签"这条路径要在块到位后播点击回弹，`playBounce` 若查 [isDragging] 会被
     * 误伤（手指还按着 → 一脚都弹不出来，用户 2026-10-05 报的就是这个）。
     * 所以回弹的守卫改用这个标记：只有真的拖过块才跳过。
     */
    var hasDragged: Boolean = false
        private set
    /**
     * 死区里攒下的横向位移（px）—— 跨过 `viewConfiguration.touchSlop` 时整笔补发。
     */
    private var dragAccum = 0f

    /**
     * 由"标签按下"这条路径调用：把"真的拖过"标记与死区累积一起复位（2026-10-05 加）。
     *
     * 为什么必须由外部复位：[modifier] 只有 `blockWidth` 宽、且跟着块走，
     * **手指按在离块较远的标签上时拖动层压根收不到 down** → `onDragStart` 不跑 →
     * [hasDragged] 保持上一次的值。一旦它被置成 `true`（真拖过、或按住块时手指累计漂移
     * 超过 touchSlop），之后所有"点远处标签"的回弹都会被 `playBounce` 的守卫**静默吞掉**，
     * 直到点了块附近的标签或重启 App —— 用户 2026-10-05 报的"开始有、点几下没了、
     * 重启软件又有了"就是这个。
     *
     * 按标签天然不是拖动，所以在这里复位；之后真去拖块时 `onDragStart` + 死区会重新置起来，
     * 守卫"别和拖动叠一起"的本意不丢。
     */
    fun resetDragState() {
        hasDragged = false
        dragAccum = 0f
    }

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        // 拖动死区**直接用系统的 `touchSlop`**（默认 8dp ≈ 24px@3x）—— 上游也是拿它分流
        // tap / drag 的（`totalDrag.getDistance() < viewConfiguration.touchSlop`）。
        // ⚠️ 别自己硬写一个小值：原来写死 8px（≈2.7dp），"按住标签不动"时手指的**指压漂移**
        // 很容易超过它 → 被误判成"真的拖了" → `hasDragged = true` → 回弹被 `playBounce`
        // 的守卫生吞，而且**时有时无**（用户 2026-10-05 报的"回弹又没了"）。
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
                // 拖动**死区** = `viewConfiguration.touchSlop`：先攒位移，攒够才算"真的拖起来了"
                // （见 [hasDragged]）。
                // * down 时那次 `Offset.Zero` 本来就被这里吃掉；
                // * 按标签时手指的指压漂移同样被吃掉 —— 否则会被当成拖动，`updateValue` 里那条
                //   新动画会取消标签正在飞的弹簧，`hasDragged` 一变 true 回弹也没了。
                // 跨过死区的那一次把**攒下的位移整笔补上**，所以 1:1 跟手不会丢（只是晚一个事件）。
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

    /** 把块动画到目标格，**带一次完整的按压反馈**（press → 移动 → release）—— 拖动收尾没跨页时用。 */
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

    /**
     * 只把块**移动**到目标、用指定曲线，**不碰按压反馈**（2026-10-05 加）。
     *
     * 之所以要单独一条：标签那套是"按下 → 块飞过去 + 自己按下去 → 抬手才切页"，
     * 按压由标签自己的 press/release 管，这里若再压一次会打架。
     */
    suspend fun animateValueTo(target: Float, spec: AnimationSpec<Float>) {
        mutatorMutex.mutate {
            valueAnimation.animateTo(target.coerceIn(valueRange), spec)
        }
    }

    /** 用内部默认弹簧把块移到目标、不碰按压 —— 拖动跨页 / 手指滑页走这条。 */
    suspend fun glideTo(target: Float) = animateValueTo(target, valueAnimationSpec)

    /**
     * 标签按下时由外部触发的按压反馈 —— 与拖动路径共用 [pressProgressAnimationSpec]，
     * 所以"按住标签"和"按住拖动层"的压感完全一致。配合 [releaseFromOutside] 使用。
     */
    fun pressFromOutside() = press()

    /** 标签抬手 / 滑出时由外部触发的恢复。 */
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
