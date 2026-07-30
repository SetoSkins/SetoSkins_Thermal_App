package com.setoskins.thermal.ui.component

import android.graphics.Paint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.data.ModuleDetector
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 解析时间字符串 "MM-DD HH:mm:ss" 为秒数
private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
internal fun parseTimeSeconds(time: String): Long = try {
    // 日志中没有年份，补充一个固定年份以支持跨月/跨天计算
    val fullTime = "2026-$time"
    LocalDateTime.parse(fullTime, timeFormatter).toEpochSecond(ZoneOffset.UTC)
} catch (_: Exception) {
    0L
}

// 绘制一条平滑曲线的 Path
fun DrawScope.drawLogPathZone(values: List<Float>, sp: Float, zoneH: Float, baseY: Float, color: Color) {
    val path = Path()
    values.forEachIndexed { i, v ->
        val x = i * sp; val y = baseY + zoneH - (v * zoneH)
        if (i == 0) path.moveTo(x, y)
        else { val px = (i - 1) * sp; val py = baseY + zoneH - (values[i - 1] * zoneH); path.cubicTo((px + x) / 2, py, (px + x) / 2, y, x, y) }
    }
    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// 绘制曲线下方的填充区域
fun DrawScope.drawLogFillZone(values: List<Float>, sp: Float, zoneH: Float, baseY: Float, color: Color) {
    val path = Path()
    values.forEachIndexed { i, v ->
        val x = i * sp; val y = baseY + zoneH - (v * zoneH)
        if (i == 0) path.moveTo(x, y)
        else { val px = (i - 1) * sp; val py = baseY + zoneH - (values[i - 1] * zoneH); path.cubicTo((px + x) / 2, py, (px + x) / 2, y, x, y) }
    }
    val lastX = (values.size - 1) * sp
    path.lineTo(lastX, baseY + zoneH)
    path.lineTo(0f, baseY + zoneH)
    path.close()
    drawPath(path = path, color = color)
}

// ── 预计算数据结构（一次性计算，避免 Canvas 内重复） ──

private data class ChartData(
    val timeSeconds: List<Long>,
    val wattNorm: List<Float>,
    val levelNorm: List<Float>,
    val tempNorm: List<Float>
)

@Composable
private fun rememberChartData(points: List<ModuleDetector.LogDataPoint>, maxWatt: Float, maxTemp: Float): ChartData = remember(points, maxWatt, maxTemp) {
    val timeSeconds = points.map { parseTimeSeconds(it.time) }
    val wattNorm = points.map { (it.watt / maxWatt).coerceIn(0f, 1f) }
    val levelNorm = points.map { it.level / 100f }
    val tempNorm = points.map { (it.temp / maxTemp).coerceIn(0f, 1f) }
    ChartData(timeSeconds = timeSeconds, wattNorm = wattNorm, levelNorm = levelNorm, tempNorm = tempNorm)
}

@Composable
fun LogLineChart(points: List<ModuleDetector.LogDataPoint>, isZh: Boolean, showWatt: Boolean, showLevel: Boolean, showTemp: Boolean, isCharging: Boolean = false, onlyWattMode: Boolean = false) {
    val wattColor = ColorWatt
    val levelColor = ColorLevel
    val tempColor = ColorTemp
    val primaryColor = MiuixTheme.colorScheme.primary
    val gridLineColor = MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.15f)
    val yAxisTextColor = MiuixTheme.colorScheme.onSurfaceSecondary
    val yAxisWidth = 32.dp           // Y 轴标尺宽度
    val yAxisLeftPadding = 8.dp      // Y 轴左侧间距，与时间标尺同步
    
    val maxWattPoint = remember(points) { points.maxOfOrNull { it.watt } ?: 0f }
    val maxWatt = remember(onlyWattMode, maxWattPoint) { if (onlyWattMode) 100f else maxOf(60f, kotlin.math.ceil(maxWattPoint / 20f).toInt() * 20f) }
    val maxTemp = 100f

    // ── 预计算所有数据，避免 Canvas 内重复计算 ──
    val chartData = rememberChartData(points, maxWatt, maxTemp)
    
    var touchIndex by remember { mutableStateOf(-1) }
    val animatedTouchIndex = remember { Animatable(-1f) }
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(touchIndex) {
        if (touchIndex == -1) {
            animatedAlpha.animateTo(0f, animationSpec = tween(150))
            animatedTouchIndex.snapTo(-1f)
        } else {
            val wasHidden = animatedTouchIndex.value < 0f
            if (wasHidden) {
                animatedTouchIndex.snapTo(0f)
                animatedAlpha.animateTo(1f, animationSpec = tween(200))
                animatedTouchIndex.animateTo(
                    targetValue = touchIndex.toFloat(),
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
            } else {
                animatedTouchIndex.snapTo(touchIndex.toFloat())
                animatedAlpha.snapTo(1f)
            }
        }
    }

    val baseTimeSeconds = chartData.timeSeconds.firstOrNull() ?: 0L
    val endTimeSeconds = chartData.timeSeconds.lastOrNull() ?: 0L
    val totalDurationMinutes = ((endTimeSeconds - baseTimeSeconds) / 60).coerceAtLeast(0L)
    val averageWatt = remember(points) { points.map { it.watt }.average().toFloat() }
    val averageTemp = remember(points) { points.map { it.temp }.average().toFloat() }
    val minLevel = remember(points) { points.minOfOrNull { it.level.toInt() } ?: 0 }
    val maxLevel = remember(points) { points.maxOfOrNull { it.level.toInt() } ?: 0 }
    val isTouching = touchIndex in points.indices

    // ── markerData：使用预计算的 timeSeconds，不再重复调用 parseTimeSeconds ──
    val markerData = remember(points, chartData.timeSeconds, baseTimeSeconds, isCharging) {
        val data = mutableListOf<Pair<Int, String>>()
        if (points.isEmpty()) return@remember data
        val timeSeconds = chartData.timeSeconds
        val charge80Index = points.indexOfFirst { it.level >= 80f }
        if (charge80Index == -1) {
            var lastM = -1L
            val lastIdx = points.lastIndex
            var lastIdxAdded = false
            points.indices.forEach { index ->
                val m = (timeSeconds[index] - baseTimeSeconds) / 60
                if (lastM == -1L || m - lastM >= 10) {
                    data.add(index to "${m}m")
                    lastM = m
                    if (index == lastIdx) lastIdxAdded = true
                }
            }
            // 最后一个点如果还没被加入（距离上一个标记不足10分钟），按间隙处理
            if (!lastIdxAdded) {
                val m = (timeSeconds[lastIdx] - baseTimeSeconds) / 60
                val lastMark = data.lastOrNull()
                // 间隙 < 3 分钟：替换上一个标记（如 12m 替换 10m、21m 替换 20m）
                // 间隙 >= 3 分钟：保留两者（如 23m 和 20m 并存）
                if (lastMark != null && m - lastM < 3) {
                    data[data.lastIndex] = lastIdx to "${m}m"
                } else {
                    data.add(lastIdx to "${m}m")
                }
            }
        } else {
            var lastM = -1L
            // 80%之前正常每10分钟标记，80%后停止
            points.indices.forEach { index ->
                if (index < charge80Index) {
                    val m = (timeSeconds[index] - baseTimeSeconds) / 60
                    if (lastM == -1L || m - lastM >= 10) {
                        data.add(index to "${m}m")
                        lastM = m
                    }
                }
            }
            // 到达80%时标记：距离上一个标记 ≤5 分钟则替换，>5 分钟则追加
            val m80 = (timeSeconds[charge80Index] - baseTimeSeconds) / 60
            if (data.isNotEmpty()) {
                val lastMarkM = (timeSeconds[data.last().first] - baseTimeSeconds) / 60
                if (m80 - lastMarkM <= 5) {
                    data[data.lastIndex] = charge80Index to "${m80}m"
                } else {
                    data.add(charge80Index to "${m80}m")
                }
            } else {
                data.add(charge80Index to "${m80}m")
            }
            val lastIdx = points.lastIndex
            val fullIdx = points.indexOfLast { it.level >= 100f }
            val hasFull = fullIdx > charge80Index
            // 到达100%时标记一次
            if (hasFull) {
                val m = (timeSeconds[fullIdx] - baseTimeSeconds) / 60
                data.add(fullIdx to "${m}m")
            }
            // 末尾点：100% 标记不可替换，其余间隙 < 3 分钟则替换上一个
            if (data.none { it.first == lastIdx }) {
                val m = (timeSeconds[lastIdx] - baseTimeSeconds) / 60
                val lastMark = data.lastOrNull()
                if (lastMark != null) {
                    val isLastFull = hasFull && lastMark.first == fullIdx
                    if (!isLastFull) {
                        val lastMarkM = (timeSeconds[lastMark.first] - baseTimeSeconds) / 60
                        if (m - lastMarkM < 3) {
                            data[data.lastIndex] = lastIdx to "${m}m"
                        } else {
                            data.add(lastIdx to "${m}m")
                        }
                    } else {
                        data.add(lastIdx to "${m}m")
                    }
                } else {
                    data.add(lastIdx to "${m}m")
                }
            }
        }
        data
    }

    // ── 时间轴刻度标记（每10分钟整点，充电时80%后停止，100%再生一次） ──
    val timeAxisMarkers = remember(points, chartData.timeSeconds, baseTimeSeconds, totalDurationMinutes) {
        val data = mutableListOf<Pair<Float, String>>()
        if (points.size < 2) return@remember data
        val timeSeconds = chartData.timeSeconds
        val step = 10L
        val charge80Idx = points.indexOfFirst { it.level >= 80f }

        if (charge80Idx == -1) {
            // 无充电：正常生成每10分钟刻度
            for (minute in step..<totalDurationMinutes step step) {
                val targetSeconds = baseTimeSeconds + minute * 60
                var idx = timeSeconds.indexOfFirst { it >= targetSeconds }
                if (idx <= 0) continue
                val prevSec = timeSeconds[idx - 1]
                val nextSec = timeSeconds[idx]
                if (nextSec == prevSec) continue
                val frac = (targetSeconds - prevSec).toFloat() / (nextSec - prevSec).toFloat()
                val fracIndex = (idx - 1) + frac
                data.add(fracIndex to "${minute}m")
            }
        } else {
            // 充电：80%之前正常生成每10分钟刻度，80%后停止
            val charge80Time = timeSeconds[charge80Idx]
            for (minute in step..<totalDurationMinutes step step) {
                val targetSeconds = baseTimeSeconds + minute * 60
                if (targetSeconds >= charge80Time) break
                var idx = timeSeconds.indexOfFirst { it >= targetSeconds }
                if (idx <= 0) continue
                val prevSec = timeSeconds[idx - 1]
                val nextSec = timeSeconds[idx]
                if (nextSec == prevSec) continue
                val frac = (targetSeconds - prevSec).toFloat() / (nextSec - prevSec).toFloat()
                val fracIndex = (idx - 1) + frac
                data.add(fracIndex to "${minute}m")
            }
            // 到达80%时生成刻度：距离上一个 ≤5 分钟则替换，>5 分钟则追加
            val m80 = (timeSeconds[charge80Idx] - baseTimeSeconds) / 60
            if (data.isNotEmpty()) {
                val lastLabel = data.last().second
                val lastM = lastLabel.substringBefore("m").toLongOrNull() ?: 0L
                if (m80 - lastM <= 5) {
                    data[data.lastIndex] = charge80Idx.toFloat() to "${m80}m"
                } else {
                    data.add(charge80Idx.toFloat() to "${m80}m")
                }
            } else {
                data.add(charge80Idx.toFloat() to "${m80}m")
            }
            // 到达100%时生成一次刻度
            val fullIdx = points.indexOfLast { it.level >= 100f }
            if (fullIdx > charge80Idx) {
                val m = (timeSeconds[fullIdx] - baseTimeSeconds) / 60
                data.add(fullIdx.toFloat() to "${m}m")
            }
        }
        data
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        // ── 摘要卡片 ──
        run {
            val animatedIndexValue = animatedTouchIndex.value
            val isAnimatingTouch = animatedIndexValue >= 0f
            val activeIndex = if (isAnimatingTouch) animatedIndexValue.toInt().coerceIn(points.indices) else points.lastIndex
            val durationValue = if (isAnimatingTouch) {
                "${((chartData.timeSeconds[activeIndex] - baseTimeSeconds) / 60).coerceAtLeast(0L)}m"
            } else {
                "${totalDurationMinutes}m"
            }
            val wattValue = if (isAnimatingTouch) "%.1f W".format(points[activeIndex].watt) else "%.1f W".format(averageWatt)
            val levelValue = "${minLevel}%→${maxLevel}%"
            val tempValue = if (isAnimatingTouch) "${points[activeIndex].temp.toInt()}°C" else "${averageTemp.toInt()}°C"
            val shouldCollapse = isTouching || isCharging || onlyWattMode
            val summaryTransition = updateTransition(targetState = shouldCollapse, label = "log_summary_transition")
            val durationWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "duration_weight"
            ) { state -> if (state && onlyWattMode && !isTouching && !isCharging) 1f else if (state) 4f else 1f }
            val wattWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "watt_weight"
            ) { state -> if (state && onlyWattMode && !isTouching && !isCharging) 1f else if (state) 0.001f else 0.9f }
            val wattAlpha by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
                label = "watt_alpha"
            ) { state -> if (state && onlyWattMode && !isTouching && !isCharging) 1f else if (state) 0f else 1f }
            val wattScale by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "watt_scale"
            ) { state -> if (state && onlyWattMode && !isTouching && !isCharging) 1f else if (state) 0.82f else 1f }
            val compactOtherWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "compact_other_weight"
            ) { state -> if (state) 0.001f else 0.9f }
            val levelWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "level_weight"
            ) { state -> if (state) 0.001f else 1.2f }
            val otherAlpha by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
                label = "other_alpha"
            ) { state -> if (state) 0f else 1f }
            val otherScale by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "other_scale"
            ) { state -> if (state) 0.82f else 1f }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(durationWeight), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(durationValue, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "时长" else "Dur", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(wattWeight).graphicsLayer { alpha = wattAlpha; scaleX = wattScale; scaleY = wattScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(wattValue, fontSize = 14.sp, color = wattColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) { if (onlyWattMode && isTouching) "实时功耗" else "平均功耗" } else { if (onlyWattMode && isTouching) "Watt" else "Avg Watt" }, fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(levelWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(levelValue, fontSize = 14.sp, color = levelColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "电量" else "Bat", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(compactOtherWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tempValue, fontSize = 14.sp, color = tempColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "平均温度" else "Avg Temp", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }
        }

        // ── 模式切换过渡动画 ──
        val modeTransition by animateFloatAsState(
            targetValue = if (onlyWattMode) 1f else 0f,
            animationSpec = tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
            label = "mode_transition"
        )

        // ── 图表区域（统一布局） ──
        Row(modifier = Modifier.fillMaxWidth().height(220.dp).padding(start = yAxisLeftPadding, end = 28.dp)) {
            // ── 左侧 Y 轴瓦数标尺（动画淡入） ──
            val yAxisPaint = remember { Paint().apply { textSize = 24f; textAlign = Paint.Align.RIGHT } }
            Canvas(modifier = Modifier.width(yAxisWidth).fillMaxSize()) {
                val h = size.height
                val rightEdge = size.width - 8.dp.toPx()
                yAxisPaint.color = yAxisTextColor.copy(alpha = modeTransition).toArgb()
                for (i in 0..4) {
                    val value = (i * 25).toInt()
                    val y = h - (i.toFloat() / 4 * h)
                    drawContext.canvas.nativeCanvas.drawText("${value}W", rightEdge, y + 4.dp.toPx(), yAxisPaint)
                }
            }
            // ── 右侧图表 ──
            Box(modifier = Modifier.weight(1f).fillMaxSize().pointerInput(points) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val sp = size.width / (points.size.coerceAtLeast(2) - 1)
                        touchIndex = (offset.x / sp).toInt().coerceIn(points.indices)
                    },
                    onDrag = { change, _ ->
                        val sp = size.width / (points.size.coerceAtLeast(2) - 1)
                        touchIndex = (change.position.x / sp).toInt().coerceIn(points.indices)
                        change.consume()
                    },
                    onDragEnd = { touchIndex = -1 },
                    onDragCancel = { touchIndex = -1 }
                )
            }) {
                val markerPaint = remember { Paint().apply { textSize = 32f; typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER } }
                val touchPaint = remember { Paint().apply { textSize = 34f; typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT } }
                val density = LocalDensity.current

                // ── 电量标签重叠检测 ──
                val levelLabelBelow = remember(showLevel, chartData.levelNorm, density) {
                    if (!showLevel || chartData.levelNorm.isEmpty()) return@remember false
                    val chartHeightPx = with(density) { 220.dp.toPx() }
                    val zoneHPx = chartHeightPx * 0.25f
                    val px10 = with(density) { 10.dp.toPx() }
                    val px22 = with(density) { 22.dp.toPx() }
                    chartData.levelNorm.any { v ->
                        val y = (1f - v) * zoneHPx
                        y - px10 < 0f || y + px22 > zoneHPx
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width; val h = size.height
                    if (points.size < 2) return@Canvas
                    val sp = w / (points.size - 1)

                    // ── 绘制网格线 ──
                    for (i in 0..4) { drawLine(gridLineColor, Offset(0f, h - (i.toFloat() / 4 * h)), Offset(w, h - (i.toFloat() / 4 * h)), strokeWidth = 0.5.dp.toPx()) }

                    val zoneH = h * 0.25f
                    val gap = h * 0.125f
                    val levelBase = 0f
                    val wattBaseNormal = zoneH + gap
                    val tempBase = 2f * (zoneH + gap)

                    val t = modeTransition
                    val wattBaseY = wattBaseNormal + (0f - wattBaseNormal) * t
                    val wattZoneH = zoneH + (h - zoneH) * t

                    // ── 功耗曲线下方填充（淡入） ──
                    if (t > 0.001f) {
                        drawLogFillZone(chartData.wattNorm, sp, wattZoneH, wattBaseY, wattColor.copy(alpha = 0.08f * t))
                    }

                    // ── 绘制曲线 ──
                    if (showWatt) drawLogPathZone(chartData.wattNorm, sp, wattZoneH, wattBaseY, wattColor)

                    val otherAlpha = (1f - t).coerceIn(0f, 1f)
                    if (showLevel && otherAlpha > 0.001f) {
                        drawLogPathZone(chartData.levelNorm, sp, zoneH, levelBase, levelColor.copy(alpha = otherAlpha))
                    }
                    if (showTemp && otherAlpha > 0.001f) {
                        drawLogPathZone(chartData.tempNorm, sp, zoneH, tempBase, tempColor.copy(alpha = otherAlpha))
                    }

                    val animatedIndex = animatedTouchIndex.value

                    // ── 绘制 marker 竖线 ──
                    markerData.forEach { (index, _) ->
                        val x = index * sp
                        drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8.dp.toPx())

                        // 所有标记点随模式过渡淡出
                        if (otherAlpha <= 0.001f) return@forEach

                        val p = points[index]
                        val markerAlpha = otherAlpha

                        // 功耗 marker
                        if (showWatt) {
                            val y = wattBaseY + (1f - chartData.wattNorm[index]) * wattZoneH
                            drawCircle(wattColor.copy(alpha = markerAlpha), radius = 4.5.dp.toPx(), center = Offset(x, y))
                            if (animatedIndex < 0f) {
                                markerPaint.color = wattColor.copy(alpha = markerAlpha).toArgb()
                                drawContext.canvas.nativeCanvas.drawText("%.1fW".format(p.watt), x, y + 22.dp.toPx(), markerPaint)
                            }
                        }
                        // 温度 marker
                        if (showTemp) {
                            val y = tempBase + (1f - chartData.tempNorm[index]) * zoneH
                            drawCircle(tempColor.copy(alpha = markerAlpha), radius = 4.5.dp.toPx(), center = Offset(x, y))
                            if (animatedIndex < 0f) {
                                markerPaint.color = tempColor.copy(alpha = markerAlpha).toArgb()
                                drawContext.canvas.nativeCanvas.drawText("${p.temp.toInt()}°", x, y - 10.dp.toPx(), markerPaint)
                            }
                        }
                        // 电量 marker
                        if (showLevel) {
                            val y = levelBase + (1f - chartData.levelNorm[index]) * zoneH
                            drawCircle(levelColor.copy(alpha = markerAlpha), radius = 4.5.dp.toPx(), center = Offset(x, y))
                            if (animatedIndex < 0f) {
                                markerPaint.color = levelColor.copy(alpha = markerAlpha).toArgb()
                                val labelY = if (levelLabelBelow) y + 22.dp.toPx() else y - 10.dp.toPx()
                                drawContext.canvas.nativeCanvas.drawText("${p.level.toInt()}%", x, labelY, markerPaint)
                            }
                        }
                    }

                    // ── 触摸指示器 ──
                    val alpha = animatedAlpha.value
                    if (animatedIndex >= 0f && animatedIndex <= points.lastIndex) {
                        val x = animatedIndex * sp
                        val p = points[animatedIndex.toInt().coerceIn(points.indices)]
                        val idx = animatedIndex.toInt().coerceIn(points.indices)
                        drawLine(primaryColor.copy(alpha = alpha), Offset(x, 0f), Offset(x, h), strokeWidth = 1.5.dp.toPx())
                        val textOffsetX = 12.dp.toPx()
                        val rightLimit = w - 8.dp.toPx()

                        // 功耗触摸（始终绘制）
                        if (showWatt) {
                            val value = "%.1fW".format(p.watt)
                            val y = wattBaseY + (1f - chartData.wattNorm[idx]) * wattZoneH
                            drawCircle(wattColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                            touchPaint.color = wattColor.copy(alpha = alpha).toArgb()
                            val desiredX = x + textOffsetX
                            val textWidth = touchPaint.measureText(value)
                            val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                            drawContext.canvas.nativeCanvas.drawText(value, tx, y + 22.dp.toPx(), touchPaint)
                        }
                        // 温度触摸（过渡时淡出）
                        if (showTemp && otherAlpha > 0.001f) {
                            val value = "${p.temp.toInt()}°"
                            val y = tempBase + (1f - chartData.tempNorm[idx]) * zoneH
                            val touchAlpha = alpha * otherAlpha
                            drawCircle(tempColor.copy(alpha = touchAlpha), radius = 5.dp.toPx(), center = Offset(x, y))
                            touchPaint.color = tempColor.copy(alpha = touchAlpha).toArgb()
                            val desiredX = x + textOffsetX
                            val textWidth = touchPaint.measureText(value)
                            val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                            drawContext.canvas.nativeCanvas.drawText(value, tx, y - 12.dp.toPx(), touchPaint)
                        }
                        // 电量触摸（过渡时淡出）
                        if (showLevel && otherAlpha > 0.001f) {
                            val value = "${p.level.toInt()}%"
                            val y = levelBase + (1f - chartData.levelNorm[idx]) * zoneH
                            val touchAlpha = alpha * otherAlpha
                            drawCircle(levelColor.copy(alpha = touchAlpha), radius = 5.dp.toPx(), center = Offset(x, y))
                            touchPaint.color = levelColor.copy(alpha = touchAlpha).toArgb()
                            val labelY = if (levelLabelBelow) y + 22.dp.toPx() else y - 12.dp.toPx()
                            val desiredX = x + textOffsetX
                            val textWidth = touchPaint.measureText(value)
                            val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                            drawContext.canvas.nativeCanvas.drawText(value, tx, labelY, touchPaint)
                        }
                    }
                }
            }
        }

        // ── 底部时间轴（Canvas，与图表同宽，刻度标签与 0m/total 并排） ──
        val timeLabelPaint = remember { Paint().apply { textSize = 26f } }
        val timeTextColor = MiuixTheme.colorScheme.onSurfaceSecondary
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = yAxisLeftPadding + yAxisWidth, end = 28.dp)
                .height(20.dp)
        ) {
            if (points.size < 2) return@Canvas
            val w = size.width
            val sp = w / (points.size - 1)
            val textY = size.height - 4.dp.toPx()

            // 0m — 左对齐
            timeLabelPaint.textAlign = Paint.Align.LEFT
            timeLabelPaint.color = timeTextColor.toArgb()
            drawContext.canvas.nativeCanvas.drawText("0m", -6.dp.toPx(), textY, timeLabelPaint)

            // 总时长 — 右对齐
            timeLabelPaint.textAlign = Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText("${totalDurationMinutes}m", w, textY, timeLabelPaint)

            // 刻度标签 — 居中，跳过于 0m / 总时长过近的标签
            timeLabelPaint.textAlign = Paint.Align.CENTER
            timeLabelPaint.color = timeTextColor.toArgb()
            val edgeThreshold = 30.dp.toPx()
            val totalRight = w - edgeThreshold
            timeAxisMarkers.forEach { (fracIndex, label) ->
                val x = fracIndex * sp
                if (x < edgeThreshold || x > totalRight) return@forEach
                drawContext.canvas.nativeCanvas.drawText(label, x, textY, timeLabelPaint)
            }
        }
    }
}