package com.devfamily.sehara.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devfamily.sehara.ui.screens.home.HomeScreen
import com.devfamily.sehara.ui.screens.landing.LandingScreen
import com.devfamily.sehara.ui.screens.music.ArtistScreen
import com.devfamily.sehara.ui.screens.music.GenreScreen
import com.devfamily.sehara.ui.screens.music.MusicFilter
import com.devfamily.sehara.ui.screens.music.MusicHomeScreen
import com.devfamily.sehara.ui.screens.music.MusicListScreen
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val playerViewModel: MusicPlayerViewModel = viewModel()

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
                onBackClick = { navController.popBackStack() },
                onArtistClick = { navController.navigate(Routes.Artist.route) },
                onGenreClick = { navController.navigate(Routes.Genre.route) },
                playerViewModel = playerViewModel
            )
        }

        composable(Routes.Artist.route) {
            ArtistScreen(
                showInitialSplash = true,
                onBackClick = { navController.popBackStack() },
                onArtistClick = { id, name ->
                    navController.navigate(Routes.MusicList.createRoute("artist", id, name))
                },
                playerViewModel = playerViewModel
            )
        }
        composable(Routes.Genre.route) {
            GenreScreen(
                showInitialSplash = true,
                onBackClick = { navController.popBackStack() },
                onGenreClick = { id, name ->
                    navController.navigate(Routes.MusicList.createRoute("genre", id, name))
                },
                playerViewModel = playerViewModel
            )
        }

        composable(
            route = Routes.MusicList.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")

            val filter = when (type) {
                "artist" -> MusicFilter.ByArtist(id, name)
                "genre" -> MusicFilter.ByGenre(id, name)
                else -> MusicFilter.ByArtist(id, name)
            }

            MusicListScreen(
                filter = filter,
                onBackClick = { navController.popBackStack() },
                playerViewModel = playerViewModel
            )
        }
    }
}