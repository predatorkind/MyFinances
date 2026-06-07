package net.vertexgraphics.myfinances.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
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
import net.vertexgraphics.myfinances.IncomeEditViewModel
import net.vertexgraphics.myfinances.ui.theme.AppTextColor

@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val currentRoute = currentDestination?.route
                    
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = AppTextColor,
                            unselectedTextColor = AppTextColor
                        ),
                        icon = { Icon(Icons.Default.List, contentDescription = null, tint = AppTextColor) },
                        label = { Text("Bills", color = AppTextColor) },
                        selected = currentRoute == "bills",
                        onClick = {
                            if (currentRoute != "bills") {
                                navController.navigate("bills") {
                                    popUpTo("bills") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )

                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = AppTextColor,
                            unselectedTextColor = AppTextColor
                        ),
                        icon = { Icon(Icons.Default.Menu, contentDescription = null, tint = AppTextColor) },
                        label = { Text("Incomes", color = AppTextColor) },
                        selected = currentRoute == "incomes",
                        onClick = {
                            if (currentRoute != "incomes") {
                                navController.navigate("incomes") {
                                    popUpTo("bills") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = AppTextColor,
                            unselectedTextColor = AppTextColor
                        ),
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = AppTextColor) },
                        label = { Text("Accounts", color = AppTextColor) },
                        selected = currentRoute == "accounts",
                        onClick = {
                            if (currentRoute != "accounts") {
                                navController.navigate("accounts") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "bills", modifier = Modifier.padding(padding)) {
            composable("bills") {
                MainScreen(
                    viewModel = mainViewModel,
                    selectedTab = "bills",
                    onAddBill = { navController.navigate("edit/-1") },
                    onEditBill = { id -> navController.navigate("edit/$id") },
                    onAddIncome = { navController.navigate("edit_income/-1") },
                    onEditIncome = { id -> navController.navigate("edit_income/$id") },
                    onViewLog = { navController.navigate("log") },
                    navController = navController
                )
            }
            composable("incomes") {
                MainScreen(
                    viewModel = mainViewModel,
                    selectedTab = "incomes",
                    onAddBill = { navController.navigate("edit/-1") },
                    onEditBill = { id -> navController.navigate("edit/$id") },
                    onAddIncome = { navController.navigate("edit_income/-1") },
                    onEditIncome = { id -> navController.navigate("edit_income/$id") },
                    onViewLog = { navController.navigate("log") },
                    navController = navController
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
            composable(
                "edit_income/{incomeId}",
                arguments = listOf(navArgument("incomeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val incomeId = backStackEntry.arguments?.getInt("incomeId") ?: -1
                val viewModel: IncomeEditViewModel = hiltViewModel()
                IncomeEditScreen(
                    viewModel = viewModel,
                    incomeId = incomeId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("accounts") {
                AccountsScreen(
                    viewModel = mainViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("log") {
                LogScreen(viewModel = mainViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
