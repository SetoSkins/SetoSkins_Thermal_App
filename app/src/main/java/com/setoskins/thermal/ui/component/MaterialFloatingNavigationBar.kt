package com.setoskins.thermal.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Composable
fun MaterialFloatingNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = MiuixTheme.colorScheme.background
    val accentColor = MiuixTheme.colorScheme.primary
    val selectedBgColor = if (isDark) accentColor.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f)
    val selectedContentColor = if (isDark) Color.White else accentColor
    val unselectedContentColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF5F6368)

    var totalWidth by remember { mutableFloatStateOf(0f) }
    val itemCount = items.size
    val density = LocalDensity.current

    val indicatorOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val dragScope = rememberCoroutineScope()

    val highlightedIndex by remember {
        derivedStateOf {
            if (totalWidth > 0f && itemCount > 0) {
                val itemWidth = totalWidth / itemCount
                (indicatorOffset.value / itemWidth).roundToInt().coerceIn(0, itemCount - 1)
            } else {
                selectedIndex
            }
        }
    }

    LaunchedEffect(selectedIndex, totalWidth) {
        if (totalWidth > 0f && itemCount > 0 && !isDragging) {
            val targetOffset = (totalWidth / itemCount) * selectedIndex
            indicatorOffset.animateTo(
                targetValue = targetOffset,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    val containerShape = CircleShape
    val indicatorShape = CircleShape

    val navBarBottomPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()
    val bottomPadding = if (navBarBottomPadding != 0.dp) 8.dp + navBarBottomPadding else 36.dp

    Box(
        modifier = modifier
            .padding(horizontal = 68.dp)
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
            .height(56.dp)
            .background(containerColor, containerShape)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { totalWidth = it.width.toFloat() },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == highlightedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onItemClick(index) }
                        ),
                    verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) selectedContentColor else unselectedContentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isSelected) selectedContentColor else unselectedContentColor,
                        maxLines = 1
                    )
                }
            }
        }

        // 选中指示器（胶囊形，匹配 MiuiX，支持拖拽切换页面）
        if (totalWidth > 0f && itemCount > 0) {
            val itemWidth = totalWidth / itemCount
            val indicatorOffsetPx = indicatorOffset.value
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = indicatorOffsetPx.roundToInt(), y = 0) }
                    .width(with(density) { itemWidth.toDp() })
                    .height(56.dp)
                    .background(selectedBgColor, indicatorShape)
                    .pointerInput(selectedIndex, itemCount) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val targetIndex = (indicatorOffset.value / itemWidth)
                                    .roundToInt()
                                    .coerceIn(0, itemCount - 1)
                                if (targetIndex != selectedIndex) {
                                    onItemClick(targetIndex)
                                } else {
                                    dragScope.launch {
                                        indicatorOffset.animateTo(
                                            itemWidth * selectedIndex,
                                            tween(durationMillis = 300)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                dragScope.launch {
                                    indicatorOffset.animateTo(
                                        itemWidth * selectedIndex,
                                        tween(durationMillis = 300)
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newValue = (indicatorOffset.value + dragAmount)
                                    .coerceIn(0f, (itemCount - 1) * itemWidth)
                                dragScope.launch {
                                    indicatorOffset.snapTo(newValue)
                                }
                            }
                        )
                    }
            )
        }
    }
}