package com.example.servora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.servora.ui.dashboard.DashboardScreen
import com.example.servora.ui.detail.ServerDetailScreen
import com.example.servora.ui.theme.ServoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ServoraTheme {
                ServoraNavHost()
            }
        }
    }
}

@Composable
fun ServoraNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                onServerClick = { serverId ->
                    navController.navigate("detail/$serverId")
                }
            )
        }

        composable(
            route = "detail/{serverId}",
            arguments = listOf(
                navArgument("serverId") { type = NavType.StringType }
            )
        ) {
            ServerDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}