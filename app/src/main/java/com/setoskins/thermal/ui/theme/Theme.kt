package com.setoskins.thermal.ui.theme

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private val SukiSuShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MyApplicationTheme(
    useMonet: Boolean = false,
    miuixMonet: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val monetEnabled = useMonet || miuixMonet
    val controller = remember(monetEnabled, darkTheme) {
        ThemeController(
            colorSchemeMode = if (monetEnabled) ColorSchemeMode.MonetSystem else ColorSchemeMode.System,
            isDark = darkTheme
        )
    }

    // MiuixTheme 会在全局把 LocalOverscrollFactory 覆盖为 MiuixOverscrollFactory(miuix 弹簧)。
    // 在进入 MiuixTheme 之前读取一次,拿到的还是平台默认工厂(Android 12+ 原生拉伸 stretch),
    // material(useMonet)风格用它覆盖回去,miuix 风格继续用 miuix 弹簧。
    val platformOverscrollFactory = LocalOverscrollFactory.current

    MiuixTheme(
        controller = controller,
        content = {

            val miuixColors = MiuixTheme.colorScheme
            val miuixMaterialColors = remember(miuixColors.primary, miuixColors.onPrimary, miuixColors.primaryContainer, miuixColors.onPrimaryContainer, miuixColors.secondary, miuixColors.onSecondary, miuixColors.secondaryContainer, miuixColors.onSecondaryContainer, miuixColors.background, miuixColors.onBackground, miuixColors.surface, miuixColors.onSurface, miuixColors.surfaceVariant, miuixColors.surfaceContainer, miuixColors.surfaceContainerHigh, miuixColors.surfaceContainerHighest, miuixColors.onSurfaceSecondary, miuixColors.outline, miuixColors.dividerLine, miuixColors.error, miuixColors.onError, darkTheme) {
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
                    surfaceBright = miuixColors.surface,
                    surfaceContainer = miuixColors.surfaceContainer,
                    surfaceContainerHigh = miuixColors.surfaceContainerHigh,
                    surfaceContainerHighest = miuixColors.surfaceContainerHighest,
                    onSurfaceVariant = miuixColors.onSurfaceSecondary,
                    outline = miuixColors.outline,
                    outlineVariant = miuixColors.dividerLine,
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
                    surfaceBright = miuixColors.surface,
                    surfaceContainer = miuixColors.surfaceContainer,
                    surfaceContainerHigh = miuixColors.surfaceContainerHigh,
                    surfaceContainerHighest = miuixColors.surfaceContainerHighest,
                    onSurfaceVariant = miuixColors.onSurfaceSecondary,
                    outline = miuixColors.outline,
                    outlineVariant = miuixColors.dividerLine,
                    error = miuixColors.error,
                    onError = miuixColors.onError
                )
            }
            }

            val materialColors = if (useMonet) {
                val context = LocalContext.current
                val systemColors = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                rememberDynamicColorScheme(
                    seedColor = systemColors.primary,
                    isDark = darkTheme,
                    style = PaletteStyle.TonalSpot,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025,
                )
            } else {
                miuixMaterialColors
            }

            if (useMonet) {
                // Material 风格：还原平台默认过滚动（Android 12+ 原生拉伸 stretch），
                // 覆盖 MiuixTheme 全局提供的 miuix 弹簧回弹，与 SukiSU-Ultra Material 界面一致。
                CompositionLocalProvider(LocalOverscrollFactory provides platformOverscrollFactory) {
                    MaterialTheme(
                        colorScheme = materialColors,
                        // 与 SukiSU-Ultra 的 Material 风格保持一致：使用 expressive 弹性动画，
                        // 让 Switch 等组件在切换时带有回弹效果。
                        motionScheme = MotionScheme.expressive(),
                        shapes = if (useMonet) SukiSuShapes else MaterialTheme.shapes,
                        content = content,
                    )
                }
            } else {
                MaterialTheme(
                    colorScheme = materialColors,
                    // 与 SukiSU-Ultra 的 Material 风格保持一致：使用 expressive 弹性动画，
                    // 让 Switch 等组件在切换时带有回弹效果。
                    motionScheme = MotionScheme.expressive(),
                    shapes = if (useMonet) SukiSuShapes else MaterialTheme.shapes,
                    content = content,
                )
            }
        }
    )
}
