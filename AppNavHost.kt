package com.petmorph.ai.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.petmorph.ai.ui.CompanionViewModel
import com.petmorph.ai.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CREATE = "create"
    const val SETUP = "setup/{companionId}"
    const val PREVIEW = "preview/{companionId}"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val FLOATING_CONTROLS = "floating_controls/{companionId}"

    fun setup(id: String) = "setup/$id"
    fun preview(id: String) = "preview/$id"
    fun floatingControls(id: String) = "floating_controls/$id"
}

@Composable
fun AppNavHost(vm: CompanionViewModel = viewModel()) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(nav, vm) }
        composable(Routes.ONBOARDING) { OnboardingScreen(nav, vm) }
        composable(Routes.HOME) { HomeScreen(nav, vm) }
        composable(Routes.CREATE) { CreateCompanionScreen(nav, vm) }
        composable(
            Routes.SETUP,
            arguments = listOf(navArgument("companionId") { type = NavType.StringType })
        ) { SetupScreen(nav, vm, it.arguments?.getString("companionId").orEmpty()) }
        composable(
            Routes.PREVIEW,
            arguments = listOf(navArgument("companionId") { type = NavType.StringType })
        ) { PreviewScreen(nav, vm, it.arguments?.getString("companionId").orEmpty()) }
        composable(Routes.LIBRARY) { LibraryScreen(nav, vm) }
        composable(Routes.SETTINGS) { SettingsScreen(nav, vm) }
        composable(
            Routes.FLOATING_CONTROLS,
            arguments = listOf(navArgument("companionId") { type = NavType.StringType })
        ) { FloatingControlsScreen(nav, vm, it.arguments?.getString("companionId").orEmpty()) }
    }
}
