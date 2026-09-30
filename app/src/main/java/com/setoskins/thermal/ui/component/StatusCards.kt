package com.setoskins.thermal.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

enum class WarningLevel { Error, Notice }

@Composable
fun WarningCard(
    message: String,
    modifier: Modifier = Modifier,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        onClick = { onClick?.invoke() },
        colors = CardDefaults.defaultColors(
            color = level.containerColor(),
            contentColor = level.contentColor(),
        ),
        showIndication = onClick != null,
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = message, fontSize = 14.sp)
            action?.invoke()
        }
    }
}

@Composable
private fun WarningLevel.containerColor(): Color = when {
    MiuixTheme.isDynamicColor -> when (this) {
        WarningLevel.Error -> MiuixTheme.colorScheme.errorContainer
        WarningLevel.Notice -> MiuixTheme.colorScheme.tertiaryContainer
    }

    isSystemInDarkTheme() -> when (this) {
        WarningLevel.Error -> Color(0xFF310808)
        WarningLevel.Notice -> Color(0xFF3E2F1B)
    }

    else -> when (this) {
        WarningLevel.Error -> Color(0xFFF8E2E2)
        WarningLevel.Notice -> Color(0xFFFFF0DB)
    }
}

@Composable
private fun WarningLevel.contentColor(): Color = when {
    MiuixTheme.isDynamicColor -> when (this) {
        WarningLevel.Error -> MiuixTheme.colorScheme.onErrorContainer
        WarningLevel.Notice -> MiuixTheme.colorScheme.onTertiaryContainer
    }

    else -> when (this) {
        WarningLevel.Error -> Color(0xFFF72727)
        WarningLevel.Notice -> Color(0xFFF5A623)
    }
}

@Composable
fun GreenActivatedCard(useMonet: Boolean, version: String) {
    if (useMonet) GreenActivatedCardMaterial(version)
    else GreenActivatedCardMiuix(version)
}

@Composable
fun RedNotInstalledCard(useMonet: Boolean) {
    if (useMonet) RedNotInstalledCardMaterial()
    else RedNotInstalledCardMiuix()
}

@Composable
fun UpdateWarningCard(useMonet: Boolean) {
    if (useMonet) {
        YellowUpdateCard()
    } else {
        val uriHandler = LocalUriHandler.current
        WarningCard(
            message = "模块检测到更新，点击下载新版本",
            modifier = Modifier.fillMaxWidth(),
            level = WarningLevel.Notice,
            onClick = { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") },
        )
    }
}

@Composable
private fun GreenActivatedCardMaterial(version: String) {
    val miuixColors = MiuixTheme.colorScheme
    val containerBg = miuixColors.secondaryContainer
    val onContainer = miuixColors.onSecondaryContainer
    val onContainerSub = miuixColors.onSurfaceSecondary

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(containerBg).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "已激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = version.ifEmpty { "" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onContainerSub)
            }
            val cc = miuixColors.primary
            Canvas(modifier = Modifier.size(32.dp)) {
                val sw = 2.dp.toPx()
                drawArc(color = cc, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round), size = Size(size.width - sw, size.height - sw), topLeft = Offset(sw / 2f, sw / 2f))
                val cp = Path().apply { moveTo(center.x - 5.dp.toPx(), center.y); lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx()); lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()) }
                drawPath(path = cp, color = cc, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
private fun RedNotInstalledCardMaterial() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) ColorRedBgDark else ColorRedBgLight
    val titleColor = if (isDark) ColorRedTitleDark else ColorRedTitleLight
    val subColor = if (isDark) ColorRedSubDark else ColorRedSubLight
    val iconColor = if (isDark) ColorRedIconDark else ColorRedIconLight
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "未激活", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "模块未安装", fontSize = 14.sp, color = subColor)
            }
            Canvas(modifier = Modifier.size(32.dp)) {
                val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center)
                drawLine(color = iconColor, start = Offset(center.x, center.y - 6.dp.toPx()), end = Offset(center.x, center.y + 2.dp.toPx()), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color = iconColor, radius = 1.5.dp.toPx(), center = Offset(center.x, center.y + 7.dp.toPx()))
            }
        }
    }
}

@Composable
private fun YellowUpdateCard() {
    val uriHandler = LocalUriHandler.current; val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) ColorYellowBgDark else ColorYellowBgLight
    val titleColor = if (isDark) ColorYellowTitleDark else ColorYellowTitleLight
    val subColor = if (isDark) ColorYellowSubDark else ColorYellowSubLight
    val iconColor = if (isDark) ColorYellowIconDark else ColorYellowIconLight
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).clickable { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") }.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = "检测到更新", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor); Spacer(modifier = Modifier.height(4.dp)); Text(text = "模块检测到更新，点击下载新版本", fontSize = 14.sp, color = subColor) }
            Canvas(modifier = Modifier.size(32.dp)) {
                val sw = 2.dp.toPx(); drawCircle(color = iconColor, radius = (size.minDimension - sw) / 2, style = Stroke(width = sw), center = center)
                val arrowPath = Path().apply { moveTo(center.x, center.y - 6.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); moveTo(center.x - 4.dp.toPx(), center.y + 2.dp.toPx()); lineTo(center.x, center.y + 6.dp.toPx()); lineTo(center.x + 4.dp.toPx(), center.y + 2.dp.toPx()) }
                drawPath(path = arrowPath, color = iconColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
private fun GreenActivatedCardMiuix(version: String) {
    val isDark = isSystemInDarkTheme(); val miuixColors = MiuixTheme.colorScheme
    val cardColor = if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4)
    val iconTint = Color(0xFF36D167)
    val sub = version.ifEmpty { "" }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = cardColor, contentColor = miuixColors.onSurface),
            onClick = {},
            showIndication = true,
            pressFeedbackType = PressFeedbackType.Tilt,
        ) {
            Box {
                Box(modifier = Modifier.fillMaxSize().offset(27.dp, 31.dp), contentAlignment = Alignment.BottomEnd) {
                    Icon(modifier = Modifier.size(110.dp), imageVector = Icons.Rounded.CheckCircleOutline, tint = iconTint, contentDescription = null)
                }
                Box(modifier = Modifier.fillMaxSize().padding(16.dp, 14.dp), contentAlignment = Alignment.TopStart) {
                    Column { Text(text = "已激活", fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(1.dp)); Text(text = sub, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun RedNotInstalledCardMiuix() {
    val uriHandler = LocalUriHandler.current
    val isDark = isSystemInDarkTheme()
    val miuixColors = MiuixTheme.colorScheme
    val cardColor = if (isDark) ColorRedBgDark else ColorRedBgLight
    val iconTint = if (isDark) ColorRedIconDark else ColorRedIconLight

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(color = cardColor, contentColor = miuixColors.onSurface),
            onClick = { uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases") },
            showIndication = true,
            pressFeedbackType = PressFeedbackType.Tilt,
        ) {
            Box {
                Box(modifier = Modifier.fillMaxSize().offset(27.dp, 31.dp), contentAlignment = Alignment.BottomEnd) {
                    Icon(modifier = Modifier.size(110.dp), imageVector = Icons.Rounded.ErrorOutline, tint = iconTint, contentDescription = null)
                }
                Box(modifier = Modifier.fillMaxSize().padding(16.dp, 14.dp), contentAlignment = Alignment.TopStart) {
                    Column { Text(text = "未激活", fontSize = 22.sp, fontWeight = FontWeight.SemiBold); Spacer(modifier = Modifier.height(1.dp)); Text(text = "模块未安装", fontSize = 15.sp) }
                }
            }
        }
    }
}