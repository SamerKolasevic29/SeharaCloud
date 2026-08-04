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
import com.devfamily.sehara.ui.screens.video.VideoHomeScreen
import com.devfamily.sehara.ui.screens.video.VideoCategoryScreen
import com.devfamily.sehara.ui.screens.video.VideoCategory
import com.devfamily.sehara.ui.screens.docs.DocumentsScreen
import com.devfamily.sehara.ui.screens.docs.DocumentViewerScreen
import com.devfamily.sehara.ui.screens.docs.DocsCategoryScreen
import com.devfamily.sehara.ui.screens.docs.DocsCategory

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
                },
                onVideoClick = { navController.navigate(Routes.Video.route) },
                onDocsClick = { navController.navigate(Routes.Docs.route) }
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

        composable(Routes.Video.route) {
            VideoHomeScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = {
                    navController.navigate(Routes.VideoCategory.createRoute("movie"))
                },
                onOtherClick = {
                    navController.navigate(Routes.VideoCategory.createRoute("other"))
                },
                onDocumentaryClick = {
                    navController.navigate(Routes.VideoCategory.createRoute("documentary"))
                }
            )
        }

        composable(
            route = Routes.VideoCategory.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryArg = backStackEntry.arguments?.getString("category") ?: "movie"
            val category = when (categoryArg) {
                "documentary" -> VideoCategory.DOCUMENTARY
                "other" -> VideoCategory.OTHER
                else -> VideoCategory.MOVIE
            }

            VideoCategoryScreen(
                category = category,
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
                },
                onDocumentsCategoryClick = {
                    navController.navigate(Routes.DocsCategory.createRoute("documents"))
                },
                onBooksClick = {
                    navController.navigate(Routes.DocsCategory.createRoute("books"))
                },
                onOthersClick = {
                    navController.navigate(Routes.DocsCategory.createRoute("others"))
                }
            )
        }

        composable(
            route = Routes.DocsCategory.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryArg = backStackEntry.arguments?.getString("category") ?: "documents"
            val category = when (categoryArg) {
                "books" -> DocsCategory.BOOKS
                "others" -> DocsCategory.OTHERS
                else -> DocsCategory.DOCUMENTS
            }

            DocsCategoryScreen(
                category = category,
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