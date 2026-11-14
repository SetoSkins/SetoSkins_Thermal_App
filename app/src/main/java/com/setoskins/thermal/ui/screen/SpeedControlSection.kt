package com.setoskins.thermal.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.setoskins.thermal.ui.component.ConfigDialog
import com.setoskins.thermal.ui.component.ThemedOptionCard
import com.setoskins.thermal.ui.component.SliderRow
import com.setoskins.thermal.ui.component.ThemedSwitch
import com.setoskins.thermal.ui.component.ThemedTextField
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class DialogState(
    val title: String,
    val summary: String,
    val initialValue: String,
    val range: IntRange,
    val onConfirm: (Int) -> Unit
)

private fun anyExclusiveActive(vararg switches: Boolean) = switches.any { it }

@OptIn(FlowPreview::class)
@Composable
fun SpeedControlSection(
    useMonet: Boolean,
    prefs: android.content.SharedPreferences,
    externalConfig: Map<String, String> = emptyMap(),
    updateSwitch: (String, String, Boolean, (Boolean) -> Unit) -> Unit,
    updateText: (String, String, String, (String) -> Unit) -> Unit,
    onShowBlacklist: () -> Unit = {},
    onShowWhitelist: () -> Unit = {},
    onDialogVisibilityChange: (Boolean) -> Unit = {},
    switch4: Boolean,
    switch10: Boolean,
    switch11: Boolean,
    switch12: Boolean,
    switch13: Boolean,
    switch8: Boolean,
    switch9: Boolean,
    homeExclusiveActive: Boolean,
    onToggleSwitch4: (Boolean) -> Unit,
    onToggleSwitch10: (Boolean) -> Unit,
    onToggleSwitch11: (Boolean) -> Unit,
    onToggleSwitch12: (Boolean) -> Unit,
    onToggleSwitch13: (Boolean) -> Unit,
    onToggleSwitch8: (Boolean) -> Unit,
    onToggleSwitch9: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val context = LocalContext.current

    var pendingWhitelist by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (pendingWhitelist) onShowWhitelist() else onShowBlacklist()
        }
    }
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
    var permDialogInternalShow by remember { mutableStateOf(false) }
    var permPendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(showPermissionDialog) { if (showPermissionDialog) { permDialogInternalShow = true; permPendingAction = null } }

    var currentValue by rememberSaveable { mutableStateOf(prefs.getString("currentValue", "22000000") ?: "22000000") }
    var screenOnValue by rememberSaveable { mutableStateOf(prefs.getString("screenOnValue", "") ?: "") }
    var screenOffValue by rememberSaveable { mutableStateOf(prefs.getString("screenOffValue", "") ?: "") }

    var limit1Temp by rememberSaveable { mutableStateOf(prefs.getString("limit1Temp", "40") ?: "40") }
    var limit1Current by rememberSaveable { mutableStateOf(prefs.getString("limit1Current", "") ?: "") }
    var limit2Temp by rememberSaveable { mutableStateOf(prefs.getString("limit2Temp", "43") ?: "43") }
    var limit2Current by rememberSaveable { mutableStateOf(prefs.getString("limit2Current", "") ?: "") }
    var limit3Temp by rememberSaveable { mutableStateOf(prefs.getString("limit3Temp", "46") ?: "46") }
    var limit3Current by rememberSaveable { mutableStateOf(prefs.getString("limit3Current", "") ?: "") }
    var delayTempThreshold by rememberSaveable { mutableStateOf(prefs.getString("delayTempThreshold", "10") ?: "10") }

    var step1Level by rememberSaveable { mutableStateOf(prefs.getString("step1Level", "20") ?: "20") }
    var step1Current by rememberSaveable { mutableStateOf(prefs.getString("step1Current", "") ?: "") }
    var step2Level by rememberSaveable { mutableStateOf(prefs.getString("step2Level", "50") ?: "50") }
    var step2Current by rememberSaveable { mutableStateOf(prefs.getString("step2Current", "") ?: "") }
    var step3Level by rememberSaveable { mutableStateOf(prefs.getString("step3Level", "80") ?: "80") }
    var step3Current by rememberSaveable { mutableStateOf(prefs.getString("step3Current", "") ?: "") }

    var stopChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("stopChargeLevel", "80") ?: "80") }
    var resumeChargeLevel by rememberSaveable { mutableStateOf(prefs.getString("resumeChargeLevel", "75") ?: "75") }
    var stopChargeCurrent by rememberSaveable { mutableStateOf(prefs.getString("stopChargeCurrent", "500") ?: "500") }

    var dialogState by remember { mutableStateOf<DialogState?>(null) }
    var dialogDismissStarted by remember { mutableStateOf(false) }
    LaunchedEffect(dialogState) {
        if (dialogState != null) {
            dialogDismissStarted = false
            onDialogVisibilityChange(true)
        } else if (!dialogDismissStarted) {
            onDialogVisibilityChange(false)
        }
    }
    var permDismissStarted by remember { mutableStateOf(false) }
    LaunchedEffect(showPermissionDialog) {
        if (showPermissionDialog) {
            permDismissStarted = false
            onDialogVisibilityChange(true)
        } else if (!permDismissStarted) {
            onDialogVisibilityChange(false)
        }
    }

    var configSynced by rememberSaveable { mutableStateOf(false) }
    var lastConfigFingerprint by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(externalConfig) {
        val fingerprint = externalConfig.entries.sortedBy { it.key }.toString()
        if (externalConfig.isEmpty() || (configSynced && fingerprint == lastConfigFingerprint)) return@LaunchedEffect
        configSynced = true
        lastConfigFingerprint = fingerprint
        val editor = prefs.edit()
        var dirty = false
        val textFields = mapOf<String, (String) -> Unit>(
            "最大电流数" to { v -> currentValue = v; editor.putString("currentValue", v); dirty = true },
            "亮屏限制电流" to { v -> screenOnValue = v; editor.putString("screenOnValue", v); dirty = true },
            "锁屏限制电流" to { v -> screenOffValue = v; editor.putString("screenOffValue", v); dirty = true },
            "一限温度阈值" to { v -> limit1Temp = v; editor.putString("limit1Temp", v); dirty = true },
            "一限限制电流" to { v -> limit1Current = v; editor.putString("limit1Current", v); dirty = true },
            "二限温度阈值" to { v -> limit2Temp = v; editor.putString("limit2Temp", v); dirty = true },
            "二限限制电流" to { v -> limit2Current = v; editor.putString("limit2Current", v); dirty = true },
            "三限温度阈值" to { v -> limit3Temp = v; editor.putString("limit3Temp", v); dirty = true },
            "三限限制电流" to { v -> limit3Current = v; editor.putString("limit3Current", v); dirty = true },
            "延迟温度阈值" to { v -> delayTempThreshold = v; editor.putString("delayTempThreshold", v); dirty = true },
            "一限电量阈值" to { v -> step1Level = v; editor.putString("step1Level", v); dirty = true },
            "一限电量限制电流" to { v -> step1Current = v; editor.putString("step1Current", v); dirty = true },
            "二限电量阈值" to { v -> step2Level = v; editor.putString("step2Level", v); dirty = true },
            "二限电量限制电流" to { v -> step2Current = v; editor.putString("step2Current", v); dirty = true },
            "三限电量阈值" to { v -> step3Level = v; editor.putString("step3Level", v); dirty = true },
            "三限电量限制电流" to { v -> step3Current = v; editor.putString("step3Current", v); dirty = true },
            "电量检测阈值" to { v -> stopChargeLevel = v; editor.putString("stopChargeLevel", v); dirty = true },
            "恢复充电电量" to { v -> resumeChargeLevel = v; editor.putString("resumeChargeLevel", v); dirty = true },
            "停充电流阈值" to { v -> stopChargeCurrent = v; editor.putString("stopChargeCurrent", v); dirty = true }
        )
        for ((key, raw) in externalConfig) {
            val trimmed = raw.trim()
            textFields[key]?.let { it(if (trimmed.equals("false", ignoreCase = true)) "" else trimmed) }
        }
        if (dirty) editor.apply()
    }

    @Composable
    fun debounceSlider(key: String, name: String, valueProvider: () -> String) {
        LaunchedEffect(key) {
            snapshotFlow { valueProvider() }
                .debounce(300)
                .drop(1)
                .collect { updateText(key, name, it) {} }
        }
    }
    debounceSlider("limit1Temp", "一限温度阈值") { limit1Temp }
    debounceSlider("limit2Temp", "二限温度阈值") { limit2Temp }
    debounceSlider("limit3Temp", "三限温度阈值") { limit3Temp }
    debounceSlider("delayTempThreshold", "延迟温度阈值") { delayTempThreshold }
    debounceSlider("step1Level", "一限电量阈值") { step1Level }
    debounceSlider("step2Level", "二限电量阈值") { step2Level }
    debounceSlider("step3Level", "三限电量阈值") { step3Level }
    debounceSlider("stopChargeLevel", "电量检测阈值") { stopChargeLevel }
    debounceSlider("resumeChargeLevel", "恢复充电电量") { resumeChargeLevel }

    fun isExclusiveEnabled() = !anyExclusiveActive(switch4, switch11, switch13, switch10, switch12, switch8, switch9) && !homeExclusiveActive

    ThemedOptionCard(useMonet = useMonet, modifier = modifier) {
        Column {

            BasicComponent(
                title = if (isZh) "修改最大电流数" else "Max current",
                enabled = switch4 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch4, onCheckedChange = null, enabled = switch4 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch4 || isExclusiveEnabled()) {
                        onToggleSwitch4(!switch4)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch4, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("max_current") {
                    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                        ThemedTextField(
                            value = currentValue,
                            onValueChange = { updateText("currentValue", "最大电流数", it) { currentValue = it } },
                                label = if (isZh) "22A＝22000mA＝22000000μA" else "22A = 22000mA = 22000000μA",
                            useMonet = useMonet,
                            modifier = Modifier.fillMaxWidth(),
                            inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                        )
                    }
                }
            }

            BasicComponent(
                title = if (isZh) "充电调速" else "Speed control while charging",
                enabled = switch11 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch11, onCheckedChange = null, enabled = switch11 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch11 || isExclusiveEnabled()) {
                        onToggleSwitch11(!switch11)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("charge_speed") {
                    Column {
                        SliderRow(
                            title = if (isZh) "一限温度阈值" else "Temp threshold 1",
                            value = limit1Temp.toFloatOrNull() ?: 40f,
                            onValueChange = { limit1Temp = it.toInt().toString() },
                            valueRange = 20f..50f, steps = 30, suffix = "°C",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整一限温度阈值" else "Adjust temp threshold 1", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", limit1Temp, 20..50) { v ->
                                    updateText("limit1Temp", "一限温度阈值", v.toString()) { limit1Temp = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = limit1Current, onValueChange = { updateText("limit1Current", "一限限制电流", it) { limit1Current = it } }, label = if (isZh) "一限限制电流" else "Current limit 1", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }
            AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("charge_speed_2") {
                    Column {
                        SliderRow(
                            title = if (isZh) "二限温度阈值" else "Temp threshold 2",
                            value = limit2Temp.toFloatOrNull() ?: 43f,
                            onValueChange = { limit2Temp = it.toInt().toString() },
                            valueRange = 20f..50f, steps = 30, suffix = "°C",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整二限温度阈值" else "Adjust temp threshold 2", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", limit2Temp, 20..50) { v ->
                                    updateText("limit2Temp", "二限温度阈值", v.toString()) { limit2Temp = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = limit2Current, onValueChange = { updateText("limit2Current", "二限限制电流", it) { limit2Current = it } }, label = if (isZh) "二限限制电流" else "Current limit 2", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }
            AnimatedVisibility(visible = switch11, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("charge_speed_3") {
                    Column {
                        SliderRow(
                            title = if (isZh) "三限温度阈值" else "Temp threshold 3",
                            value = limit3Temp.toFloatOrNull() ?: 46f,
                            onValueChange = { limit3Temp = it.toInt().toString() },
                            valueRange = 20f..50f, steps = 30, suffix = "°C",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整三限温度阈值" else "Adjust temp threshold 3", if (isZh) "输入温度 (20°C-50°C)" else "Enter temperature (20°C-50°C)", limit3Temp, 20..50) { v ->
                                    updateText("limit3Temp", "三限温度阈值", v.toString()) { limit3Temp = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = limit3Current, onValueChange = { updateText("limit3Current", "三限限制电流", it) { limit3Current = it } }, label = if (isZh) "三限限制电流" else "Current limit 3", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                        SliderRow(
                            title = if (isZh) "延迟温度阈值" else "Delayed temp threshold",
                            summary = if (isZh) "为了防止电流卡在限制电流，需要设置延迟温度阈值。" else "Set a delayed temp threshold to prevent the current from getting stuck at the limit.",
                            value = delayTempThreshold.toFloatOrNull() ?: 10f,
                            onValueChange = { delayTempThreshold = it.toInt().toString() },
                            valueRange = 5f..30f, steps = 25, suffix = " S",
                            onClickLabel = {
                                dialogState = DialogState(
                                    if (isZh) "调整延迟温度阈值" else "Adjust delayed temp threshold",
                                    if (isZh) "输入延迟时间 (5S-30S)" else "Enter delay time (5S-30S)",
                                    delayTempThreshold,
                                    5..30
                                ) { v -> delayTempThreshold = v.toString() }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
            }

            BasicComponent(
                title = if (isZh) "自定义阶梯模式" else "Custom step mode",
                enabled = switch13 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch13, onCheckedChange = null, enabled = switch13 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch13 || isExclusiveEnabled()) {
                        onToggleSwitch13(!switch13)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("step_mode_1") {
                    Column {
                        SliderRow(
                            title = if (isZh) "一限电量阈值" else "Battery threshold 1",
                            value = step1Level.toFloatOrNull() ?: 20f,
                            onValueChange = { step1Level = it.toInt().toString() },
                            valueRange = 0f..100f, steps = 100, suffix = " %",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整一限电量阈值" else "Adjust battery threshold 1", if (isZh) "输入电量 (0%-100%)" else "Enter battery level (0%-100%)", step1Level, 0..100) { v ->
                                    updateText("step1Level", "一限电量阈值", v.toString()) { step1Level = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = step1Current, onValueChange = { updateText("step1Current", "一限电量限制电流", it) { step1Current = it } }, label = if (isZh) "一限电量限制电流" else "Battery current limit 1", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }
            AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("step_mode_2") {
                    Column {
                        SliderRow(
                            title = if (isZh) "二限电量阈值" else "Battery threshold 2",
                            value = step2Level.toFloatOrNull() ?: 50f,
                            onValueChange = { step2Level = it.toInt().toString() },
                            valueRange = 0f..100f, steps = 100, suffix = " %",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整二限电量阈值" else "Adjust battery threshold 2", if (isZh) "输入电量 (0%-100%)" else "Enter battery level (0%-100%)", step2Level, 0..100) { v ->
                                    updateText("step2Level", "二限电量阈值", v.toString()) { step2Level = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = step2Current, onValueChange = { updateText("step2Current", "二限电量限制电流", it) { step2Current = it } }, label = if (isZh) "二限电量限制电流" else "Battery current limit 2", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }
            AnimatedVisibility(visible = switch13, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("step_mode_3") {
                    Column {
                        SliderRow(
                            title = if (isZh) "三限电量阈值" else "Battery threshold 3",
                            value = step3Level.toFloatOrNull() ?: 80f,
                            onValueChange = { step3Level = it.toInt().toString() },
                            valueRange = 0f..100f, steps = 100, suffix = " %",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整三限电量阈值" else "Adjust battery threshold 3", if (isZh) "输入电量 (0%-100%)" else "Enter battery level (0%-100%)", step3Level, 0..100) { v ->
                                    updateText("step3Level", "三限电量阈值", v.toString()) { step3Level = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = step3Current, onValueChange = { updateText("step3Current", "三限电量限制电流", it) { step3Current = it } }, label = if (isZh) "三限电量限制电流" else "Battery current limit 3", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }

            BasicComponent(
                title = if (isZh) "亮息屏调速" else "Speed control on screen off/on",
                enabled = switch10 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch10, onCheckedChange = null, enabled = switch10 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch10 || isExclusiveEnabled()) {
                        onToggleSwitch10(!switch10)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch10, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("screen_control") {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemedTextField(value = screenOnValue, onValueChange = { updateText("screenOnValue", "亮屏限制电流", it) { screenOnValue = it } }, label = if (isZh) "亮屏限制电流" else "Screen-on current limit", useMonet = useMonet, modifier = Modifier.fillMaxWidth(), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                        ThemedTextField(value = screenOffValue, onValueChange = { updateText("screenOffValue", "锁屏限制电流", it) { screenOffValue = it } }, label = if (isZh) "锁屏限制电流" else "Screen-off current limit", useMonet = useMonet, modifier = Modifier.fillMaxWidth(), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            BasicComponent(
                title = if (isZh) "当电流低于阈值执行停充" else "Stop charging when current drops below threshold",
                enabled = switch12 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch12, onCheckedChange = null, enabled = switch12 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch12 || isExclusiveEnabled()) {
                        onToggleSwitch12(!switch12)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch12, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("stop_charge") {
                    Column {
                        SliderRow(
                            title = if (isZh) "电量检测阈值" else "Battery detection threshold",
                            value = stopChargeLevel.toFloatOrNull() ?: 80f,
                            onValueChange = { stopChargeLevel = it.toInt().toString() },
                            valueRange = 0f..100f, steps = 100, suffix = " %",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整电量检测阈值" else "Adjust battery detection threshold", if (isZh) "输入电量检测阈值范围 (0%-100%)" else "Enter battery detection threshold range (0%-100%)", stopChargeLevel, 0..100) { v ->
                                    updateText("stopChargeLevel", "电量检测阈值", v.toString()) { stopChargeLevel = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        SliderRow(
                            title = if (isZh) "恢复充电电量" else "Resume charging level",
                            value = resumeChargeLevel.toFloatOrNull() ?: 75f,
                            onValueChange = { resumeChargeLevel = it.toInt().toString() },
                            valueRange = 0f..100f, steps = 100, suffix = " %",
                            onClickLabel = {
                                dialogState = DialogState(if (isZh) "调整恢复充电电量" else "Adjust resume charging level", if (isZh) "输入恢复充电电量范围 (0%-100%)" else "Enter resume charging level range (0%-100%)", resumeChargeLevel, 0..100) { v ->
                                    updateText("resumeChargeLevel", "恢复充电电量", v.toString()) { resumeChargeLevel = it }
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        ThemedTextField(value = stopChargeCurrent, onValueChange = { updateText("stopChargeCurrent", "停充电流阈值", it) { stopChargeCurrent = it } }, label = if (isZh) "停充电流阈值" else "Stop charging current threshold", useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } })
                    }
                }
            }

            BasicComponent(
                title = if (isZh) "分应用调速" else "Per-app speed control",
                enabled = switch8 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch8, onCheckedChange = null, enabled = switch8 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch8 || isExclusiveEnabled()) {
                        onToggleSwitch8(!switch8)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch8, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("per_app_blacklist") {
                    BasicComponent(
                        title = if (isZh) "分应用调速名单" else "Per-app speed control list",
                        summary = if (isZh) "选择参与分应用调速的应用" else "Select apps to participate in per-app speed control.",
                        endActions = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                            )
                        },
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.QUERY_ALL_PACKAGES) == PackageManager.PERMISSION_GRANTED) {
                                onShowBlacklist()
                            } else {
                                showPermissionDialog = true
                                pendingWhitelist = false
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }

            BasicComponent(
                title = if (isZh) "无温控应用" else "No-thermal apps",
                enabled = switch9 || isExclusiveEnabled(),
                endActions = { ThemedSwitch(checked = switch9, onCheckedChange = null, enabled = switch9 || isExclusiveEnabled(), useMonet = useMonet) },
                onClick = {
                    if (switch9 || isExclusiveEnabled()) {
                        onToggleSwitch9(!switch9)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
            AnimatedVisibility(visible = switch9, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                key("per_app_whitelist") {
                    BasicComponent(
                        title = if (isZh) "无温控应用名单" else "No-thermal app list",
                        endActions = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                            )
                        },
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.QUERY_ALL_PACKAGES) == PackageManager.PERMISSION_GRANTED) {
                                onShowWhitelist()
                            } else {
                                showPermissionDialog = true
                                pendingWhitelist = true
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }

            dialogState?.let { state ->
                ConfigDialog(
                    show = true,
                    title = state.title,
                    summary = state.summary,
                    initialValue = state.initialValue,
                    validationRange = state.range,
                    isZh = isZh,
                    onConfirm = state.onConfirm,
                    onDismiss = { dialogState = null },
                    onDismissStart = { dialogDismissStarted = true; onDialogVisibilityChange(false) }
                )
            }

            OverlayDialog(
                show = showPermissionDialog,
                title = if (isZh) "需要权限" else "Permission Required",
                summary = if (isZh) "获取已安装应用列表需要查询所有应用权限，请在接下来的对话框中授权。" else "Access to installed apps requires the \"Query all packages\" permission. Please grant it in the next dialog.",
                onDismissRequest = { permDismissStarted = true; onDialogVisibilityChange(false); showPermissionDialog = false },
                content = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { permDismissStarted = true; onDialogVisibilityChange(false); showPermissionDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text(if (isZh) "取消" else "Cancel", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                permissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColorsPrimary()
                        ) {
                            Text(if (isZh) "授权" else "Grant", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    }
}
