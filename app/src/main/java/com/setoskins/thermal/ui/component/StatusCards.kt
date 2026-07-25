package com.setoskins.thermal.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun YellowUpdateCard() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) ColorYellowBgDark else ColorYellowBgLight
    val titleColor = if (isDark) ColorYellowTitleDark else ColorYellowTitleLight
    val subColor = if (isDark) ColorYellowSubDark else ColorYellowSubLight
    val iconColor = if (isDark) ColorYellowIconDark else ColorYellowIconLight
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "检测到更新", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor); Spacer(modifier = Modifier.height(4.dp)); Text(text = "新版本已发布,点击去下载", fontSize = 14.sp, color = subColor) }
            Canvas(modifier = Modifier.size(32.dp)) {
                val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center)
                val arrowPath = Path().apply { moveTo(center.x, center.y - 6.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); moveTo(center.x - 4.dp.toPx(), center.y + 2.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); lineTo(center.x + 4.dp.toPx(), center.y + 2.dp.toPx()) }
                drawPath(path = arrowPath, color = iconColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
fun RedNotInstalledCard() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) ColorRedBgDark else ColorRedBgLight
    val titleColor = if (isDark) ColorRedTitleDark else ColorRedTitleLight
    val subColor = if (isDark) ColorRedSubDark else ColorRedSubLight
    val iconColor = if (isDark) ColorRedIconDark else ColorRedIconLight
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "未激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor); Spacer(modifier = Modifier.height(4.dp)); Text(text = "模块未安装", fontSize = 14.sp, color = subColor) }
            Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center); drawLine(color = iconColor, start = Offset(center.x, center.y - 6.dp.toPx()), end = Offset(center.x, center.y + 2.dp.toPx()), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round); drawCircle(color = iconColor, radius = 1.5.dp.toPx(), center = Offset(center.x, center.y + 7.dp.toPx())) }
        }
    }
}

@Composable
fun GreenActivatedCard(useMonet: Boolean, version: String) {
    val isDark = isSystemInDarkTheme(); val miuixColors = MiuixTheme.colorScheme
    val greenAccent = if (isDark) ColorGreenAccentDark else ColorGreenAccentLight
    val greenContainerBg = if (isDark) ColorGreenBgDark else ColorGreenBgLight
    val greenTitleColor = if (isDark) ColorGreenTitleDark else ColorGreenTitleLight
    val greenSubColor = if (isDark) ColorGreenSubDark else ColorGreenSubLight
    val containerBg = if (useMonet) miuixColors.secondaryContainer else greenContainerBg
    val onContainer = if (useMonet) miuixColors.onSecondaryContainer else greenTitleColor
    val onContainerSub = if (useMonet) miuixColors.onSurfaceSecondary else greenSubColor

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(containerBg).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "已激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onContainer); Spacer(modifier = Modifier.height(4.dp)); Text(text = version.ifEmpty { "检测中..." }, fontSize = 14.sp, color = onContainerSub) }
            if (useMonet) { val cc = miuixColors.primary; Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawArc(color = cc, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round), size = Size(size.width - sw, size.height - sw), topLeft = Offset(sw / 2f, sw / 2f)); val cp = Path().apply { moveTo(center.x - 5.dp.toPx(), center.y); lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx()); lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()) }; drawPath(path = cp, color = cc, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) } }
            else { Canvas(modifier = Modifier.size(32.dp)) { val sw = 2.dp.toPx(); drawArc(color = greenAccent, startAngle = 90f, sweepAngle = 270f, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round), size = Size(size.width - sw, size.height - sw), topLeft = Offset(sw / 2f, sw / 2f)); val cp = Path().apply { moveTo(center.x - 5.dp.toPx(), center.y); lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx()); lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()) }; drawPath(path = cp, color = greenAccent, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) } }
        }
    }
}