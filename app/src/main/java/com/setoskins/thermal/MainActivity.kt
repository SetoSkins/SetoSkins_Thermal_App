package com.setoskins.thermal

import android.graphics.Color as AndroidColor
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
        val defaultNavBarColor = window.navigationBarColor
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = defaultNavBarColor,
                darkScrim = defaultNavBarColor
            )
        )

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedUseMonet = prefs.getBoolean("useMonet", false)

        setContent {
            var useMonet by remember { mutableStateOf(savedUseMonet) }
            MyApplicationTheme(useMonet = useMonet) {
                MyApplicationApp(
                    useMonet = useMonet,
                    onUseMonetChange = { value ->
                        useMonet = value
                        prefs.edit().putBoolean("useMonet", value).apply()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme(useMonet = false) {
        MyApplicationApp(useMonet = false, onUseMonetChange = {})
    }
}