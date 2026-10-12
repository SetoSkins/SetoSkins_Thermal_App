package com.setoskins.thermal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.setoskins.thermal.ui.component.animation.DampedDragAnimation
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MaterialFloatingNavigationBar(
    items: List<NavigationItem>,
    filledIcons: List<ImageVector>? = null,
    selectedIndex: Int,
    pagerState: androidx.compose.foundation.pager.PagerState?,
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

    val animationScope = rememberCoroutineScope()
    // 换页松手后压制 pager 跟随，等 pager 稳定后再恢复，避免两次跳动
    var suppressFollow by remember { mutableStateOf(false) }
    var isIndicatorDragging by remember { mutableStateOf(false) }

    // DampedDragAnimation：与 Miuix 版（IosLiquidGlassNavigationBar）完全相同的拖拽状态机，
    // 无 touch slop、同步状态写入、graphicsLayer 绘制阶段读取，已实测丝滑
    class DragHolder { var instance: DampedDragAnimation? = null }
    val dampedDragHolder = remember { DragHolder() }
    val onItemClickUpdated by rememberUpdatedState(onItemClick)
    val selectedIndexUpdated by rememberUpdatedState(selectedIndex)

    val dampedDrag = remember(animationScope, itemCount) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = selectedIndex.toFloat(),
            valueRange = 0f..(itemCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1f,
            onDragStarted = {
                isIndicatorDragging = true
                val seed = pagerState?.let { it.currentPage + it.currentPageOffsetFraction }
                dampedDragHolder.instance?.syncToPage(seed ?: selectedIndexUpdated.toFloat())
            },
            onDragStopped = {
                isIndicatorDragging = false
                val targetIndex = targetValue.roundToInt().coerceIn(0, itemCount - 1)
                // 无条件压制 pager 跟随：归位动画期间指示器持续读 pagerPosition，
                // 软弹簧 spring(0.85f, 400f) 平滑归位（约 400~600ms 带自然回弹）全程可见
                suppressFollow = true
                if (targetIndex != selectedIndexUpdated) {
                    onItemClickUpdated(targetIndex)
                }
                dampedDragHolder.instance?.settleToPage(targetIndex.toFloat()) {
                    // 归位落定：仅当 pager 已稳定在目标页（不换页时立即满足）才恢复跟随
                    val pagerPos = pagerState
                        ?.let { it.currentPage + it.currentPageOffsetFraction }
                        ?: targetIndex.toFloat()
                    if (abs(pagerPos - targetIndex) < 0.002f) {
                        suppressFollow = false
                    }
                }
            },
            onDrag = { _, dragAmount ->
                updateValue(
                    (targetValue + dragAmount.x / (totalWidth / itemCount))
                        .coerceIn(valueRange),
                )
            },
        ).also { dampedDragHolder.instance = it }
    }

    // 指示器位置（页数 0..count-1）：拖拽/压制期读硬跟随的 pagerPosition，否则读 pager 实时位置
    val indicatorPosition: () -> Float = {
        if (isIndicatorDragging || suppressFollow) {
            dampedDrag.pagerPosition.floatValue
        } else {
            pagerState?.let { it.currentPage + it.currentPageOffsetFraction }
                ?: dampedDrag.pagerPosition.floatValue
        }
    }

    val highlightedIndex by remember {
        derivedStateOf {
            if (itemCount > 0) {
                indicatorPosition().roundToInt().coerceIn(0, itemCount - 1)
            } else {
                selectedIndex
            }
        }
    }

    // pager 跟随：非拖拽时硬同步位置；pager 稳定且处于压制期时解除压制并对齐
    LaunchedEffect(pagerState) {
        val state = pagerState ?: return@LaunchedEffect
        snapshotFlow { state.currentPage + state.currentPageOffsetFraction }.collect { pos ->
            val settled = abs(pos - pos.roundToInt()) < 0.002f
            if (suppressFollow && settled && !isIndicatorDragging) {
                suppressFollow = false
                dampedDrag.syncToPage(pos.roundToInt().toFloat())
            }
            if (!isIndicatorDragging && !suppressFollow) {
                dampedDrag.syncToPage(pos)
            }
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
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected && filledIcons != null) filledIcons[index] else item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) selectedContentColor else unselectedContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
            Box(
                modifier = Modifier
                    // 独立图层平移：绘制阶段读取状态，位置变化零布局零重绘
                    .graphicsLayer {
                        translationX = indicatorPosition() * itemWidth
                    }
                    .width(with(density) { itemWidth.toDp() })
                    .height(56.dp)
                    .background(selectedBgColor, indicatorShape)
                    // 与 Miuix 版同款手势：无 touch slop、按住即拖
                    .then(dampedDrag.modifier)
            )
        }
    }
}
