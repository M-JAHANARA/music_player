package com.jahanmusic.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jahanmusic.app.screens.*
import com.jahanmusic.app.viewmodel.MusicViewModel

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Access : Screen("access")
    // Swipeable tabs host (0 home, 1 songs, 2 albums, 3 settings)
    object Main : Screen("main?tab={tab}") {
        fun withTab(tab: Int) = "main?tab=$tab"
    }
    object Playback : Screen("playback")
    object Equalizer : Screen("equalizer")
    // Appearance settings (theme.png page) — opened from the settings tab
    object Appearance : Screen("appearance")
    // Kept for backwards-compat (old bottom-bar code paths redirect here)
    object Home : Screen("home")
    object AllSongs : Screen("all_songs")
    object Settings : Screen("settings")
    object Albums : Screen("albums")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val musicVm: MusicViewModel = viewModel()
    NavHost(navController = navController, startDestination = Screen.Loading.route) {
        // Loading is the FIRST page — nothing comes before it.
        composable(Screen.Loading.route) {
            LoadingScreen(onLoadingComplete = {
                // Skip Access forever once permission was granted
                if (musicVm.checkPermission()) {
                    navController.navigate(Screen.Main.withTab(0)) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Access.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            })
        }
        composable(Screen.Access.route) {
            AccessScreen(
                onAccessGranted = {
                    musicVm.onPermissionGranted()
                    navController.navigate(Screen.Main.withTab(0)) {
                        popUpTo(Screen.Access.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Main.route,
            arguments = listOf(navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            MainTabsScreen(
                initialPage = (backStackEntry.arguments?.getInt("tab") ?: 0).coerceIn(0, 3),
                onNavigateToPlayback = { navController.navigate(Screen.Playback.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) },
                vm = musicVm
            )
        }
        composable(Screen.Appearance.route) {
            AppearanceScreen(onNavigateBack = { navController.popBackStack() })
        }
        // Legacy routes redirect into swipeable tabs (preserves old call sites)
        composable(Screen.Home.route) {
            MainTabsScreen(initialPage = 0, onNavigateToPlayback = { navController.navigate(Screen.Playback.route) }, onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }, vm = musicVm)
        }
        composable(Screen.AllSongs.route) {
            MainTabsScreen(initialPage = 1, onNavigateToPlayback = { navController.navigate(Screen.Playback.route) }, onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }, vm = musicVm)
        }
        composable(Screen.Albums.route) {
            MainTabsScreen(initialPage = 2, onNavigateToPlayback = { navController.navigate(Screen.Playback.route) }, onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }, vm = musicVm)
        }
        composable(Screen.Settings.route) {
            MainTabsScreen(initialPage = 3, onNavigateToPlayback = { navController.navigate(Screen.Playback.route) }, onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }, vm = musicVm)
        }
        composable(Screen.Playback.route) {
            PlaybackScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                vm = musicVm
            )
        }
        composable(Screen.Equalizer.route) {
            EqualizerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Main.withTab(0)) },
                onNavigateToAllSongs = { navController.navigate(Screen.Main.withTab(1)) },
                onNavigateToSettings = { navController.navigate(Screen.Main.withTab(3)) },
            )
        }
    }
}
