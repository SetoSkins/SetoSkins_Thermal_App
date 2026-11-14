package com.setoskins.thermal.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.setoskins.thermal.R
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.ui.component.ThemedSwitch
import com.setoskins.thermal.ui.component.VerticalScrollBar
import com.setoskins.thermal.ui.component.UpdateDialog
import com.setoskins.thermal.data.UpdateManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.setoskins.thermal.ui.component.effect.BgEffectBackground
import com.setoskins.thermal.ui.component.ThemedOptionCard
import com.setoskins.thermal.ui.component.rememberBlurBackdrop
import com.setoskins.thermal.ui.component.rememberScrollBarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import com.setoskins.thermal.ui.component.IconTile
import com.setoskins.thermal.ui.component.MaterialChevron
import com.setoskins.thermal.ui.component.MaterialDropdownItem
import com.setoskins.thermal.ui.component.MaterialListItem
import com.setoskins.thermal.ui.component.MaterialSegmentedColumn
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import com.setoskins.thermal.ui.component.animation.customOverScroll
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi::class)
@Composable
fun ProfileScreen(useMonet: Boolean, onUseMonetChange: (Boolean) -> Unit, miuixMonet: Boolean = false, onMiuixMonetChange: (Boolean) -> Unit = {}, floatingNavBar: Boolean, onFloatingNavBarChange: (Boolean) -> Unit, onConfigImported: () -> Unit = {}, onNavigateToDonate: () -> Unit = {}, onNavigateToCredits: () -> Unit = {}, reduceEffects: Boolean = false, isPageActive: Boolean = true, onDialogVisibilityChange: (Boolean) -> Unit = {}, bottomContentInset: Dp = 0.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current
    val repoUrl = "https://github.com/SetoSkins/SetoSkins_Thermal_App"
    var versionName by remember { mutableStateOf("1.0") }
    var versionCode by remember { mutableStateOf(0L) }
    var appName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        versionName = packageInfo.versionName ?: "1.0"
        versionCode = packageInfo.longVersionCode
        appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    }
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"; val scope = rememberCoroutineScope(); val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDismissStarted by remember { mutableStateOf(false) }

    UpdateDialog(
        show = showUpdateDialog,
        version = updateInfo?.latestVersion ?: "",
        releaseNotes = updateInfo?.releaseNotes ?: "",
        isZh = isZh,
        onConfirm = {
            updateInfo?.downloadUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            onDialogVisibilityChange(false)
            showUpdateDialog = false
        },
        onDismiss = {
            updateDismissStarted = true
            onDialogVisibilityChange(false)
            showUpdateDialog = false
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/plain"), onResult = { uri -> uri?.let { scope.launch { val content = ModuleDetector.readConfigRaw(); if (content.isNotEmpty()) { context.contentResolver.openOutputStream(it)?.use { s -> s.write(content.toByteArray()) }; Toast.makeText(context, if (isZh) "导出成功" else "Export Success", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "导出失败：内容为空" else "Export Failed: Empty content", Toast.LENGTH_SHORT).show() } } } })
    val importLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(), onResult = { uri -> uri?.let { scope.launch { try { val tempFile = java.io.File(context.cacheDir, "temp_config_import.prop"); context.contentResolver.openInputStream(it)?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }; val success = ModuleDetector.importConfigFile(tempFile.absolutePath); tempFile.delete(); if (success) { onConfigImported(); Toast.makeText(context, if (isZh) "导入成功，配置已重载" else "Import Success, Config reloaded", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "导入失败" else "Import Failed", Toast.LENGTH_SHORT).show() } } catch (e: Exception) { Toast.makeText(context, if (isZh) "错误: ${e.message}" else "Error: ${e.message}", Toast.LENGTH_SHORT).show() } } } })

    var showResetDialog by remember { mutableStateOf(false) }
    var resetDismissStarted by remember { mutableStateOf(false) }
    LaunchedEffect(showResetDialog) {
        if (showResetDialog) {
            resetDismissStarted = false
            onDialogVisibilityChange(true)
        } else if (!resetDismissStarted) {
            onDialogVisibilityChange(false)
        }
    }
    var resetDialogInternalShow by remember { mutableStateOf(false) }
    var resetPendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(showResetDialog) { if (showResetDialog) { resetDialogInternalShow = true; resetPendingAction = null } }
    OverlayDialog(show = resetDialogInternalShow, title = if (isZh) "重置模块配置" else "Reset Module Config", summary = if (isZh) "确定要重置模块配置吗？所有开关将被关闭。" else "Are you sure you want to reset the module config? All switches will be turned off.", onDismissRequest = { resetDismissStarted = true; onDialogVisibilityChange(false); resetDialogInternalShow = false }, onDismissFinished = { if (!resetDialogInternalShow) { resetPendingAction?.invoke(); showResetDialog = false } }, content = { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            resetDismissStarted = true; onDialogVisibilityChange(false); resetDialogInternalShow = false
        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) { Text(if (isZh) "取消" else "Cancel", fontWeight = FontWeight.Bold) };
        Button(onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            resetPendingAction = {
                scope.launch { val success = ModuleDetector.resetConfig(); if (success) { prefs.edit().clear().apply(); delay(200); onConfigImported(); Toast.makeText(context, if (isZh) "已重置并关闭所有开关" else "All configs reset and turned off", Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, if (isZh) "重置失败" else "Reset Failed", Toast.LENGTH_SHORT).show() } }
            }
            resetDialogInternalShow = false
        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) { Text(if (isZh) "确定" else "Confirm", fontWeight = FontWeight.Bold) } } })
    val listState = rememberLazyListState(); val density = LocalDensity.current; var logoHeightDp by remember { mutableStateOf(300.dp) }; val fadeDistancePx = remember(density) { with(density) { 360.dp.toPx() } }
    val progress by remember { derivedStateOf { val idx = listState.firstVisibleItemIndex; val offset = listState.firstVisibleItemScrollOffset.toFloat(); val scrollPx = if (idx <= 0) offset else fadeDistancePx; (scrollPx / fadeDistancePx).coerceIn(0f, 1f) } }
    val shaderSupported = remember { isRuntimeShaderSupported() }; val backdrop = rememberBlurBackdrop(); val dynamicBackground = shaderSupported && !reduceEffects; val isInDark = isSystemInDarkTheme(); var noiseCoefficient by remember { mutableFloatStateOf(BlurDefaults.NoiseCoefficient) }
    val logoBlend = remember(isInDark) { if (isInDark) listOf(BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge), BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight), BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab)) else listOf(BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn), BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight), BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab)) }
    val blurEnabled = backdrop != null
    val cardBlend = remember(isInDark) { if (isInDark) listOf(BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity), BlendColorEntry(Color(0x1A9C9C9C), BlurBlendMode.PlusDarker)) else listOf(BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay), BlendColorEntry(Color(0xB3FFFFFF), BlurBlendMode.HardLight)) }
    val aboutBlurModifier = if (backdrop != null) Modifier.textureBlur(backdrop = backdrop, shape = RoundedCornerShape(24.dp), blurRadius = 60f, noiseCoefficient = noiseCoefficient, colors = BlurDefaults.blurColors(blendColors = cardBlend)) else Modifier
    val aboutCardColor = if (blurEnabled) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer
    val appIconContext = LocalContext.current
    val logoMask = remember {
        val viewportSize = 108
        val maskSize = 360
        val scale = maskSize.toFloat() / viewportSize
        val bitmap = android.graphics.Bitmap.createBitmap(maskSize, maskSize, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(appIconContext, R.drawable.ic_launcher_foreground)!!
        drawable.setBounds(0, 0, maskSize, maskSize)
        val minX = 36.934f * scale; val minY = 33.002f * scale; val maxX = 71.274f * scale; val maxY = 74.202f * scale
        val logoW = maxX - minX; val logoH = maxY - minY
        val zoom = minOf(maskSize.toFloat() / logoW, maskSize.toFloat() / logoH) * 0.70f
        val matrix = android.graphics.Matrix()
        matrix.setTranslate(-minX, -minY)
        matrix.postScale(zoom, zoom)
        matrix.postTranslate((maskSize - logoW * zoom) / 2f, (maskSize - logoH * zoom) / 2f)
        canvas.concat(matrix)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }
    BgEffectBackground(dynamicBackground = dynamicBackground, isOs3Effect = true, isFullSize = true, modifier = Modifier.fillMaxSize(), bgModifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier, alpha = { 1f - progress }, playing = isPageActive) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 200.dp).onSizeChanged { size -> with(density) { logoHeightDp = size.height.toDp() } }, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp).graphicsLayer { val iconProgress = ((progress - 0.35f) / 0.15f).coerceIn(0f, 1f); alpha = 1 - iconProgress; scaleX = 1 - (iconProgress * 0.05f); scaleY = 1 - (iconProgress * 0.05f) }.then(if (backdrop != null) { Modifier.textureBlur(backdrop = backdrop, shape = RoundedCornerShape(28.dp), blurRadius = 96f, noiseCoefficient = noiseCoefficient, colors = BlurDefaults.blurColors(blendColors = logoBlend), contentBlendMode = ComposeBlendMode.DstIn) } else Modifier).clip(RoundedCornerShape(28.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { uriHandler.openUri(repoUrl) }) {
                val iconTileColor = MiuixTheme.colorScheme.onSurface
                Canvas(modifier = Modifier.matchParentSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
                    drawRoundRect(color = iconTileColor, cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()))
                    drawImage(image = logoMask, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), blendMode = ComposeBlendMode.DstOut)
                }
            }
            Text(modifier = Modifier.padding(top = 12.dp).fillMaxWidth().graphicsLayer { val projectNameProgress = ((progress - 0.20f) / 0.15f).coerceIn(0f, 1f); alpha = 1 - projectNameProgress; scaleX = 1 - (projectNameProgress * 0.05f); scaleY = 1 - (projectNameProgress * 0.05f) }.then(if (backdrop != null) { Modifier.textureBlur(backdrop = backdrop, shape = RoundedCornerShape(16.dp), blurRadius = 96f, noiseCoefficient = noiseCoefficient, colors = BlurDefaults.blurColors(blendColors = logoBlend), contentBlendMode = ComposeBlendMode.DstIn) } else Modifier).clip(RoundedCornerShape(16.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { uriHandler.openUri(repoUrl) }, text = appName, color = MiuixTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, textAlign = TextAlign.Center)
            Text(modifier = Modifier.padding(top = 12.dp).fillMaxWidth().graphicsLayer { val versionCodeProgress = ((progress - 0.05f) / 0.15f).coerceIn(0f, 1f); alpha = 1 - versionCodeProgress; scaleX = 1 - (versionCodeProgress * 0.05f); scaleY = 1 - (versionCodeProgress * 0.05f) }, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, text = "$versionName ($versionCode) | release", fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(80.dp))
        }
        Box(modifier = modifier.fillMaxSize()) {
            @OptIn(ExperimentalFoundationApi::class)
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                LazyColumn(modifier = Modifier.fillMaxSize().customOverScroll(), state = listState, horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 170.dp, bottom = 46.dp + bottomContentInset)) {

                    item(key = "logoSpacer") {
                        Box(Modifier.fillMaxWidth().height(logoHeightDp + 80.dp), contentAlignment = Alignment.TopCenter) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { uriHandler.openUri(repoUrl) }
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth(0.7f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { uriHandler.openUri(repoUrl) }
                                )
                            }
                        }
                    }
                    item(key = "setoskins_link") {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (useMonet) {
                            MaterialSegmentedColumn(modifier = Modifier.padding(horizontal = 20.dp), items = listOf({
                                MaterialListItem(
                                    headline = "SetoSkins",
                                    leading = { Image(painter = painterResource(id = R.drawable.seto), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape)) },
                                    trailing = { MaterialChevron() },
                                    onClick = { uriHandler.openUri("https://github.com/SetoSkins") }
                                )
                            }))
                        } else {
                            ThemedOptionCard(useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).then(aboutBlurModifier), miuixColor = aboutCardColor) { ArrowPreference(title = if (isZh) "SetoSkins" else "SetoSkins", startAction = { Box(modifier = Modifier.padding(end = 10.dp)) { Image(painter = painterResource(id = R.drawable.seto), contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape)) } }, onClick = { uriHandler.openUri("https://github.com/SetoSkins") }) }
                        }
                    }
                    item(key = "ui_style") {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (useMonet) {
                            MaterialSegmentedColumn(modifier = Modifier.padding(horizontal = 20.dp), items = listOf(
                                {
                                    MaterialDropdownItem(
                                        headline = if (isZh) "界面风格" else "UI Style",
                                        items = listOf("MiuiX", "Material"),
                                        selectedIndex = 1,
                                        onItemSelected = { onUseMonetChange(it == 1) },
                                        leading = { IconTile(Icons.Filled.DisplaySettings, contentDescription = if (isZh) "界面风格" else "UI Style") }
                                    )
                                }
                            ))
                        } else {
                            ThemedOptionCard(useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).then(aboutBlurModifier), miuixColor = aboutCardColor) {
                                WindowDropdownPreference(items = listOf("MiuiX", "Material"), selectedIndex = if (useMonet) 1 else 0, title = if (isZh) "界面风格" else "UI Style", onSelectedIndexChange = { onUseMonetChange(it == 1) })
                                AnimatedVisibility(
                                    visible = !useMonet,
                                    enter = expandVertically(animationSpec = tween(320, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))) + fadeIn(tween(200)),
                                    exit = shrinkVertically(animationSpec = tween(260, easing = CubicBezierEasing(0.12f, 0.0f, 0.05f, 1.0f))) + fadeOut(tween(160))
                                ) {
                                    BasicComponent(title = if (isZh) "启用Monet颜色" else "Enable Monet Colors", summary = if (isZh) "从壁纸提取颜色作为主题色" else "Derive theme colors from wallpaper", endActions = { ThemedSwitch(checked = miuixMonet, onCheckedChange = null, useMonet = useMonet) }, onClick = { onMiuixMonetChange(!miuixMonet); hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                                }
                                BasicComponent(title = if (isZh) "启用悬浮底栏" else "Floating Nav Bar", summary = if (isZh) "在屏幕底部使用悬浮样式的导航栏" else "Use floating-style navigation bar at the bottom of the screen", endActions = { ThemedSwitch(checked = floatingNavBar, onCheckedChange = null, useMonet = useMonet) }, onClick = { onFloatingNavBarChange(!floatingNavBar); hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) })
                            }
                        }
                    }
                    item(key = "config_io") {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (useMonet) {
                            MaterialSegmentedColumn(modifier = Modifier.padding(horizontal = 20.dp), items = listOf(
                                {
                                    MaterialListItem(
                                        headline = if (isZh) "导出软件配置" else "Export App Config",
                                        leading = { IconTile(Icons.Filled.Upload, contentDescription = if (isZh) "导出软件配置" else "Export App Config") },
                                        trailing = { MaterialChevron() },
                                        onClick = {
                                            scope.launch {
                                                if (!ModuleDetector.requestRoot()) {
                                                    Toast.makeText(context, if (isZh) "需要 Root 权限" else "Root access required", Toast.LENGTH_SHORT).show()
                                                    return@launch
                                                }
                                                exportLauncher.launch(if (isZh) "SetoSkins_配置_备份.prop" else "SetoSkins_Config_Backup.prop")
                                            }
                                        }
                                    )
                                },
                                {
                                    MaterialListItem(
                                        headline = if (isZh) "导入软件配置" else "Import App Config",
                                        leading = { IconTile(Icons.Filled.Download, contentDescription = if (isZh) "导入软件配置" else "Import App Config") },
                                        trailing = { MaterialChevron() },
                                        onClick = {
                                            scope.launch {
                                                if (!ModuleDetector.requestRoot()) {
                                                    Toast.makeText(context, if (isZh) "需要 Root 权限" else "Root access required", Toast.LENGTH_SHORT).show()
                                                    return@launch
                                                }
                                                importLauncher.launch(arrayOf("*/*"))
                                            }
                                        }
                                    )
                                },
                                {
                                    MaterialListItem(
                                        headline = if (isZh) "重置模块配置" else "Reset Module Config",
                                        leading = { IconTile(Icons.Filled.RestartAlt, contentDescription = if (isZh) "重置模块配置" else "Reset Module Config") },
                                        trailing = { MaterialChevron() },
                                        onClick = { showResetDialog = true }
                                    )
                                }
                            ))
                        } else {
                            ThemedOptionCard(useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).then(aboutBlurModifier), miuixColor = aboutCardColor) {
                                ArrowPreference(title = if (isZh) "导出软件配置" else "Export App Config", onClick = {
                                    scope.launch {
                                        if (!ModuleDetector.requestRoot()) {
                                            Toast.makeText(context, if (isZh) "需要 Root 权限" else "Root access required", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        exportLauncher.launch(if (isZh) "SetoSkins_配置_备份.prop" else "SetoSkins_Config_Backup.prop")
                                    }
                                })
                                ArrowPreference(title = if (isZh) "导入软件配置" else "Import App Config", onClick = {
                                    scope.launch {
                                        if (!ModuleDetector.requestRoot()) {
                                            Toast.makeText(context, if (isZh) "需要 Root 权限" else "Root access required", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        importLauncher.launch(arrayOf("*/*"))
                                    }
                                })
                                ArrowPreference(title = if (isZh) "重置模块配置" else "Reset Module Config", onClick = { showResetDialog = true })
                            }
                        }
                    }
                    item(key = "support") {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (useMonet) {
                            MaterialSegmentedColumn(modifier = Modifier.padding(horizontal = 20.dp), items = listOf(
                                {
                                    MaterialListItem(
                                        headline = if (isZh) "致谢" else "Credits",
                                        leading = { IconTile(Icons.Filled.Info, contentDescription = if (isZh) "致谢" else "Credits") },
                                        trailing = { MaterialChevron() },
                                        onClick = onNavigateToCredits
                                    )
                                },
                                {
                                    MaterialListItem(
                                        headline = if (isZh) "捐赠" else "Donate",
                                        leading = { IconTile(Icons.Filled.Favorite, contentDescription = if (isZh) "捐赠" else "Donate") },
                                        trailing = { MaterialChevron() },
                                        onClick = onNavigateToDonate
                                    )
                                }
                            ))
                        } else {
                            ThemedOptionCard(useMonet = useMonet, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).then(aboutBlurModifier), miuixColor = aboutCardColor) {
                                ArrowPreference(title = if (isZh) "致谢" else "Credits", onClick = onNavigateToCredits)
                                ArrowPreference(title = if (isZh) "捐赠" else "Donate", onClick = onNavigateToDonate)
                            }
                        }
                    }
                    item(key = "check_update") {
                        Spacer(modifier = Modifier.height(16.dp))
                        val checkUpdate: () -> Unit = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                Toast.makeText(context, if (isZh) "正在检查更新..." else "Checking for updates...", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    val info = UpdateManager.checkAppUpdate(context)
                                    isCheckingUpdate = false
                                    if (info.hasUpdate) {
                                        updateInfo = info
                                        showUpdateDialog = true
                                        onDialogVisibilityChange(true)
                                    } else {
                                        Toast.makeText(context, if (isZh) "已是最新版本" else "Already up to date", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        if (useMonet) {
                            MaterialSegmentedColumn(modifier = Modifier.padding(horizontal = 20.dp), items = listOf({
                                MaterialListItem(
                                    headline = if (isZh) "检查更新" else "Check Update",
                                    leading = { IconTile(Icons.Filled.SystemUpdate, contentDescription = if (isZh) "检查更新" else "Check Update") },
                                    trailing = { MaterialChevron() },
                                    onClick = checkUpdate
                                )
                            }))
                        } else {
                            ThemedOptionCard(
                                useMonet = useMonet,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).then(aboutBlurModifier),
                                miuixColor = aboutCardColor,
                            ) {
                                Box(modifier = Modifier.clickable { checkUpdate() }) {
                                    ArrowPreference(
                                        title = if (isZh) "检查更新" else "Check Update",
                                        onClick = null
                                    )
                                }
                            }
                        }
                    }
                    if (floatingNavBar) {
                        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                }
            }
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(listState)
            )
        }

        val aboutBarColor = if (useMonet) {
            MaterialTheme.colorScheme.surfaceContainer
        } else if (isInDark) {
            Color.Black
        } else {
            MiuixTheme.colorScheme.surface
        }

        val aboutBarFadeDistancePx = remember(density) { with(density) { 72.dp.toPx() } }
        val aboutBarAlpha by remember {
            derivedStateOf {
                if (!listState.canScrollForward) {
                    1f
                } else {
                    when (listState.firstVisibleItemIndex) {
                        0 -> 0f
                        1 -> (listState.firstVisibleItemScrollOffset.toFloat() / aboutBarFadeDistancePx).coerceIn(0f, 1f)
                        else -> 1f
                    }
                }
            }
        }
        if (useMonet) {

            TopAppBar(
                title = {
                    MaterialText(
                        text = "关于",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                modifier = Modifier.align(Alignment.TopCenter),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = aboutBarColor.copy(alpha = aboutBarAlpha),
                    scrolledContainerColor = aboutBarColor.copy(alpha = aboutBarAlpha),
                    titleContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = aboutBarAlpha),
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                ),
            )
        } else {

            SmallTopAppBar(
                title = "关于",

                modifier = Modifier.offset(y = (-6).dp),
                color = aboutBarColor.copy(alpha = aboutBarAlpha),
                titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = aboutBarAlpha),
            )
        }
    }
}
