package com.devfamily.sehara.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devfamily.sehara.ui.screens.home.HomeScreen
import com.devfamily.sehara.ui.screens.landing.LandingScreen
import com.devfamily.sehara.ui.screens.music.MusicHomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Landing.route
    ) {
        composable(Routes.Landing.route) {
            LandingScreen(
                onTimeout = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onMusicClick = {
                    navController.navigate(Routes.Music.route)
                }
            )
        }

        composable(Routes.Music.route) {
            MusicHomeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}