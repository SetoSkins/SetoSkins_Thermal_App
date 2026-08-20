package com.setoskins.thermal.ui.component.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

// ── Spring Physics (ported from compose-miuix-ui/miuix) ──────────────────────

private object SpringMath {
    const val MAX_FRAME_DELTA_SECONDS = 0.016f
    const val MIN_FRAME_DELTA_SECONDS = 0.001f
    const val HIGH_VELOCITY_THRESHOLD = 5000.0
    const val CRITICAL_DAMPING_RATIO = 1.0f
    const val STANDARD_SPRING_PERIOD = 0.4f
    const val SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY = 0.55f

    fun obtainDampingDistance(normalizedInput: Float, range: Float): Float {
        val x = maxOf(0.0f, min(normalizedInput, 1.0f)).toDouble()
        val dampedFactor = x - x.pow(2.0) + (x.pow(3.0) / 3.0)
        return (dampedFactor * range).toFloat()
    }

    fun obtainTouchDistance(currentPixelOffset: Float, range: Float): Float {
        var absPixelOffset = abs(currentPixelOffset)
        val absMaxOffset = abs(obtainDampingDistance(1.0f, range))
        if (absPixelOffset <= 0f) return 0f
        if (absPixelOffset >= absMaxOffset) {
            absPixelOffset = absMaxOffset
        }
        val base = range - (3.0 * absPixelOffset)
        val part2 = range.toDouble().pow(2.0 / 3.0) * sign(base) * abs(base).pow(1.0 / 3.0)
        return (range - part2).toFloat()
    }
}

private class SpringOperator(dampingRatio: Float, naturalPeriod: Float) {
    private val dampingCoefficient: Double
    private val stiffnessOverMass: Double

    init {
        val angularFrequency = (2.0 * PI) / naturalPeriod
        stiffnessOverMass = angularFrequency * angularFrequency
        dampingCoefficient = 2.0 * dampingRatio * angularFrequency
    }

    fun updateVelocity(
        currentVelocity: Double,
        deltaTime: Float,
        currentPosition: Double,
        targetPosition: Double,
    ): Double {
        val velocityDecayFactor = 1.0 - dampingCoefficient * deltaTime
        val velocityIncreaseFromSpring = stiffnessOverMass * (targetPosition - currentPosition) * deltaTime
        return currentVelocity * velocityDecayFactor + velocityIncreaseFromSpring
    }
}

private class SpringEngine {
    private var springOperator: SpringOperator? = null

    var velocity: Double = 0.0
    var currentPos: Double = 0.0
    private var targetPos: Double = 0.0
    private var initialPos: Double = 0.0
    private var initialVelocity: Double = 0.0

    private fun isAtEquilibrium(): Boolean {
        if (initialPos < targetPos && currentPos > targetPos) return true
        if (initialPos <= targetPos || currentPos >= targetPos) {
            return (initialPos == targetPos && sign(initialVelocity) != sign(currentPos)) ||
                abs(currentPos - targetPos) < 1.0
        }
        return true
    }

    fun start(startValue: Float, targetValue: Float, initialVel: Float) {
        currentPos = startValue.toDouble()
        initialPos = startValue.toDouble()
        targetPos = targetValue.toDouble()
        velocity = initialVel.toDouble()
        initialVelocity = initialVel.toDouble()
        springOperator = SpringOperator(
            SpringMath.CRITICAL_DAMPING_RATIO,
            if (abs(initialVel) > SpringMath.HIGH_VELOCITY_THRESHOLD) {
                SpringMath.SLOWER_SPRING_PERIOD_FOR_HIGH_VELOCITY
            } else {
                SpringMath.STANDARD_SPRING_PERIOD
            },
        )
    }

    fun step(deltaTime: Float): Boolean {
        val operator = springOperator ?: return false
        val dt = deltaTime.coerceIn(SpringMath.MIN_FRAME_DELTA_SECONDS, SpringMath.MAX_FRAME_DELTA_SECONDS)
        velocity = operator.updateVelocity(velocity, dt, currentPos, targetPos)
        currentPos += dt * velocity
        if (isAtEquilibrium()) {
            currentPos = targetPos
            velocity = 0.0
            return true
        }
        return false
    }
}

private suspend fun SpringEngine.runSettleAnimation(
    startValue: Float,
    targetValue: Float = 0f,
    initialVelocity: Float,
    onFrame: (Float) -> Unit,
    onSettle: () -> Unit,
) {
    start(startValue = startValue, targetValue = targetValue, initialVel = initialVelocity)
    var lastFrameTimeNanos = -1L
    var isFinished = false
    try {
        while (!isFinished && currentCoroutineContext().isActive) {
            isFinished = androidx.compose.runtime.withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos == -1L) {
                    lastFrameTimeNanos = frameTimeNanos
                    return@withFrameNanos false
                }
                val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                lastFrameTimeNanos = frameTimeNanos
                val finished = step(dt)
                onFrame(currentPos.toFloat())
                finished
            }
        }
    } finally {
        onSettle()
    }
}

// ── Public API ────────────────────────────────────────────────────────────────

@Stable
class CustomOverscrollState {
    var offset by mutableFloatStateOf(0f)
        internal set
}

