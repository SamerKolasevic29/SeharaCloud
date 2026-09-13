package com.devfamily.sehara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devfamily.sehara.ui.screens.home.HomeScreen
import com.devfamily.sehara.ui.screens.landing.LandingScreen
import com.devfamily.sehara.ui.theme.SeharaTheme
import com.devfamily.sehara.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeharaTheme {
                AppNavigation()
            }
        }
    }
}