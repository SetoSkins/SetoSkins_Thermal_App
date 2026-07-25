package com.setoskins.thermal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * App 主题入口。
 *
 * - useMonet = true  : 使用 ColorSchemeMode.MonetSystem(Material You / Monet 动态取色)
 * - useMonet = false : 使用 ColorSchemeMode.System(MIUIX 自己的浅蓝/深色主题)
 *
 * 为了让 Material3 组件(Switch / NavigationBar / OutlinedTextField 等)在两种主题下
 * 都能跟当前主题保持一致(而不是用 Material3 默认的紫色),这里把 MIUIX 的 colorScheme
 * 同步映射到一个 Material3 ColorScheme 并通过 MaterialTheme 包裹子树。
 */
@Composable
fun MyApplicationTheme(
    useMonet: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val controller = remember(useMonet, darkTheme) {
        ThemeController(
            colorSchemeMode = if (useMonet) ColorSchemeMode.MonetSystem else ColorSchemeMode.System,
            isDark = darkTheme
        )
    }

    MiuixTheme(
        controller = controller,
        content = {
            // 把 MIUIX 主题色同步到 Material3,这样 Material3 组件
            // 在 Material You / MIUIX 模式下都能遵循当前主题
            val miuixColors = MiuixTheme.colorScheme
            val materialColors = remember(miuixColors, darkTheme) {
            if (darkTheme) {
                darkColorScheme(
                    primary = miuixColors.primary,
                    onPrimary = miuixColors.onPrimary,
                    primaryContainer = miuixColors.primaryContainer,
                    onPrimaryContainer = miuixColors.onPrimaryContainer,
                    secondary = miuixColors.secondary,
                    onSecondary = miuixColors.onSecondary,
                    secondaryContainer = miuixColors.secondaryContainer,
                    onSecondaryContainer = miuixColors.onSecondaryContainer,
                    background = miuixColors.background,
                    onBackground = miuixColors.onBackground,
                    surface = miuixColors.surface,
                    onSurface = miuixColors.onSurface,
                    surfaceVariant = miuixColors.surfaceVariant,
                    onSurfaceVariant = miuixColors.onSurfaceSecondary,
                    outline = miuixColors.outline,
                    error = miuixColors.error,
                    onError = miuixColors.onError
                )
            } else {
                lightColorScheme(
                    primary = miuixColors.primary,
                    onPrimary = miuixColors.onPrimary,
                    primaryContainer = miuixColors.primaryContainer,
                    onPrimaryContainer = miuixColors.onPrimaryContainer,
                    secondary = miuixColors.secondary,
                    onSecondary = miuixColors.onSecondary,
                    secondaryContainer = miuixColors.secondaryContainer,
                    onSecondaryContainer = miuixColors.onSecondaryContainer,
                    background = miuixColors.background,
                    onBackground = miuixColors.onBackground,
                    surface = miuixColors.surface,
                    onSurface = miuixColors.onSurface,
                    surfaceVariant = miuixColors.surfaceVariant,
                    onSurfaceVariant = miuixColors.onSurfaceSecondary,
                    outline = miuixColors.outline,
                    error = miuixColors.error,
                    onError = miuixColors.onError
                )
            }
            }

            MaterialTheme(
                colorScheme = materialColors,
                content = content
            )
        }
    )
}
