package com.setoskins.thermal.ui.screen

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.data.ModuleDetector
import com.setoskins.thermal.ui.component.ThemedSwitch
import com.setoskins.thermal.ui.component.ThemedTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)

@Composable
fun BlacklistPage(
    useMonet: Boolean,
    isZh: Boolean,
    prefs: android.content.SharedPreferences,
    onBack: () -> Unit,
    progressProvider: () -> Float = { 1f },
    mode: String = "blacklist"
) {
    val isWhitelist = mode == "whitelist"
    var isWhitelistMode by remember { mutableStateOf(false) }
    val effectiveWhitelist = isWhitelist && isWhitelistMode
    val prefsKey = if (effectiveWhitelist) "whitelist_apps" else "blacklist_apps"
    val title = if (isWhitelist) "黑白名单" else "黑名单"
    val configPath = "/data/adb/modules/SetoSkins/黑名单.prop"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val bgColor = if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface
    var isLoading by remember { mutableStateOf(true) }
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var appSet by remember { mutableStateOf(prefs.getStringSet(prefsKey, emptySet()) ?: emptySet()) }
    var appCurrentValues by remember { mutableStateOf(emptyMap<String, String>()) }

    LaunchedEffect(Unit) {
        allApps = withContext(Dispatchers.IO) {
            loadThirdPartyApps(context)
        }
        if (isWhitelist) {
            val filePkgs = withContext(Dispatchers.IO) { ModuleDetector.readWhitelistAppPackages() }
            appSet = appSet.toMutableSet().apply { addAll(filePkgs) }
            prefs.edit().putStringSet(prefsKey, appSet).apply()
        } else {
            appCurrentValues = withContext(Dispatchers.IO) { ModuleDetector.readAppConfig(configPath) }
        }
        allApps = allApps.sortedByDescending { appSet.contains(it.packageName) }
        isLoading = false
    }

    LaunchedEffect(isWhitelistMode) {
        appSet = prefs.getStringSet(prefsKey, emptySet()) ?: emptySet()
        allApps = allApps.sortedByDescending { appSet.contains(it.packageName) }
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    suspend fun syncAppConfig() {
        withContext(Dispatchers.IO) {
            val entries = appCurrentValues.filterKeys { appSet.contains(it) }.filterValues { it.isNotEmpty() }
            ModuleDetector.writeAppConfig(configPath, entries)
        }
    }

    val toggleApp: (String) -> Unit = { pkg ->
        val newSet = appSet.toMutableSet()
        if (newSet.contains(pkg)) newSet.remove(pkg) else newSet.add(pkg)
        appSet = newSet
        prefs.edit().putStringSet(prefsKey, newSet).apply()
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isWhitelist) {
            scope.launch { ModuleDetector.writeWhitelistAppPackages(newSet) }
        } else {
            scope.launch { syncAppConfig() }
        }
    }

    val updateAppCurrent: (String, String) -> Unit = { pkg, value ->
        appCurrentValues = appCurrentValues.toMutableMap().apply { put(pkg, value) }
        prefs.edit().putString("${prefsKey}_current_$pkg", value).apply()
        scope.launch { syncAppConfig() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val transitionProgress = progressProvider()
                translationX = (1f - transitionProgress) * size.width
                scaleX = 1f
                scaleY = 1f
            }
            .background(bgColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── 顶部栏（固定） ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = if (isZh) "返回" else "Back",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() }
                        .padding(12.dp),
                    tint = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
            // ── 白名单模式切换（仅黑白名单页显示） ──
            if (isWhitelist) {
                // ── 模式说明 Tips ──
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isZh) "黑名单模式：列表中的应用将不受温控限制" else "Blacklist mode: Apps in the list will not be throttled.",
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = if (isZh) "白名单模式：列表之外的应用将不受温控限制" else "Whitelist mode: Apps not in the list will not be throttled.",
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    cornerRadius = 20.dp,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    )
                ) {
                    BasicComponent(
                        title = if (isZh) (if (isWhitelistMode) "白名单模式" else "黑名单模式") else (if (isWhitelistMode) "Whitelist Mode" else "Blacklist Mode"),
                        endActions = {
                            ThemedSwitch(
                                checked = isWhitelistMode,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = {
                            val newMode = !isWhitelistMode
                            isWhitelistMode = newMode
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                ModuleDetector.updateConfig("黑白名单", if (newMode) "白名单" else "黑名单")
                            }
                        }
                    )
                }
            }

            // ── 搜索栏（固定） ──
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isZh = isZh,
                useMonet = useMonet,
                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp, bottom = 12.dp)
            )

            val cardColor = MiuixTheme.colorScheme.surfaceContainer

            // ── 应用列表 / 加载中 ──
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(size = 24.dp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        colors = CardDefaults.defaultColors(
                            color = cardColor,
                            contentColor = MiuixTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            filteredApps.forEachIndexed { index, app ->
                                val isInSet = appSet.contains(app.packageName)
                                AppListItem(
                                    app = app,
                                    isBlacklisted = isInSet,
                                    currentValue = appCurrentValues[app.packageName] ?: "",
                                    onToggle = { toggleApp(app.packageName) },
                                    onCurrentValueChange = { updateAppCurrent(app.packageName, it) },
                                    showCurrentField = !isWhitelist,
                                    useMonet = useMonet,
                                    modifier = if (index == 0) Modifier.padding(top = 16.dp) else Modifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isZh: Boolean,
    useMonet: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = MiuixTheme.colorScheme.surfaceContainerHigh
    val textColor = MiuixTheme.colorScheme.onSurfaceContainerHigh

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 45.dp)
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp).size(20.dp),
            tint = textColor
        )
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = if (isZh) "搜索应用名称或包名" else "Search app name or package",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: AppInfo,
    isBlacklisted: Boolean,
    currentValue: String,
    onToggle: () -> Unit,
    onCurrentValueChange: (String) -> Unit,
    showCurrentField: Boolean,
    useMonet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── 应用图标 ──
            val iconBitmap = remember(app.packageName) {
                app.icon?.let { drawable ->
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
            }
            if (iconBitmap != null) {
                Image(
                    painter = BitmapPainter(iconBitmap.asImageBitmap()),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.first().uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── 应用名 + 包名 ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── 开关 ──
            ThemedSwitch(
                checked = isBlacklisted,
                onCheckedChange = null,
                useMonet = useMonet
            )
        }

        // ── 修改最大电流数（仅黑名单页面显示） ──
        if (showCurrentField) {
            AnimatedVisibility(
                visible = isBlacklisted,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ThemedTextField(
                    value = currentValue,
                    onValueChange = onCurrentValueChange,
                    label = "22A＝22000mA＝22000000",
                    useMonet = useMonet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } }
                )
            }
            if (isBlacklisted) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}


private fun loadThirdPartyApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }.filter { app ->
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map { app ->
            AppInfo(
                packageName = app.packageName,
                appName = app.loadLabel(pm).toString(),
                icon = app.loadIcon(pm)
            )
        }.sortedBy { it.appName.lowercase() }
    } catch (e: Exception) {
        emptyList()
    }
}