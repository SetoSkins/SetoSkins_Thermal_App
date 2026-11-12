package com.setoskins.thermal

import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.setoskins.thermal.ui.navigation.MyApplicationApp
import com.setoskins.thermal.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applySystemBars()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedUseMonet = prefs.getBoolean("useMonet", false)
        val savedMiuixMonet = prefs.getBoolean("miuixMonet", false)
        val savedFloatingNavBar = prefs.getBoolean("floatingNavBar", false)

        setContent {
            var useMonet by remember { mutableStateOf(savedUseMonet) }
            var miuixMonet by remember { mutableStateOf(savedMiuixMonet) }
            var floatingNavBar by remember { mutableStateOf(savedFloatingNavBar) }
            MyApplicationTheme(useMonet = useMonet, miuixMonet = miuixMonet) {
                MyApplicationApp(
                    useMonet = useMonet,
                    onUseMonetChange = { value ->
                        useMonet = value
                        prefs.edit().putBoolean("useMonet", value).apply()
                    },
                    miuixMonet = miuixMonet,
                    onMiuixMonetChange = { value ->
                        miuixMonet = value
                        prefs.edit().putBoolean("miuixMonet", value).apply()
                    },
                    floatingNavBar = floatingNavBar,
                    onFloatingNavBarChange = { value ->
                        floatingNavBar = value
                        prefs.edit().putBoolean("floatingNavBar", value).apply()
                    }
                )
            }
        }
    }

    /**
     * 系统栏沉浸（参考 HyperExtreme 的 applyAppSystemBars）：
     *
     * 1. 状态栏 / 导航条（小白条）两个 scrim 都用**全透明**。`SystemBarStyle.auto` 的默认
     *    scrim 是半透明遮罩，会盖在状态栏/导航条后面，看起来就不沉浸。
     * 2. 关掉导航条的「对比度强制」：系统默认会在透明导航条后面再糊一层 scrim 保证图标可见，
     *    即使 bar 本身透明也能看出色差（HyperOS 上就是一条灰条）。
     * 3. 图标明暗用本 App 的深浅色判定（这里跟随系统，与 MyApplicationTheme 一致）。
     */
    @Suppress("DEPRECATION")
    private fun applySystemBars() {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            ) { _ -> isDark },
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            ) { _ -> isDark }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme(useMonet = false) {
        MyApplicationApp(useMonet = false, onUseMonetChange = {}, miuixMonet = false, onMiuixMonetChange = {}, floatingNavBar = false, onFloatingNavBarChange = {})
    }
}
