package com.example.piddepartures.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.piddepartures.ui.add.AddStopScreen
import com.example.piddepartures.ui.detail.DetailScreen
import com.example.piddepartures.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddStop : Screen("add_stop")
    object Detail : Screen("detail/{stopId}") {
        fun createRoute(stopId: Long) = "detail/$stopId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddStop.route)
                },
                onNavigateToDetail = { stopId ->
                    navController.navigate(Screen.Detail.createRoute(stopId))
                }
            )
        }
        
        composable(Screen.AddStop.route) {
            AddStopScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("stopId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val stopId = backStackEntry.arguments?.getLong("stopId") ?: 0L
            DetailScreen(
                stopId = stopId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
