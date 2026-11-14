package com.setoskins.thermal.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round

private val SegmentedOuterRadius = 24.dp
private val SegmentedInnerRadius = 6.dp

/**
 * SukiSU-Ultra 风格的分段设置卡片容器。
 *
 * 外层首项/末项使用 24dp 圆角，中间项使用 6dp 圆角，项与项之间保留 2dp 背景间隙，
 * 每项底色为 surfaceBright，与 SukiSU 的 Material 设置页一致。
 */
@Composable
fun MaterialSegmentedColumn(
    modifier: Modifier = Modifier,
    items: List<@Composable () -> Unit>,
) {
    if (items.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEachIndexed { index, item ->
            val top = if (index == 0) SegmentedOuterRadius else SegmentedInnerRadius
            val bottom = if (index == items.lastIndex) SegmentedOuterRadius else SegmentedInnerRadius
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = top,
                    topEnd = top,
                    bottomStart = bottom,
                    bottomEnd = bottom,
                ),
                color = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) { item() }
        }
    }
}

/**
 * SukiSU-Ultra 风格的分段列表项，带按压缩放与触感反馈。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "MaterialListItemScale",
    )
    LaunchedEffect(pressed) {
        if (pressed && onClick != null && enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier
                        .scale(scale)
                        .clickable(
                            enabled = enabled,
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = onClick,
                        )
                } else {
                    Modifier
                }
            )
            .graphicsLayer { alpha = if (enabled) 1f else 0.38f }
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
            if (supporting != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(16.dp))
            trailing()
        }
    }
}

/**
 * 带尾部开关的分段设置项（开关不可单独点击，点击整行切换）。
 */
@Composable
fun MaterialSwitchItem(
    headline: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    MaterialListItem(
        headline = headline,
        supporting = supporting,
        leading = leading,
        enabled = enabled,
        modifier = modifier,
        onClick = if (enabled) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onCheckedChange(!checked)
            }
        } else {
            null
        },
        trailing = {
            ThemedSwitch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                useMonet = true,
            )
        },
    )
}

@Composable
private fun OffsetAnchoredExpressiveMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    anchorOffset: IntOffset = IntOffset.Zero,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(Constraints())
            val width = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
            layout(width, 0) {
                placeable.place(anchorOffset.x, anchorOffset.y)
            }
        }
    ) {
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
                modifier = Modifier.verticalScroll(rememberScrollState()),
                content = content,
            )
        }
    }
}

private fun Modifier.trackPressPosition(onPress: (Offset) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onPress(down.position)
    }
}

/**
 * 尾部展示当前值并可下拉选择的分段设置项。
 */
@Composable
fun MaterialDropdownItem(
    headline: String,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    var anchorOffset by remember { mutableStateOf(IntOffset.Zero) }
    val safeIndex = if (items.isNotEmpty()) selectedIndex.coerceIn(0, items.lastIndex) else -1
    Box(modifier = modifier.trackPressPosition { anchorOffset = it.round() }) {
        MaterialListItem(
            headline = headline,
            supporting = supporting,
            leading = leading,
            enabled = enabled,
            onClick = if (enabled) {
                { expanded = true }
            } else {
                null
            },
            trailing = {
                Text(
                    text = if (safeIndex >= 0) items[safeIndex] else "",
                    // 与 SukiSU 的列表尾部值一致：bodyMedium，选中值用主色
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.End,
                )
            },
        )
        OffsetAnchoredExpressiveMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            anchorOffset = anchorOffset,
        ) {
            items.forEachIndexed { index, text ->
                // 与 SukiSU 完全一致：SelectableDropdownMenuItem（ListItem 行高 + bodyLarge 字号）
                SelectableDropdownMenuItem(
                    text = { Text(text) },
                    selected = index == safeIndex,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onItemSelected(index)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(index = index, count = items.size),
                    selectedLeadingIcon = {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                        )
                    },
                )
            }
        }
    }
}

/**
 * SukiSU-Ultra 设置页的图标瓷片：36dp 圆角方块，secondaryContainer 底色。
 */
@Composable
fun IconTile(icon: ImageVector, contentDescription: String? = null) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/**
 * 分段列表项尾部的向右箭头，颜色与 SukiSU 一致（onSurfaceVariant）。
 */
@Composable
fun MaterialChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
