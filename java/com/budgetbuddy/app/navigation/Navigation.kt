package com.budgetbuddy.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.budgetbuddy.app.ui.screens.dashboard.DashboardScreen
import com.budgetbuddy.app.ui.screens.transactions.TransactionsScreen
import com.budgetbuddy.app.ui.screens.budget.BudgetPlanningScreen
import com.budgetbuddy.app.ui.screens.analytics.AnalyticsScreen
import com.budgetbuddy.app.ui.screens.auth.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Budget : Screen("budget")
    object Analytics : Screen("analytics")
}

@Composable
fun BudgetBuddyNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(navController = navController)
        }
        
        composable(Screen.Budget.route) {
            BudgetPlanningScreen(navController = navController)
        }
        
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }
    }
}
