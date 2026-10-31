package com.devfamily.sehara

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.devfamily.sehara.ui.navigation.AppNavigation
import com.devfamily.sehara.ui.theme.SeharaTheme

class MainActivity : ComponentActivity() {

    private var openMusic by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        openMusic = savedInstanceState == null && intent?.action == ACTION_OPEN_MUSIC
        setContent {
            SeharaTheme {
                AppNavigation(
                    openMusic = openMusic,
                    onOpenMusicHandled = { openMusic = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_OPEN_MUSIC) openMusic = true
    }

    companion object {
        const val ACTION_OPEN_MUSIC = "com.devfamily.sehara.OPEN_MUSIC"
    }
}