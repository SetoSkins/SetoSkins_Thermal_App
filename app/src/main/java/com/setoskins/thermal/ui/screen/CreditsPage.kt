package com.setoskins.thermal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.ui.component.VerticalScrollBar
import com.setoskins.thermal.ui.component.rememberScrollBarAdapter
import com.setoskins.thermal.ui.component.animation.customOverScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class CreditEntry(
    val title: String,
    val summary: String,
    val url: String? = null
)

@OptIn(ExperimentalScrollBarApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreditsPage(
    useMonet: Boolean,
    onDismiss: () -> Unit,
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val isDark = isSystemInDarkTheme()
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val scrollBehavior = MiuixScrollBehavior()
    val title = if (isZh) "致谢" else "Credits"

    val primaryEntries = remember(isZh) {
        listOf(
            CreditEntry("SetoSkins Thermal", if (isZh) "Seto温控模块" else "Seto Thermal", "https://github.com/SetoSkins/SetoSkins_Thermal")
        )
    }

    val entries = remember(isZh) {
        if (isZh) listOf(
            CreditEntry("Miuix", "MIUI 风格 Compose 组件库 (yukonga)", "https://github.com/yukonga/Miuix"),
            CreditEntry("HyperExtreme / HyperChanger", "悬浮底栏与动效参考 (22Frederic)", "https://github.com/22Frederic/HyperExtreme"),
            CreditEntry("SukiSU Ultra", "状态卡片样式参考", "https://github.com/SukiSU-Ultra/SukiSU-Ultra")
        ) else listOf(
            CreditEntry("Miuix", "MIUI-style Compose UI library (yukonga)", "https://github.com/yukonga/Miuix"),
            CreditEntry("HyperExtreme / HyperChanger", "Floating nav bar & animations reference (22Frederic)", "https://github.com/22Frederic/HyperExtreme"),
            CreditEntry("SukiSU Ultra", "Status card style reference", "https://github.com/SukiSU-Ultra/SukiSU-Ultra")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val transitionProgress = progressProvider()
                translationX = (1f - transitionProgress) * size.width
                scaleX = 1f
                scaleY = 1f
            }
            .background(if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (useMonet) {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        MaterialIconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        }
                        MaterialText(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    TopAppBar(
                        title = title,
                        largeTitle = title,
                        largeTitleColor = Color.Transparent,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.padding(start = 0.dp)
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Back,
                                    contentDescription = "Back",
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .customOverScroll()
                        .then(if (useMonet) Modifier else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    SmallTitle(
                        text = if (isZh) "温控模块" else "Thermal Module",
                        modifier = Modifier.offset(x = (-12).dp).padding(top = 8.dp, start = 0.dp, bottom = 2.dp)
                    )
                    CreditCard(entries = primaryEntries, useMonet = useMonet, onOpen = { uriHandler.openUri(it) })
                    Spacer(Modifier.height(4.dp))
                    SmallTitle(
                        text = if (isZh) "引用" else "References",
                        modifier = Modifier.offset(x = (-12).dp).padding(top = 8.dp, start = 0.dp, bottom = 2.dp)
                    )
                    CreditCard(entries = entries, useMonet = useMonet, onOpen = { uriHandler.openUri(it) })
                    Spacer(Modifier.height(24.dp))
                }

                VerticalScrollBar(
                    adapter = rememberScrollBarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        if (!useMonet) {
            val collapsedFraction by remember { derivedStateOf { scrollBehavior.state.collapsedFraction } }
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = MiuixTheme.colorScheme.onBackground,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 52.dp, start = 26.dp)
                    .graphicsLayer {
                        val fraction = collapsedFraction.coerceIn(0f, 1f)
                        alpha = (1f - fraction * 3f).coerceIn(0f, 1f)
                        translationY = -fraction * 45.dp.toPx()
                    }
            )
        }
    }
}

@Composable
private fun CreditCard(entries: List<CreditEntry>, useMonet: Boolean, onOpen: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (useMonet) 16.dp else 0.dp, vertical = 4.dp)
        ) {
            entries.forEach { entry ->
                CreditRow(
                    entry = entry,
                    useMonet = useMonet,
                    onClick = { entry.url?.let(onOpen) }
                )
            }
        }
    }
}

@Composable
private fun CreditRow(entry: CreditEntry, useMonet: Boolean, onClick: () -> Unit) {
    if (!useMonet) {
        ArrowPreference(
            title = entry.title,
            summary = entry.summary,
            onClick = if (entry.url != null) onClick else null
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick, enabled = entry.url != null)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.summary,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
            if (entry.url != null) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
