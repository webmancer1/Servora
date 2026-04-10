package com.example.servora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.servora.ui.account.AccountScreen
import com.example.servora.ui.dashboard.DashboardScreen
import com.example.servora.ui.detail.ServerDetailScreen
import com.example.servora.ui.navigation.BottomNavItem
import com.example.servora.ui.settings.SettingsScreen
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.Charcoal
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.ServoraTheme
import com.example.servora.ui.theme.TextTertiary
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
    val authViewModel: com.example.servora.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isCheckingAuth) {
        Box(modifier = Modifier.fillMaxSize().background(DeepNavy))
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarRoutes = BottomNavItem.items.map { it.route }
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        containerColor = DeepNavy,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(CardBorder)
                    )
                    NavigationBar(
                        containerColor = Charcoal,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        BottomNavItem.items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.1f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "nav_scale"
                            )

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 24.dp, height = 3.dp)
                                                    .clip(RoundedCornerShape(1.5.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                NeonCyan,
                                                                NeonCyan.copy(alpha = 0.4f)
                                                            )
                                                        )
                                                    )
                                                    .offset(y = (-4).dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(3.dp).offset(y = (-4).dp))
                                        }
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .scale(scale)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextTertiary,
                                    unselectedTextColor = TextTertiary,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "dashboard" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                com.example.servora.ui.auth.LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo(0)
                        }
                    },
                    onNavigateToSignUp = { navController.navigate("signup") }
                )
            }

            composable("signup") {
                com.example.servora.ui.auth.SignUpScreen(
                    viewModel = authViewModel,
                    onSignUpSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo(0)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

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

            composable("settings") {
                SettingsScreen()
            }

            composable("account") {
                AccountScreen(
                    onSignOut = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}