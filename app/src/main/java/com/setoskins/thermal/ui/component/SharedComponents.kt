package com.setoskins.thermal.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

// ── DeviceInfoItem ──

@Composable
fun DeviceInfoItem(value: String, label: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ColorDeviceInfoValue)
        Text(text = label, fontSize = 13.sp, color = ColorDeviceInfoLabel)
    }
}

// ── MiuixCard ──

@Composable
fun MiuixCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), cornerRadius = 16.dp) { content() }
}

// ── MiuixListItem ──

@Composable
fun MiuixListItem(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
            if (subtitle != null) { Spacer(modifier = Modifier.height(2.dp)); Text(text = subtitle, fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary) }
        }
    }
}

// ── compactSmallTitle Modifier 扩展 ──

fun Modifier.compactSmallTitle(): Modifier = this.padding(start = 4.dp).offset(x = (-13).dp, y = 11.dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); val reduce = 24.dp.roundToPx(); layout(placeable.width, (placeable.height - reduce).coerceAtLeast(1)) { placeable.place(0, -reduce) } }

// ── SectionTitle ──

@Composable
fun SectionTitle(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.offset(x = (-12).dp).layout { measurable, constraints -> val placeable = measurable.measure(constraints); layout(placeable.width, placeable.height - 8.dp.roundToPx()) { placeable.place(0, 0) } }) { content() }
}

// ── ThemedTextField：增强版，支持输入过滤和验证 ──

@Composable
fun ThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    useMonet: Boolean,
    modifier: Modifier = Modifier,
    inputFilter: (String) -> Boolean = { true },
    isValid: Boolean = true,
    errorMessage: String? = null
) {
    val filteredChange: (String) -> Unit = { newValue ->
        if (inputFilter(newValue)) onValueChange(newValue)
    }
    if (useMonet) {
        OutlinedTextField(
            value = value,
            onValueChange = filteredChange,
            modifier = modifier,
            label = { Text(label) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            isError = !isValid,
            supportingText = if (errorMessage != null && !isValid) ({ Text(errorMessage) }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MiuixTheme.colorScheme.primary,
                focusedLabelColor = MiuixTheme.colorScheme.primary,
                cursorColor = MiuixTheme.colorScheme.primary,
                focusedTextColor = MiuixTheme.colorScheme.onSurface
            )
        )
    } else {
        TextField(value = value, onValueChange = filteredChange, modifier = modifier, label = label, singleLine = true)
    }
}

// ── ConfigDialog：统一对话框，消除 8 个重复 OverlayDialog ──

@Composable
fun ConfigDialog(
    show: Boolean,
    title: String,
    summary: String,
    initialValue: String,
    validationRange: IntRange,
    isZh: Boolean,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    LaunchedEffect(show) { if (show) inputValue = initialValue }
    OverlayDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onDismiss,
        content = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = inputValue,
                    onValueChange = { if (it.all { char -> char.isDigit() } || it.isEmpty()) inputValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) {
                        Text(if (isZh) "取消" else "Cancel")
                    }
                    Button(onClick = {
                        val v = inputValue.toIntOrNull()
                        if (v != null && v in validationRange) {
                            onConfirm(v)
                            onDismiss()
                        }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) {
                        Text(if (isZh) "确认" else "Confirm")
                    }
                }
            }
        }
    )
}

// ── SliderRow：Slider + 可点击数值标签（精确输入入口） ──

@Composable
fun SliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    suffix: String,
    summary: String? = null,
    onClickLabel: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var previousValue by remember { mutableStateOf(value) }
    BasicComponent(
        title = title,
        summary = summary,
        endActions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${value.toInt()}$suffix",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    modifier = Modifier.size(width = 10.dp, height = 16.dp),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantActions)
                )
            }
        },
        onClick = onClickLabel
    )
    Slider(
        value = value,
        onValueChange = { if (it != previousValue) { previousValue = it; hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove) }; onValueChange(it) },
        valueRange = valueRange,
        steps = steps,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    )
}