@Composable
fun rememberCustomOverscrollState(): CustomOverscrollState {
    return remember { CustomOverscrollState() }
}

@Composable
fun Modifier.customOverScroll(
    state: CustomOverscrollState = rememberCustomOverscrollState(),
    isVertical: Boolean = true,
): Modifier {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    var containerSizePx by remember { mutableFloatStateOf(0f) }
    var rawAccumulation by remember { mutableFloatStateOf(0f) }
    var animationJob: Job? by remember { mutableStateOf(null) }
    var gestureActive by remember { mutableStateOf(false) }

    val offsetThreshold = 1f
    val springEngine = remember { SpringEngine() }

    fun resetState() {
        state.offset = 0f
        rawAccumulation = 0f
    }

    fun applyDrag(delta: Float) {
        if (delta == 0f || containerSizePx == 0f) return
        rawAccumulation += delta
        rawAccumulation = rawAccumulation.coerceIn(-containerSizePx, containerSizePx)
        val normalized = min(abs(rawAccumulation) / containerSizePx, 1.0f)
        state.offset = sign(rawAccumulation) * SpringMath.obtainDampingDistance(normalized, containerSizePx)
    }

    fun syncRawAccumulationFromOffset() {
        rawAccumulation = sign(state.offset) * SpringMath.obtainTouchDistance(state.offset, containerSizePx)
    }

    fun startSpringAnimation(initialVelocity: Float = 0f) {
        if (abs(state.offset) <= offsetThreshold && initialVelocity == 0f) {
            resetState()
            return
        }
        animationJob?.cancel()
        animationJob = scope.launch {
            springEngine.runSettleAnimation(
                startValue = state.offset,
                initialVelocity = initialVelocity,
                onFrame = { state.offset = it },
                onSettle = {
                    if (abs(state.offset) <= offsetThreshold) resetState()
                },
            )
        }
    }

    fun unwindStaleOffset(consumedDelta: Float) {
        if (abs(state.offset) <= offsetThreshold || consumedDelta == 0f) return
        if (rawAccumulation == 0f) syncRawAccumulationFromOffset()
        if (sign(consumedDelta) != sign(rawAccumulation)) return
        if (abs(rawAccumulation) <= abs(consumedDelta)) {
            resetState()
        } else {
            applyDrag(-consumedDelta)
        }
    }

    val connection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || !gestureActive) {
                    return Offset.Zero
                }

                if (animationJob?.isActive == true) syncRawAccumulationFromOffset()
                animationJob?.cancel()

                val delta = if (isVertical) available.y else available.x

                if (abs(state.offset) <= offsetThreshold || sign(delta) == sign(rawAccumulation)) {
                    return Offset.Zero
                }

                if (sign(delta) != sign(rawAccumulation)) {
                    val actualConsumed = if (abs(rawAccumulation) <= abs(delta)) {
                        -rawAccumulation
                    } else {
                        delta
                    }
                    if (abs(rawAccumulation) <= abs(delta)) {
                        resetState()
                    } else {
                        applyDrag(actualConsumed)
                    }
                    return if (isVertical) Offset(0f, actualConsumed) else Offset(actualConsumed, 0f)
                }

                applyDrag(delta)
                return if (isVertical) Offset(0f, available.y) else Offset(available.x, 0f)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || !gestureActive) {
                    if (animationJob?.isActive != true) {
                        unwindStaleOffset(if (isVertical) consumed.y else consumed.x)
                    }
                    return Offset.Zero
                }

                animationJob?.cancel()
                unwindStaleOffset(if (isVertical) consumed.y else consumed.x)

                val delta = if (isVertical) available.y else available.x
                applyDrag(delta)
                return if (isVertical) Offset(0f, delta) else Offset(delta, 0f)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (abs(state.offset) <= offsetThreshold) return Velocity.Zero

                animationJob?.cancel()

                val velocity = if (isVertical) available.y else available.x

                if (sign(velocity) != sign(state.offset)) {
                    startSpringAnimation(velocity)
                    return if (isVertical) Velocity(0f, velocity / 2.13333f) else Velocity(velocity / 2.13333f, 0f)
                } else {
                    startSpringAnimation(velocity)
                    return if (isVertical) Velocity(0f, velocity) else Velocity(velocity, 0f)
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val velocity = (if (isVertical) available.y else available.x) / 1.53333f
                startSpringAnimation(velocity)
                return if (isVertical) Velocity(0f, velocity) else Velocity(velocity, 0f)
            }
        }
    }

    return this
        .clipToBounds()
        .onSizeChanged { size ->
            if (containerSizePx == 0f) {
                containerSizePx = with(density) {
                    if (isVertical) windowInfo.containerDpSize.height.toPx()
                    else windowInfo.containerDpSize.width.toPx()
                }
            }
        }
        .graphicsLayer {
            if (isVertical) translationY = state.offset.roundToInt().toFloat()
            else translationX = state.offset.roundToInt().toFloat()
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Press -> gestureActive = true
                        PointerEventType.Release -> {
                            gestureActive = false
                            if (animationJob?.isActive != true && abs(state.offset) > offsetThreshold) {
                                startSpringAnimation()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
        .nestedScroll(connection)
}