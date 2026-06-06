package net.vertexgraphics.myfinances.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import net.vertexgraphics.myfinances.MainViewModel
import net.vertexgraphics.myfinances.BillViewModel
import net.vertexgraphics.myfinances.IncomeViewModel

@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Bills") },
                    selected = currentDestination?.hierarchy?.any { it.route == "main" } == true,
                    onClick = {
                        navController.navigate("main") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Income") },
                    selected = currentDestination?.hierarchy?.any { it.route == "income" } == true,
                    onClick = {
                        navController.navigate("income") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "main", modifier = Modifier.padding(padding)) {
            composable("main") {
                MainScreen(
                    viewModel = mainViewModel,
                    onAddBill = { navController.navigate("edit/-1") },
                    onEditBill = { id -> navController.navigate("edit/$id") },
                    onViewLog = { navController.navigate("log") }
                )
            }
            composable(
                "edit/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.IntType })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getInt("billId") ?: -1
                val viewModel: BillViewModel = hiltViewModel()
                BillEditScreen(
                    viewModel = viewModel,
                    billId = billId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("income") {
                val viewModel: IncomeViewModel = hiltViewModel()
                IncomeScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("log") {
                LogScreen(viewModel = mainViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
