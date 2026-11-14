package com.setoskins.thermal.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 风格大标题顶栏（实现方式参考 SukiSU-Ultra）：
 * 使用 [LargeFlexibleTopAppBar] 自带的大标题（Material DisplaySmall 字号）与
 * [TopAppBarDefaults.exitUntilCollapsedScrollBehavior]，内容滚动时大标题平滑收起为小标题。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialLargeTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    LargeFlexibleTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        // 与 SukiSU 一致：常态与收起态都用 surfaceContainer，和页面底色保持统一
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior,
    )
}

/**
 * Material 子页面顶栏返回按钮（样式与 SukiSU 的 TopBarBackButton 一致）：
 * 圆形 surfaceContainerHighest 底色 + onSurfaceVariant 图标。
 */
@Composable
fun MaterialTopBarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    IconButton(
        modifier = modifier.padding(horizontal = 10.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = contentDescription,
        )
    }
}

/**
 * Material 风格页面容器：顶栏大标题 + 内容区（参考 SukiSU-Ultra 的 Scaffold + LargeFlexibleTopAppBar 结构）。
 * content 回调参数为顶栏当前高度（用作列表 contentPadding 的 top）和需要在列表 Modifier 上挂载的嵌套滚动连接。
 *
 * 注意：连接必须挂在列表自身的 Modifier 上、且位于自定义 overscroll 之后（即更靠近列表），
 * 否则顶部下拉的越界手势会先被 overscroll 吃掉，导致大标题无法拉回展开。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialPageTopBarScaffold(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (topPadding: Dp, scrollConnection: NestedScrollConnection) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            MaterialLargeTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                actions = actions,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(innerPadding.calculateTopPadding(), scrollBehavior.nestedScrollConnection)
        }
    }
}
