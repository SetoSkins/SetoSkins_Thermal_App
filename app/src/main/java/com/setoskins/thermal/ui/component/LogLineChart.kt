package com.setoskins.thermal.ui.component

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
internal fun parseTimeSeconds(time: String): Long = try {
    val p = time.substringAfter(" ").split(":")
    p[0].toLong() * 3600 + p[1].toLong() * 60 + p[2].toLong()
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

// ── 预计算数据结构（一次性计算，避免 Canvas 内重复） ──

private data class ChartData(
    val timeSeconds: List<Long>,
    val wattNorm: List<Float>,
    val levelNorm: List<Float>,
    val tempNorm: List<Float>,
    val levelLabelBelow: Boolean
)

@Composable
private fun rememberChartData(points: List<ModuleDetector.LogDataPoint>, maxWatt: Float, maxTemp: Float): ChartData = remember(points, maxWatt, maxTemp) {
    val timeSeconds = points.map { parseTimeSeconds(it.time) }
    val wattNorm = points.map { (it.watt / maxWatt).coerceIn(0f, 1f) }
    val levelNorm = points.map { it.level / 100f }
    val tempNorm = points.map { (it.temp / maxTemp).coerceIn(0f, 1f) }
    ChartData(timeSeconds = timeSeconds, wattNorm = wattNorm, levelNorm = levelNorm, tempNorm = tempNorm, levelLabelBelow = false) // levelLabelBelow 需要在 Canvas 中计算（依赖像素坐标）
}

@Composable
fun LogLineChart(points: List<ModuleDetector.LogDataPoint>, isZh: Boolean, showWatt: Boolean, showLevel: Boolean, showTemp: Boolean, isCharging: Boolean = false) {
    val wattColor = ColorWatt
    val levelColor = ColorLevel
    val tempColor = ColorTemp
    val primaryColor = MiuixTheme.colorScheme.primary
    val gridLineColor = MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.15f)
    
    val maxWattPoint = points.maxOfOrNull { it.watt } ?: 0f
    val maxWatt = maxOf(60f, kotlin.math.ceil(maxWattPoint / 20f).toInt() * 20f)
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
        val charge95Index = points.indexOfFirst { it.level >= 95f }
        if (charge95Index == -1) {
            var lastM = -1L
            points.indices.forEach { index ->
                val m = (timeSeconds[index] - baseTimeSeconds) / 60
                if (lastM == -1L || m - lastM >= 10) {
                    data.add(index to "${m}m")
                    lastM = m
                }
            }
        } else {
            var lastM = -1L
            points.indices.forEach { index ->
                if (index < charge95Index) {
                    val m = (timeSeconds[index] - baseTimeSeconds) / 60
                    if (lastM == -1L || m - lastM >= 10) {
                        data.add(index to "${m}m")
                        lastM = m
                    }
                }
            }
            val lastIdx = points.lastIndex
            if (!isCharging) {
                val m = (timeSeconds[lastIdx] - baseTimeSeconds) / 60
                data.add(lastIdx to "${m}m")
            } else if (points[lastIdx].level >= 100f) {
                val fullIdx = points.indexOfLast { it.level >= 100f }
                if (fullIdx >= charge95Index) {
                    val m = (timeSeconds[fullIdx] - baseTimeSeconds) / 60
                    data.add(fullIdx to "${m}m")
                }
            }
        }
        data
    }

    // ── 时间轴刻度标记（每10分钟整点，充电时95%后停止，100%再生一次） ──
    val timeAxisMarkers = remember(points, chartData.timeSeconds, baseTimeSeconds, totalDurationMinutes) {
        val data = mutableListOf<Pair<Float, String>>()
        if (points.size < 2) return@remember data
        val timeSeconds = chartData.timeSeconds
        val step = 10L
        val charge95Index = points.indexOfFirst { it.level >= 95f }

        if (charge95Index == -1) {
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
            // 充电：95%之前正常生成每10分钟刻度，95%后停止
            val charge95Time = timeSeconds[charge95Index]
            for (minute in step..<totalDurationMinutes step step) {
                val targetSeconds = baseTimeSeconds + minute * 60
                if (targetSeconds >= charge95Time) break
                var idx = timeSeconds.indexOfFirst { it >= targetSeconds }
                if (idx <= 0) continue
                val prevSec = timeSeconds[idx - 1]
                val nextSec = timeSeconds[idx]
                if (nextSec == prevSec) continue
                val frac = (targetSeconds - prevSec).toFloat() / (nextSec - prevSec).toFloat()
                val fracIndex = (idx - 1) + frac
                data.add(fracIndex to "${minute}m")
            }
            // 到达100%时生成一次刻度
            val fullIdx = points.indexOfLast { it.level >= 100f }
            if (fullIdx > charge95Index) {
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
            val levelValue = "${minLevel}% → ${maxLevel}%"
            val tempValue = if (isAnimatingTouch) "${points[activeIndex].temp.toInt()}°C" else "${averageTemp.toInt()}°C"
            val shouldCollapse = isTouching || isCharging
            val summaryTransition = updateTransition(targetState = shouldCollapse, label = "log_summary_transition")
            val durationWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "duration_weight"
            ) { touching -> if (touching) 4f else 1f }
            val compactOtherWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "compact_other_weight"
            ) { touching -> if (touching) 0.001f else 0.9f }
            val levelWeight by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "level_weight"
            ) { touching -> if (touching) 0.001f else 1.2f }
            val otherAlpha by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 320, easing = FastOutSlowInEasing) },
                label = "other_alpha"
            ) { touching -> if (touching) 0f else 1f }
            val otherScale by summaryTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 420, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                label = "other_scale"
            ) { touching -> if (touching) 0.82f else 1f }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(durationWeight), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(durationValue, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "时长" else "Dur", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(compactOtherWeight).graphicsLayer { alpha = otherAlpha; scaleX = otherScale; scaleY = otherScale }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(wattValue, fontSize = 14.sp, color = wattColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                            Text(if (isZh) "平均功耗" else "Avg Watt", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, textAlign = TextAlign.Center, maxLines = 1)
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

        // ── 图表区域 ──
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 28.dp).pointerInput(points) {
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
            // ── Paint 对象复用：只创建一次 ──
            val markerPaint = remember { Paint().apply { textSize = 32f; typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER } }
            val touchPaint = remember { Paint().apply { textSize = 34f; typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT } }
            val timeLabelPaint = remember { Paint().apply { textSize = 26f; textAlign = Paint.Align.CENTER } }
            val onSurfaceSecondaryColor = MiuixTheme.colorScheme.onSurfaceSecondary
            val density = LocalDensity.current

            // ── 电量标签重叠检测：预计算，避免 Canvas 内每帧遍历 ──
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
                val zoneH = h * 0.25f
                val gap = h * 0.125f
                val levelBase = 0f
                val tempBase = zoneH + gap
                val wattBase = 2f * (zoneH + gap)
                val usableZoneH = zoneH

                // ── 绘制网格线 ──
                for (i in 0..4) { drawLine(gridLineColor, Offset(0f, h - (i.toFloat() / 4 * h)), Offset(w, h - (i.toFloat() / 4 * h)), strokeWidth = 0.5.dp.toPx()) }

                // ── 绘制曲线（使用预计算的数据） ──
                if (showWatt) drawLogPathZone(chartData.wattNorm, sp, usableZoneH, wattBase, wattColor)
                if (showLevel) drawLogPathZone(chartData.levelNorm, sp, usableZoneH, levelBase, levelColor)
                if (showTemp) drawLogPathZone(chartData.tempNorm, sp, usableZoneH, tempBase, tempColor)

                val animatedIndex = animatedTouchIndex.value
                val lastRealTimeX = if (points.isNotEmpty()) points.lastIndex * sp else -1f

                // ── 绘制 marker 数据点 ──
                markerData.forEach { (index, _) ->
                    val x = index * sp
                    if (isCharging && lastRealTimeX >= 0f && kotlin.math.abs(x - lastRealTimeX) < 30.dp.toPx()) return@forEach
                    drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8.dp.toPx())
                    val p = points[index]

                    if (showWatt) {
                        val y = wattBase + (1f - chartData.wattNorm[index]) * usableZoneH
                        drawCircle(wattColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            markerPaint.color = wattColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("%.1fW".format(p.watt), x, y - 10.dp.toPx(), markerPaint)
                        }
                    }
                    if (showTemp) {
                        val y = tempBase + (1f - chartData.tempNorm[index]) * usableZoneH
                        drawCircle(tempColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            markerPaint.color = tempColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.temp.toInt()}°", x, y + 22.dp.toPx(), markerPaint)
                        }
                    }
                    if (showLevel) {
                        val y = levelBase + (1f - chartData.levelNorm[index]) * usableZoneH
                        drawCircle(levelColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (animatedIndex < 0f) {
                            markerPaint.color = levelColor.toArgb()
                            val labelY = if (levelLabelBelow) y + 22.dp.toPx() else y - 10.dp.toPx()
                            drawContext.canvas.nativeCanvas.drawText("${p.level.toInt()}%", x, labelY, markerPaint)
                        }
                    }
                }

                // ── 充电时始终显示最右边数据点 ──
                if (isCharging && animatedIndex < 0f && points.isNotEmpty()) {
                    val lastIdx = points.lastIndex
                    val isLastPointMarker = markerData.any { it.first == lastIdx }
                    val x = lastIdx * sp
                    val p = points[lastIdx]
                    if (showWatt) {
                        val y = wattBase + (1f - chartData.wattNorm[lastIdx]) * usableZoneH
                        drawCircle(wattColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (!isLastPointMarker) {
                            markerPaint.color = wattColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("%.1fW".format(p.watt), x, y - 10.dp.toPx(), markerPaint)
                        }
                    }
                    if (showTemp) {
                        val y = tempBase + (1f - chartData.tempNorm[lastIdx]) * usableZoneH
                        drawCircle(tempColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (!isLastPointMarker) {
                            markerPaint.color = tempColor.toArgb()
                            drawContext.canvas.nativeCanvas.drawText("${p.temp.toInt()}°", x, y + 22.dp.toPx(), markerPaint)
                        }
                    }
                    if (showLevel) {
                        val y = levelBase + (1f - chartData.levelNorm[lastIdx]) * usableZoneH
                        drawCircle(levelColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
                        if (!isLastPointMarker) {
                            markerPaint.color = levelColor.toArgb()
                            val labelY = if (levelLabelBelow) y + 22.dp.toPx() else y - 10.dp.toPx()
                            drawContext.canvas.nativeCanvas.drawText("${p.level.toInt()}%", x, labelY, markerPaint)
                        }
                    }
                }

                // ── 时间轴刻度标签（每10分钟，不与实时数据点标记重叠） ──
                if (timeAxisMarkers.isNotEmpty()) {
                    val overlapThreshold = 30.dp.toPx()
                    timeAxisMarkers.forEach { (fracIndex, label) ->
                        val x = fracIndex * sp
                        // 检查是否与已有实时 markerData 位置重叠，优先保留实时标记
                        val tooClose = markerData.any { (markerIdx, _) ->
                            kotlin.math.abs(markerIdx * sp - x) < overlapThreshold
                        }
                        if (tooClose) return@forEach
                        timeLabelPaint.color = onSurfaceSecondaryColor.copy(alpha = 0.45f).toArgb()
                        drawContext.canvas.nativeCanvas.drawText(label, x, h - 4.dp.toPx(), timeLabelPaint)
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

                    if (showWatt) {
                        val value = "%.1fW".format(p.watt)
                        val y = wattBase + (1f - chartData.wattNorm[idx]) * usableZoneH
                        drawCircle(wattColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        touchPaint.color = wattColor.copy(alpha = alpha).toArgb()
                        val desiredX = x + textOffsetX
                        val textWidth = touchPaint.measureText(value)
                        val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                        drawContext.canvas.nativeCanvas.drawText(value, tx, y - 12.dp.toPx(), touchPaint)
                    }
                    if (showTemp) {
                        val value = "${p.temp.toInt()}°"
                        val y = tempBase + (1f - chartData.tempNorm[idx]) * usableZoneH
                        drawCircle(tempColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        touchPaint.color = tempColor.copy(alpha = alpha).toArgb()
                        val desiredX = x + textOffsetX
                        val textWidth = touchPaint.measureText(value)
                        val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                        drawContext.canvas.nativeCanvas.drawText(value, tx, y + 22.dp.toPx(), touchPaint)
                    }
                    if (showLevel) {
                        val value = "${p.level.toInt()}%"
                        val y = levelBase + (1f - chartData.levelNorm[idx]) * usableZoneH
                        drawCircle(levelColor.copy(alpha = alpha), radius = 5.dp.toPx(), center = Offset(x, y))
                        touchPaint.color = levelColor.copy(alpha = alpha).toArgb()
                        val labelY = if (levelLabelBelow) y + 22.dp.toPx() else y - 12.dp.toPx()
                        val desiredX = x + textOffsetX
                        val textWidth = touchPaint.measureText(value)
                        val tx = if (desiredX + textWidth <= rightLimit) desiredX else (x - textOffsetX - textWidth).coerceAtLeast(8.dp.toPx())
                        drawContext.canvas.nativeCanvas.drawText(value, tx, labelY, touchPaint)
                    }
                }
            }
        }

        // ── 底部时间轴 ──
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0m", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
            if (points.isNotEmpty()) {
                Text("${totalDurationMinutes}m", fontSize = 10.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
            }
        }
    }
}