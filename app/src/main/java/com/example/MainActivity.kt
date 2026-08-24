package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DreamDetailScreen
import com.example.ui.screens.DreamJournalHomeScreen
import com.example.ui.screens.RecordDreamScreen
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DreamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: DreamViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() }
                    ) {
                        composable("home") {
                            DreamJournalHomeScreen(
                                viewModel = viewModel,
                                onNavigateToRecord = {
                                    navController.navigate("record")
                                },
                                onSelectDream = { dreamId ->
                                    navController.navigate("detail/$dreamId")
                                }
                            )
                        }

                        composable("record") {
                            RecordDreamScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onDreamCreated = { newDreamId ->
                                    navController.navigate("detail/$newDreamId") {
                                        popUpTo("home")
                                    }
                                }
                            )
                        }

                        composable(
                            route = "detail/{dreamId}",
                            arguments = listOf(navArgument("dreamId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val dreamId = backStackEntry.arguments?.getLong("dreamId") ?: 0L
                            DreamDetailScreen(
                                dreamId = dreamId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
