package com.setoskins.thermal.ui.screen

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BypassListPage(
    useMonet: Boolean,
    isZh: Boolean,
    prefs: android.content.SharedPreferences,
    onBack: () -> Unit,
    progressProvider: () -> Float = { 1f }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val bgColor = if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface
    var isLoading by remember { mutableStateOf(true) }
    var allApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var appSet by remember { mutableStateOf(prefs.getStringSet("bypass_apps", emptySet()) ?: emptySet()) }

    LaunchedEffect(Unit) {
        allApps = withContext(Dispatchers.IO) {
            loadThirdPartyApps(context)
        }
        val filePkgs = withContext(Dispatchers.IO) { ModuleDetector.readBypassListPackages() }
        appSet = appSet.toMutableSet().apply { addAll(filePkgs) }
        prefs.edit().putStringSet("bypass_apps", appSet).apply()
        allApps = allApps.sortedByDescending { appSet.contains(it.packageName) }
        isLoading = false
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val toggleApp: (String) -> Unit = { pkg ->
        val newSet = appSet.toMutableSet()
        if (newSet.contains(pkg)) newSet.remove(pkg) else newSet.add(pkg)
        appSet = newSet
        prefs.edit().putStringSet("bypass_apps", newSet).apply()
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { ModuleDetector.writeBypassListPackages(newSet) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val transitionProgress = progressProvider()
                translationX = (1f - transitionProgress) * size.width
            }
            .background(bgColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = if (isZh) "旁路充电名单" else "Bypass Charging List",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isZh = isZh,
                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp, bottom = 12.dp)
            )

            val cardColor = MiuixTheme.colorScheme.surfaceContainer

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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (index == 0) Modifier.padding(top = 16.dp) else Modifier)
                                        .clickable { toggleApp(app.packageName) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                    ThemedSwitch(
                                        checked = isInSet,
                                        onCheckedChange = null,
                                        useMonet = useMonet
                                    )
                                }
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