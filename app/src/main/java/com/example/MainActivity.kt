package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AyraRoutes
import com.example.ui.AyraViewModel
import com.example.ui.screens.AccessibilitySetupScreen
import com.example.ui.screens.CommandHistoryScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.FirstLaunchSetupScreen
import com.example.ui.screens.MainVoiceAssistantScreen
import com.example.ui.screens.NotesRemindersScreen
import com.example.ui.screens.PermissionDashboardScreen
import com.example.ui.screens.PrivacyTroubleshootingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AyraTheme {
                val viewModel: AyraViewModel = viewModel()
                val navController = rememberNavController()
                val setupCompleted by viewModel.preferences.setupCompleted.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AyraMidnightBg
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AyraRoutes.SPLASH,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(AyraRoutes.SPLASH) {
                            SplashScreen(
                                isFirstLaunch = !setupCompleted,
                                onNavigateNext = { dest ->
                                    navController.navigate(dest) {
                                        popUpTo(AyraRoutes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(AyraRoutes.SETUP) {
                            FirstLaunchSetupScreen(
                                onComplete = {
                                    viewModel.preferences.setSetupCompleted(true)
                                    navController.navigate(AyraRoutes.MAIN) {
                                        popUpTo(AyraRoutes.SETUP) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(AyraRoutes.MAIN) {
                            MainVoiceAssistantScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { navController.navigate(AyraRoutes.SETTINGS) }
                            )
                        }

                        composable(AyraRoutes.SETTINGS) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable(AyraRoutes.ACCESSIBILITY) {
                            AccessibilitySetupScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AyraRoutes.PERMISSIONS) {
                            PermissionDashboardScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AyraRoutes.HISTORY) {
                            CommandHistoryScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onReExecute = { cmd ->
                                    navController.popBackStack()
                                    viewModel.handleRecognizedInput(cmd)
                                }
                            )
                        }

                        composable(AyraRoutes.NOTES) {
                            NotesRemindersScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AyraRoutes.FILES) {
                            FileManagerScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AyraRoutes.PRIVACY) {
                            PrivacyTroubleshootingScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
