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
                    },

                    initialPage = 0,
                    onPageChange = {}
                )
            }
        }
    }

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
        MyApplicationApp(useMonet = false, onUseMonetChange = {}, miuixMonet = false, onMiuixMonetChange = {}, floatingNavBar = false, onFloatingNavBarChange = {}, initialPage = 0, onPageChange = {})
    }
}
