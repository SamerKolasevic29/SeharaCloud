package com.devfamily.sehara.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devfamily.sehara.ui.screens.home.HomeScreen
import com.devfamily.sehara.ui.screens.landing.LandingScreen
import com.devfamily.sehara.ui.screens.music.ArtistScreen
import com.devfamily.sehara.ui.screens.music.FavoriteScreen
import com.devfamily.sehara.ui.screens.music.GenreScreen
import com.devfamily.sehara.ui.screens.music.MusicFilter
import com.devfamily.sehara.ui.screens.music.MusicHomeScreen
import com.devfamily.sehara.ui.screens.music.MusicListScreen
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel
import com.devfamily.sehara.ui.screens.video.VideoHomeScreen
import com.devfamily.sehara.ui.screens.docs.DocumentsScreen
import com.devfamily.sehara.ui.screens.docs.DocumentViewerScreen
import com.devfamily.sehara.ui.screens.photo.PhotoHostScreen
import com.devfamily.sehara.ui.screens.photo.PhotoViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun AppNavigation(
    openMusic: Boolean = false,
    onOpenMusicHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val playerViewModel: MusicPlayerViewModel = viewModel()
    val photoViewModel: PhotoViewModel = viewModel()
    val startRoute = remember { if (openMusic) Routes.Home.route else Routes.Landing.route }

    LaunchedEffect(openMusic) {
        if (openMusic) {
            navController.navigate(Routes.Music.route) {
                launchSingleTop = true
            }
            onOpenMusicHandled()
        }
    }

    LaunchedEffect(Unit) {
        playerViewModel.openArtist.collect { (id, name) ->
            val entry = navController.currentBackStackEntry
            val sameArtist = entry?.destination?.route == Routes.MusicList.route &&
                    entry.arguments?.getString("type") == "artist" &&
                    entry.arguments?.getString("id") == id
            if (!sameArtist) {
                navController.navigate(Routes.MusicList.createRoute("artist", id, name))
            }
        }
    }
    LaunchedEffect(Unit) {
        playerViewModel.openGenre.collect { (id, name) ->
            val entry = navController.currentBackStackEntry
            val sameGenre = entry?.destination?.route == Routes.MusicList.route &&
                    entry.arguments?.getString("type") == "genre" &&
                    entry.arguments?.getString("id") == id
            if (!sameGenre) {
                navController.navigate(Routes.MusicList.createRoute("genre", id, name))
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161616))
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
                },
                onVideoClick = { navController.navigate(Routes.Video.route) },
                onDocsClick = { navController.navigate(Routes.Docs.route) },
                onPhotoClick = { navController.navigate(Routes.Photo.route) }
            )
        }

        composable(Routes.Music.route) {
            MusicHomeScreen(
                onBackClick = { navController.popBackStack() },
                onArtistClick = { navController.navigate(Routes.Artist.route) },
                onGenreClick = { navController.navigate(Routes.Genre.route) },
                onFavoriteClick = { navController.navigate(Routes.Favorite.route) },
                playerViewModel = playerViewModel
            )
        }

        composable(Routes.Favorite.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                playerViewModel = playerViewModel
            )
        }

        composable(Routes.Video.route) {
            VideoHomeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Docs.route) {
            DocumentsScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { doc ->
                    navController.navigate(
                        Routes.DocumentViewer.createRoute(doc.id, doc.title ?: "Untitled")
                    )
                }
            )
        }

        composable(
            route = Routes.DocumentViewer.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")

            DocumentViewerScreen(
                documentId = id,
                documentName = name,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.Photo.route) {
            PhotoHostScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = photoViewModel
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
                onArtistClick = {
                    navController.navigate(Routes.Artist.route) {
                        popUpTo(Routes.Music.route)
                    }
                },
                onGenreClick = {
                    navController.navigate(Routes.Genre.route) {
                        popUpTo(Routes.Music.route)
                    }
                },
                onFavoriteClick = {
                    navController.navigate(Routes.Favorite.route) {
                        popUpTo(Routes.Music.route)
                    }
                },
                playerViewModel = playerViewModel
            )
        }
    }
}