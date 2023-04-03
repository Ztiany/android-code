package me.ztiany.compose.realistic

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import me.ztiany.compose.facilities.widget.EntranceList
import me.ztiany.compose.facilities.widget.buildEntrances
import me.ztiany.compose.facilities.widget.buildNavigation

private const val REALISTIC_PAGE = "realistic_page"
private const val REALISTIC_INTERNAL_PAGE = "realistic_internal_page"

@Composable
private fun RealisticScreen(navController: NavHostController) {
    EntranceList(entranceList = buildEntrances(entrances, navController))
}

private val entrances = linkedMapOf<String, @Composable (NavBackStackEntry) -> Unit>(
    "Google Basic Layout CodeLab" to { GoogleBaseLayoutCodeLab() },
)

fun NavController.navigateToRealistic() {
    this.navigate(REALISTIC_PAGE)
}

fun NavGraphBuilder.realisticScreen(navController: NavHostController) {
    buildNavigation(REALISTIC_PAGE, REALISTIC_INTERNAL_PAGE, entrances) {
        RealisticScreen(navController)
    }
}