package com.budgetbuddy.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Metrics Row
            item {
                Text(
                    "Key Metrics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Net Income",
                        value = "₹${String.format("%.0f", uiState.netIncome)}",
                        subtitle = "Income - Expenses",
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.weight(1f)
                    )
                    
                    MetricCard(
                        title = "Savings Rate",
                        value = "${uiState.savingsRate.toInt()}%",
                        subtitle = getSavingsRateText(uiState.savingsRate),
                        color = when {
                            uiState.savingsRate >= 70 -> Color(0xFF10B981)
                            uiState.savingsRate >= 50 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Avg. Daily",
                        value = "₹${String.format("%.0f", uiState.avgDailySpend)}",
                        subtitle = "Last 30 days",
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                    
                    MetricCard(
                        title = "Budget Status",
                        value = "${uiState.categoriesOverBudget} / ${uiState.totalCategories}",
                        subtitle = "Over budget",
                        color = if (uiState.categoriesOverBudget > 0) 
                            Color(0xFFEF4444) 
                        else 
                            Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Expense Breakdown
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Expense Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (uiState.expensesByCategory.isEmpty()) {
                item {
                    EmptyAnalyticsView()
                }
            } else {
                items(
                    items = uiState.expensesByCategory,
                    key = { it.name }
                ) { categoryData ->
                    ExpenseCategoryItem(categoryData)
                }
            }
            
            // Income vs Expenses
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Income vs Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                IncomeVsExpensesCard(
                    income = uiState.totalIncome,
                    expenses = uiState.totalExpenses
                )
            }
            
            // Monthly Trend (Placeholder)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Monthly Comparison",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                MonthlyComparisonCard(
                    currentMonthExpenses = uiState.totalExpenses,
                    currentMonthIncome = uiState.totalIncome
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ExpenseCategoryItem(categoryData: CategoryExpenseData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(android.graphics.Color.parseColor(categoryData.color)))
                    )
                    
                    Text(
                        categoryData.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${String.format("%.2f", categoryData.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${categoryData.percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (categoryData.percentage / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(android.graphics.Color.parseColor(categoryData.color)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun IncomeVsExpensesCard(income: Double, expenses: Double) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "₹${String.format("%.0f", income)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total Income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    "vs",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "₹${String.format("%.0f", expenses)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Visual representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (income > 0 || expenses > 0) {
                    val total = income + expenses
                    Box(
                        modifier = Modifier
                            .weight((income / total).toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF10B981))
                    )
                    Box(
                        modifier = Modifier
                            .weight((expenses / total).toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFFEF4444))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val netAmount = income - expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (netAmount >= 0) "Surplus: " else "Deficit: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${String.format("%.2f", Math.abs(netAmount))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (netAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun MonthlyComparisonCard(
    currentMonthExpenses: Double,
    currentMonthIncome: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "This Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Income: ₹${String.format("%.0f", currentMonthIncome)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        "Expenses: ₹${String.format("%.0f", currentMonthExpenses)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5),
                                    Color(0xFF7C3AED)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Track your spending patterns over time to make better financial decisions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyAnalyticsView() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No expense data available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add some transactions to see analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getSavingsRateText(rate: Double): String {
    return when {
        rate >= 70 -> "Excellent"
        rate >= 50 -> "Good"
        rate >= 30 -> "Fair"
        else -> "Needs Improvement"
    }
}
