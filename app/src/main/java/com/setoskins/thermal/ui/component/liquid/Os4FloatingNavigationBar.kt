// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 btm_m
//
// 从 HyperChanger **v1.1.3**（https://github.com/ColdP/HyperChanger，Apache-2.0）移植：
// app/src/main/java/btm/m/liquidglass/hook/GlassNavigation.kt 的 HyperFloatingNavigationBar。
// ⚠️ 版本号不能删（2026-11-02 查证）：v1.1.3 是 Apache-2.0，上游后来换成了 AGPL-3.0-or-later；
//    Apache-2.0 不可撤销，本文件按 v1.1.3 的授权使用。
// 改动：去掉与业务无关的 tabIconContent/FontNavigationItem 分支，标签用传进来的 ImageVector；
//      宽度改成参数（2026-10-04 按主题商店 4-tab 底栏截图实测对齐：0.88 屏宽 + 内边距
//      —— 主题商店实测 4dp，用户随后要求改成 3dp；原上游的 floatingScale 缩放整体移除）。
package com.setoskins.thermal.ui.component.liquid

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.Capsule
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlur
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import com.setoskins.thermal.ui.component.animation.Os4DampedDragAnimation

internal data class NavTab(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

internal val NavBarBottomGap = 26.dp
internal val NavBarPillHeight = 56.7.dp

private val NavIconBox = 24.dp
private val NavLabelSize = 11.sp

private val NavLabelSizeOnly = 14.sp

private val NavIconLabelGap = 2.5.dp

private val NavContentOpticalOffset = 0.25.dp

private const val NavIndicatorStepRatio = 1.108f

private val NavBarInnerPadding = 3.dp

private fun navWidthFractionFor(tabCount: Int): Float = when (tabCount) {
    3 -> 0.72f
    4 -> 0.88f
    5 -> 1.00f
    else -> 0.88f
}

private fun calcIndicatorWidthPx(innerWidthPx: Float, tabCount: Int): Float {
    val r = NavIndicatorStepRatio
    return r * innerWidthPx / (tabCount - 1 + r)
}

private fun calcIndicatorWidthFromScreenPx(
    screenWidthPx: Float,
    widthFraction: Float,
    tabCount: Int,
    innerPaddingPx: Float,
): Float {
    val innerWidthPx = screenWidthPx * widthFraction - innerPaddingPx * 2
    return calcIndicatorWidthPx(innerWidthPx, tabCount)
}

private const val GlassBlurRadiusDp = 11f

private val NavBarShadowSpread = 3.dp

private val NavBarShadowBlur = 28.dp

private const val NavBarShadowAlpha = 0.13f

private const val NavBounceBaseAmplitude = 0.015f
private const val NavBouncePerTabAmplitude = 0.02f
private const val NavBounceStaggerMs = 50L

private const val NavBounceRiseDamping = 1f
private const val NavBounceRiseStiffness = 3200f

private const val NavBounceReturnMs = 200
private val NavBounceReturnEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

private const val NavBounceTrailDelayMs = 50L

private const val NavInertiaRangePx = 20f

private const val NavInertiaRangeTrailPx = 10f

private const val NavBounceStretchWaitMs = 900L

private const val NavStretchHandoffRemain = 0.03f

private const val NavBounceHandoffPx = 16f

private const val NAV_TAG = "HyperExtremeNav"

private const val NavTabFlyStiffness = 535f

private const val NavTabFlyThreshold = 0.001f

private fun navTabFlySpec(span: Float) =
    spring(1f, NavTabFlyStiffness, NavTabFlyThreshold * span.coerceAtLeast(0.05f))

private const val NavStretchV0 = 150f
private const val NavStretchV1 = 900f
private const val NavStretchMax = 0.25f

private val NavStretchEasing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

private const val NavStretchTauDrag = 0.06f
private const val NavStretchTauFly = 0.02f

private const val NavStretchFallDuration = 0.15f
private val NavStretchFallEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

private const val NavStretchFingerStillSpeed = 50f

private const val NavEdgeFallDelaySec = 0.25f

private const val NavEdgeSqueezeFraction = 0.05f
private const val NavEdgeSqueezeMs = 150

private val NavGlassStroke = BloomStroke(
    color = Color.White,
    innerBlurRadius = 1.35.dp,
    primaryLight = LightSource(LightPosition(0.5f, 0.5f, -0.5f), intensity = 0f),
    secondaryLight = LightSource(LightPosition(0.5f, 0.8f, -0.5f), intensity = 0f),
)
private val NavGlassHighlightLight = Highlight(width = 1.35.dp, alpha = 0.9f, style = NavGlassStroke)
private val NavGlassHighlightDark = Highlight(width = 1.35.dp, alpha = 0.1f, style = NavGlassStroke)

@Composable
internal fun Os4FloatingNavigationBar(
    tabs: List<NavTab>,
    selectedIndex: MutableIntState,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    showIcons: Boolean = true,
    showLabels: Boolean = true,
    advancedMaterial: Boolean = true,
    isDarkTheme: Boolean = false,
    accentColorOverride: Color? = null,

    widthFraction: Float? = null,
    onTabSelected: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val innerPaddingPx = with(density) { NavBarInnerPadding.toPx() }
    val animationScope = rememberCoroutineScope()

    val bounceLeft = remember { Animatable(0f) }
    val bounceRight = remember { Animatable(0f) }

    val squeezeLeft = remember { Animatable(0f) }
    val squeezeRight = remember { Animatable(0f) }

    var squeezeEdge by remember { mutableStateOf(0) }

    val stretchNow = remember { mutableFloatStateOf(0f) }

    val dirNow = remember { mutableFloatStateOf(0f) }

    var isHolding by remember { mutableStateOf(false) }

    var isFlying by remember { mutableStateOf(false) }

    var tabFlyTarget by remember { mutableIntStateOf(-1) }
    var tabFlyToken by remember { mutableIntStateOf(0) }

    var suppressBounce by remember { mutableStateOf(false) }

    var suppressSync by remember { mutableStateOf(false) }

    val layoutTabWidthPx = remember { mutableFloatStateOf(0f) }

    val stretchOverShoot = remember { mutableFloatStateOf(0f) }

    val flyLeadShift = remember { mutableFloatStateOf(0f) }
    val flyTrailShift = remember { mutableFloatStateOf(0f) }

    val flyBoxOvershoot = remember { mutableFloatStateOf(0f) }

    var flyInertiaActive by remember { mutableStateOf(false) }

    val fallFromV = remember { mutableFloatStateOf(0f) }
    val fallElapsedV = remember { mutableFloatStateOf(0f) }
    val fallingV = remember { mutableStateOf(false) }
    val tabCount = tabs.size.coerceAtLeast(1)

    val effectiveWidthFraction = widthFraction ?: navWidthFractionFor(tabCount)

    val indicatorWidthPx = calcIndicatorWidthFromScreenPx(
        screenWidthPx, effectiveWidthFraction, tabCount, innerPaddingPx,
    )
    val initialIndex = selectedIndex.intValue.coerceIn(0, tabCount - 1)
    val dragAnimation = remember(tabCount) {
        Os4DampedDragAnimation(
            animationScope = animationScope,
            initialValue = initialIndex.toFloat(),
            valueRange = 0f..(tabCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,

            pressedScale = 0.915f,
            onDragStopped = {

                isHolding = false
                squeezeEdge = 0

                if (hasDragged) {
                    val target = targetValue.roundToInt().coerceIn(0, tabCount - 1)
                    if (target == selectedIndex.intValue) {

                        animateToValue(target.toFloat())
                    } else {

                        suppressBounce = true
                        onTabSelected(target)
                    }
                }
            },
            onDrag = { _, amount ->

                isHolding = true
                val step = layoutTabWidthPx.floatValue
                if (step > 0f) {
                    val maxValue = (tabCount - 1).toFloat()

                    val raw = targetValue + amount.x / step
                    updateValue(raw.coerceIn(0f, maxValue))

                    squeezeEdge = when {
                        targetValue <= 0f && raw < 0f -> -1
                        targetValue >= maxValue && raw > maxValue -> 1
                        else -> 0
                    }
                }
            },

            awaitTargetBeforeRelease = false,
        )
    }

    suspend fun runEdgePulse(edge: Animatable<Float, AnimationVector1D>, offsetPx: Float) {
        Log.d(NAV_TAG, "runEdgePulse 起  offset=$offsetPx  (edge 当前=${edge.value})")
        edge.snapTo(0f)
        edge.animateTo(
            targetValue = offsetPx,
            animationSpec = spring(dampingRatio = NavBounceRiseDamping, stiffness = NavBounceRiseStiffness),
        )
        Log.d(NAV_TAG, "runEdgePulse 到峰  edge=${edge.value}")

        edge.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = NavBounceReturnMs, easing = NavBounceReturnEasing),
        )
        Log.d(NAV_TAG, "runEdgePulse 归零  edge=${edge.value}")
    }

    suspend fun waitHandoff(target: Float) {
        val step = layoutTabWidthPx.floatValue
        withTimeoutOrNull(NavBounceStretchWaitMs) {
            snapshotFlow { dragAnimation.value }
                .first { abs(it - target) * step <= NavBounceHandoffPx }
            snapshotFlow { stretchNow.floatValue }.first { it <= NavStretchHandoffRemain }
        }
    }

    suspend fun playBounce(from: Float, target: Float) {

        Log.d(
            NAV_TAG,
            "playBounce 入口 from=$from target=$target hasDragged=${dragAnimation.hasDragged} " +
                "isDragging=${dragAnimation.isDragging} step=${layoutTabWidthPx.floatValue}",
        )
        if (dragAnimation.hasDragged) {
            Log.d(NAV_TAG, "playBounce 跳过：hasDragged=true")
            return
        }
        val delta = target - from
        if (delta == 0f) {
            Log.d(NAV_TAG, "playBounce 跳过：delta=0（按的是当前已选中的标签）")
            return
        }
        val movingRight = delta > 0f

        val span = abs(delta)
        val overshoot = layoutTabWidthPx.floatValue *
            (NavBounceBaseAmplitude + NavBouncePerTabAmplitude * span) *
            (if (movingRight) 1f else -1f)
        Log.d(NAV_TAG, "playBounce 执行 movingRight=$movingRight span=$span overshoot=$overshoot")

        val leftAllowed = !(!movingRight && target <= 0f)
        val rightAllowed = !(movingRight && target >= (tabCount - 1).toFloat())

        val lead = if (movingRight) bounceRight else bounceLeft
        val trail = if (movingRight) bounceLeft else bounceRight
        val leadAllowed = if (movingRight) rightAllowed else leftAllowed
        val trailAllowed = if (movingRight) leftAllowed else rightAllowed
        try {
            coroutineScope {
                if (leadAllowed) launch { runEdgePulse(lead, overshoot) }
                if (trailAllowed) {
                    launch {
                        if (leadAllowed) delay(NavBounceStaggerMs)
                        runEdgePulse(trail, overshoot)
                    }
                }
            }
        } finally {

            withContext(NonCancellable) {
                bounceLeft.snapTo(0f)
                bounceRight.snapTo(0f)
            }
        }
    }

    LaunchedEffect(isHolding, isFlying) {

        fun updateInertia() {
            if (!flyInertiaActive) {
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
                return
            }

            val step = layoutTabWidthPx.floatValue
            val remainPx = abs(dragAnimation.value - dragAnimation.targetValue) * step
            val p = (1f - remainPx / NavInertiaRangePx).coerceIn(0f, 1f)

            val overshoot = flyBoxOvershoot.floatValue

            val pLead = (1f - remainPx / NavInertiaRangePx).coerceIn(0f, 1f)
            val pTrail = (1f - remainPx / NavInertiaRangeTrailPx).coerceIn(0f, 1f)
            flyLeadShift.floatValue = overshoot * pLead
            flyTrailShift.floatValue = overshoot * pTrail
            Log.d(
                NAV_TAG,
                "惯性过冲 pLead=${"%.3f".format(pLead)} pTrail=${"%.3f".format(pTrail)} " +
                    "remain=${"%.1f".format(remainPx)}px " +
                    "lead=${"%.2f".format(flyLeadShift.floatValue)} " +
                    "trail=${"%.2f".format(flyTrailShift.floatValue)}",
            )
        }
        if (isHolding || isFlying) {

            val fingerGate = isHolding

            if (isHolding) {
                stretchOverShoot.floatValue = 0f
                flyInertiaActive = false
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            }
            var prevValue = dragAnimation.value
            var prevTarget = dragAnimation.targetValue

            var flyElapsedSec = 0f
            var prevNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { nanos ->
                    val dtSec = (nanos - prevNanos) / 1_000_000_000f
                    val value = dragAnimation.value
                    val targetValue = dragAnimation.targetValue
                    if (dtSec > 0f) {
                        val step = layoutTabWidthPx.floatValue

                        val velocity = (value - prevValue) / dtSec * step
                        val speed = abs(velocity)

                        val fingerSpeed = abs(targetValue - prevTarget) / dtSec * step
                        if (isFlying) flyElapsedSec += dtSec

                        val edgeFallDue =
                            stretchOverShoot.floatValue > 1e-4f &&
                                flyElapsedSec >= NavEdgeFallDelaySec
                        val target = if (
                            (fingerGate && fingerSpeed < NavStretchFingerStillSpeed) || edgeFallDue
                        ) {
                            0f
                        } else {

                            NavStretchEasing.transform(
                                ((speed - NavStretchV0) / (NavStretchV1 - NavStretchV0))
                                    .coerceIn(0f, 1f),
                            ) * NavStretchMax
                        }
                        if (target <= 0f) {

                            if (!fallingV.value) {
                                fallFromV.floatValue = stretchNow.floatValue
                                fallElapsedV.floatValue = 0f
                                fallingV.value = true
                            }
                            fallElapsedV.floatValue += dtSec
                            val t =
                                (fallElapsedV.floatValue / NavStretchFallDuration).coerceIn(0f, 1f)
                            val fallTarget = -stretchOverShoot.floatValue
                            stretchNow.floatValue = fallFromV.floatValue +
                                (fallTarget - fallFromV.floatValue) *
                                NavStretchFallEasing.transform(t)
                            if (t >= 1f) {
                                stretchNow.floatValue = fallTarget
                                fallingV.value = false
                            }
                        } else {

                            fallingV.value = false
                            val tau = if (isFlying) NavStretchTauFly else NavStretchTauDrag
                            val alpha = 1f - exp(-dtSec / tau)
                            stretchNow.floatValue += (target - stretchNow.floatValue) * alpha
                        }

                        if (speed > NavStretchV0) {
                            val dirTarget = if (velocity > 0f) 1f else -1f
                            dirNow.floatValue +=
                                (dirTarget - dirNow.floatValue) * (1f - exp(-dtSec / NavStretchTauDrag))
                        }
                        prevValue = value
                    }

                    updateInertia()
                    prevTarget = targetValue
                    prevNanos = nanos
                }
            }
        } else {

            val over = stretchOverShoot.floatValue
            val fallTarget = -over

            if (abs(stretchNow.floatValue - fallTarget) > 5e-4f) {
                if (!fallingV.value) {
                    fallFromV.floatValue = stretchNow.floatValue
                    fallElapsedV.floatValue = 0f
                    fallingV.value = true
                }
                var prevFallNanos = withFrameNanos { it }
                while (true) {
                    var finished = false
                    withFrameNanos { nanos ->
                        fallElapsedV.floatValue += (nanos - prevFallNanos) / 1_000_000_000f
                        prevFallNanos = nanos
                        val t =
                            (fallElapsedV.floatValue / NavStretchFallDuration).coerceIn(0f, 1f)
                        stretchNow.floatValue = fallFromV.floatValue +
                            (fallTarget - fallFromV.floatValue) * NavStretchFallEasing.transform(t)
                        if (t >= 1f) finished = true
                    }
                    if (finished) break
                }
                stretchNow.floatValue = fallTarget
                fallingV.value = false
            }

            val backFromStretch = stretchNow.floatValue
            val backFromLead = flyLeadShift.floatValue
            val backFromTrail = flyTrailShift.floatValue

            val trailDelayMs = if (backFromTrail > 0.01f) NavBounceTrailDelayMs else 0L
            if (over > 1e-4f || backFromLead > 0.01f || backFromTrail > 0.01f) {
                Log.d(
                    NAV_TAG,
                    "整体移回：stretch=$backFromStretch lead=$backFromLead " +
                        "trail=$backFromTrail（后缘滞后 ${trailDelayMs}ms）",
                )
                val startNanos = withFrameNanos { it }
                while (true) {
                    var finished = false
                    withFrameNanos { nanos ->
                        val elapsedMs = (nanos - startNanos) / 1_000_000f
                        val tLead = (elapsedMs / NavBounceReturnMs).coerceIn(0f, 1f)
                        val tTrail =
                            ((elapsedMs - trailDelayMs) / NavBounceReturnMs).coerceIn(0f, 1f)
                        val kLead = 1f - NavBounceReturnEasing.transform(tLead)
                        val kTrail = 1f - NavBounceReturnEasing.transform(tTrail)
                        stretchNow.floatValue = backFromStretch * kLead
                        flyLeadShift.floatValue = backFromLead * kLead
                        flyTrailShift.floatValue = backFromTrail * kTrail
                        if (elapsedMs >= NavBounceReturnMs + trailDelayMs) finished = true
                    }
                    if (finished) break
                }
                stretchNow.floatValue = 0f
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            }
            flyInertiaActive = false
        }
    }

    LaunchedEffect(squeezeEdge) {
        val amount = indicatorWidthPx * NavEdgeSqueezeFraction
        coroutineScope {
            launch {
                squeezeRight.animateTo(
                    targetValue = if (squeezeEdge == -1) -amount else 0f,
                    animationSpec = tween(NavEdgeSqueezeMs),
                )
            }
            launch {
                squeezeLeft.animateTo(
                    targetValue = if (squeezeEdge == 1) amount else 0f,
                    animationSpec = tween(NavEdgeSqueezeMs),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d(NAV_TAG, "=== 底栏组合完成 tabCount=$tabCount ===")
    }

    LaunchedEffect(tabFlyToken) {
        if (tabFlyToken > 0) {
            val target = tabFlyTarget.coerceIn(0, tabCount - 1).toFloat()
            val from = dragAnimation.value
            Log.d(NAV_TAG, "fly effect 启动 token=$tabFlyToken from=$from target=$target")

            dirNow.floatValue = if (target >= from) 1f else -1f

            val spanPx = abs(target - from)
            val overshootPx = layoutTabWidthPx.floatValue *
                (NavBounceBaseAmplitude + NavBouncePerTabAmplitude * spanPx)
            val edgeCase = target <= 0f || target >= (tabCount - 1).toFloat()
            if (edgeCase) {

                stretchOverShoot.floatValue = overshootPx / indicatorWidthPx
                flyBoxOvershoot.floatValue = 0f
                flyInertiaActive = false
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            } else {

                stretchOverShoot.floatValue = 0f
                flyBoxOvershoot.floatValue = overshootPx
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
                flyInertiaActive = true
                Log.d(
                    NAV_TAG,
                    "中间格惯性过冲：overshoot=$overshootPx span=$spanPx" +
                        "（前缘前伸 + 后缘内缩，后缘滞后）",
                )
            }

            isFlying = true

            dragAnimation.animateValueTo(target, navTabFlySpec(spanPx))

            isFlying = false
        }
    }
    LaunchedEffect(selectedIndex.intValue) {
        val target = selectedIndex.intValue.coerceIn(0, tabCount - 1).toFloat()
        val from = dragAnimation.value
        val fromDrag = suppressBounce
        suppressBounce = false
        val fromTab = suppressSync
        suppressSync = false

        if (!fromTab) {

            if (fromDrag) {

                dragAnimation.glideTo(target)
            } else {

                bounceLeft.snapTo(0f)
                bounceRight.snapTo(0f)

                val glide = launch { dragAnimation.glideTo(target) }
                waitHandoff(target)
                playBounce(from, target)
                glide.join()
            }
        }
    }
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val selectedContentColor = accentColorOverride ?: contentColor

    val pressedContentColor = if (isDarkTheme) Color(0xFFB4B4B4) else Color(0xFF434343)
    val pressedIndex = if (dragAnimation.pressProgress > 0.001f) {
        dragAnimation.value.roundToInt().coerceIn(0, tabCount - 1)
    } else {
        -1
    }
    val selectedSurface = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val selectedSurfacePressed = if (isDarkTheme) {
        Color.White.copy(alpha = 0.30f)
    } else {
        Color.Black.copy(alpha = 0.19f)
    }
    val selectedSurfaceNow = lerp(
        selectedSurface,
        selectedSurfacePressed,
        dragAnimation.pressProgress.coerceIn(0f, 1f),
    )

    val containerSurface = if (isDarkTheme) {
        Color(0xFF252525).copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.625f)
    }
    val fallbackSurface = if (isDarkTheme) {
        Color(0xFF242424).copy(alpha = 0.88f)
    } else {
        Color.White.copy(alpha = 0.82f)
    }
    val capsule = Capsule()
    val shadowColor = Color.Black.copy(alpha = NavBarShadowAlpha)
    val useGlass = advancedMaterial && isRuntimeShaderSupported() && backdrop != null

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth(effectiveWidthFraction)

                .padding(bottom = NavBarBottomGap)
                .height(NavBarPillHeight),
            contentAlignment = Alignment.CenterStart,
        ) {

            val innerPadding = NavBarInnerPadding
            val innerWidthPx = with(density) { (maxWidth - innerPadding * 2).toPx() }

            val blockWidthPx = calcIndicatorWidthPx(innerWidthPx, tabCount)
            val blockStepPx = ((innerWidthPx - blockWidthPx) / (tabCount - 1).coerceAtLeast(1))
                .coerceAtLeast(1f)
            val iconCentersPx = List(tabCount) { blockWidthPx / 2f + it * blockStepPx }
            val cellBoundsPx = List(tabCount + 1) { idx ->
                when (idx) {
                    0 -> 0f
                    tabCount -> innerWidthPx
                    else -> (iconCentersPx[idx - 1] + iconCentersPx[idx]) / 2f
                }
            }
            val cellWidthsPx = List(tabCount) { cellBoundsPx[it + 1] - cellBoundsPx[it] }

            val iconOffsetsPx = List(tabCount) {
                iconCentersPx[it] - (cellBoundsPx[it] + cellWidthsPx[it] / 2f)
            }

            SideEffect { layoutTabWidthPx.floatValue = blockStepPx }
            val blockWidth = with(density) { blockWidthPx.toDp() }
            val selectedTranslation = dragAnimation.value * blockStepPx

            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(
                        width = maxWidth + NavBarShadowSpread * 2,
                        height = NavBarPillHeight + NavBarShadowSpread * 2,
                    )

                    .blur(NavBarShadowBlur, BlurredEdgeTreatment.Unbounded)
                    .background(shadowColor, capsule),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {

                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .then(
                        if (useGlass) {
                            Modifier

                                .textureBlur(
                                    backdrop = backdrop,
                                    shape = capsule,
                                    blurRadius = GlassBlurRadiusDp,

                                    noiseCoefficient = 0f,

                                    highlight = if (isDarkTheme) NavGlassHighlightDark else NavGlassHighlightLight,
                                )

                                .background(containerSurface, capsule)
                        } else {
                            Modifier.clip(capsule).background(fallbackSurface)
                        },
                    ),
            )
            Box(Modifier.fillMaxSize().padding(innerPadding)) {

                Box(
                    Modifier
                        .width(blockWidth)
                        .fillMaxHeight()
                        .graphicsLayer {

                            val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)
                            translationX =
                                selectedTranslation + bounceLeft.value + squeezeLeft.value * press
                            scaleX = dragAnimation.scaleX
                            scaleY = dragAnimation.scaleY
                        },
                ) {

                    fun stretchSpan(): Pair<Float, Float> {
                        val stretchPx = blockWidthPx * stretchNow.floatValue

                        if (stretchPx == 0f && flyLeadShift.floatValue == 0f &&
                            flyTrailShift.floatValue == 0f
                        ) {
                            return 0f to 0f
                        }
                        val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)
                        val pivot = blockWidthPx / 2f

                        val gap = pivot * (1f - press)
                        val tx = selectedTranslation + bounceLeft.value + squeezeLeft.value * press
                        val leftRoom = (-((gap - tx - pivot) / press + pivot)).coerceAtLeast(0f)
                        val rightRoom =
                            ((innerWidthPx - gap - tx - pivot) / press + pivot - blockWidthPx)
                                .coerceAtLeast(0f)

                        val leftShare = ((1f + dirNow.floatValue) / 2f).coerceIn(0f, 1f)

                        val lead = flyLeadShift.floatValue
                        val trail = flyTrailShift.floatValue
                        return (
                            stretchPx * leftShare + lead * (1f - leftShare) - trail * leftShare
                            ).coerceAtMost(leftRoom) to (
                            stretchPx * (1f - leftShare) + lead * leftShare -
                                trail * (1f - leftShare)
                            ).coerceAtMost(rightRoom)
                    }

                    Box(
                        Modifier
                            .layout { measurable, constraints ->
                                val (left, right) = stretchSpan()
                                val w = (blockWidthPx + left + right).roundToInt().coerceAtLeast(1)
                                val placeable =
                                    measurable.measure(Constraints.fixed(w, constraints.maxHeight))
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    placeable.place(0, 0)
                                }
                            }
                            .graphicsLayer {
                                val (left, right) = stretchSpan()
                                val w = blockWidthPx + left + right
                                val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)

                                val lShift = bounceLeft.value + squeezeLeft.value * press
                                val rRender = (bounceRight.value + squeezeRight.value) * press

                                val rLocal = (rRender - lShift * (1f - press)) / press

                                translationX = -left
                                scaleX = 1f + (rLocal - lShift) / w.coerceAtLeast(1f)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .clip(capsule)
                            .background(selectedSurfaceNow),
                    )
                }
                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val pressedProgress =
                            if (pressedIndex == index) dragAnimation.pressProgress else 0f

                        val color = lerp(
                            selectedContentColor,
                            pressedContentColor,
                            pressedProgress.coerceIn(0f, 1f),
                        )

                        val tabInteraction = remember(index) { MutableInteractionSource() }
                        val tabPressed by tabInteraction.collectIsPressedAsState()

                        var everPressed by remember(index) { mutableStateOf(false) }
                        LaunchedEffect(tabPressed) {
                            Log.d(
                                NAV_TAG,
                                "tab[$index] pressed=$tabPressed everPressed=$everPressed " +
                                    "selected=${selectedIndex.intValue} hasDragged=${dragAnimation.hasDragged}",
                            )
                            if (tabPressed) {
                                everPressed = true

                                dragAnimation.resetDragState()
                                dragAnimation.pressFromOutside()
                                tabFlyTarget = index
                                tabFlyToken++
                            } else if (everPressed) {
                                dragAnimation.releaseFromOutside()
                                if (index != selectedIndex.intValue) {
                                    suppressSync = true
                                    onTabSelected(index)
                                }
                            }
                        }
                        Column(

                            Modifier
                                .weight(cellWidthsPx[index])
                                .fillMaxHeight()

                                .clickable(
                                    interactionSource = tabInteraction,
                                    indication = null,
                                    role = Role.Tab,
                                ) { },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {

                            Column(
                                Modifier.offset(
                                    x = with(density) { iconOffsetsPx[index].toDp() },
                                    y = NavContentOpticalOffset,
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (showIcons) {
                                    Image(
                                        tab.icon,
                                        null,
                                        Modifier.size(NavIconBox),
                                        colorFilter = ColorFilter.tint(color),
                                    )
                                }
                                if (showLabels) {
                                    BasicText(
                                        tab.label,
                                        style = TextStyle(
                                            color = color,
                                            fontSize = if (showIcons) NavLabelSize else NavLabelSizeOnly,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = if (showIcons) {
                                            Modifier.padding(top = NavIconLabelGap)
                                        } else {
                                            Modifier
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .width(blockWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = selectedTranslation
                            scaleX = dragAnimation.scaleX
                            scaleY = dragAnimation.scaleY
                        }
                        .then(dragAnimation.modifier),
                )
            }
        }
    }
}
