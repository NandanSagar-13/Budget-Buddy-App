package com.budgetbuddy.app.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.budgetbuddy.app.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlanningScreen(
    navController: NavController,
    viewModel: BudgetPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Planning", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditBudgetDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit Budget")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Category")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Budget Card
            item {
                OverallBudgetCard(
                    monthlyBudget = uiState.monthlyBudget,
                    totalSpent = uiState.totalSpent,
                    budgetUsedPercentage = uiState.budgetUsedPercentage,
                    onResetClick = { showResetDialog = true }
                )
            }
            
            // Category Budgets
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Budget Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${uiState.categoriesOverBudget} over budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.categoriesOverBudget > 0) 
                            Color(0xFFEF4444) 
                        else 
                            Color(0xFF10B981)
                    )
                }
            }
            
            if (uiState.categories.isEmpty()) {
                item {
                    EmptyCategoriesView()
                }
            } else {
                items(
                    items = uiState.categories,
                    key = { it.id }
                ) { category ->
                    CategoryBudgetCard(
                        category = category,
                        onDelete = { viewModel.deleteCategory(it) },
                        onEdit = { /* TODO: Implement edit */ }
                    )
                }
            }
        }
    }
    
    // Edit Budget Dialog
    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = uiState.monthlyBudget,
            onDismiss = { showEditBudgetDialog = false },
            onConfirm = { newBudget ->
                viewModel.updateMonthlyBudget(newBudget)
                showEditBudgetDialog = false
            }
        )
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { category ->
                viewModel.addCategory(category)
                showAddCategoryDialog = false
            }
        )
    }
    
    // Reset Month Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Month") },
            text = { Text("This will delete all transactions and reset spending for all categories. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetMonth()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OverallBudgetCard(
    monthlyBudget: Double,
    totalSpent: Double,
    budgetUsedPercentage: Double,
    onResetClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF9333EA)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Overall Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    TextButton(
                        onClick = onResetClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "₹${String.format("%.2f", monthlyBudget)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Total Spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "₹${String.format("%.2f", totalSpent)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "₹${String.format("%.2f", monthlyBudget - totalSpent)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Used",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "${budgetUsedPercentage.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = (budgetUsedPercentage / 100).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(
    category: Category,
    onDelete: (Category) -> Unit,
    onEdit: (Category) -> Unit
) {
    val percentage = (category.spent / category.monthlyLimit * 100)
    val isOverBudget = percentage >= 100
    val isWarning = percentage >= 80 && percentage < 100
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverBudget -> Color(0xFFFEE2E2)
                isWarning -> Color(0xFFFEF3C7)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isOverBudget -> CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFEF4444)))
            )
            isWarning -> CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFF59E0B)))
            )
            else -> null
        }
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color(android.graphics.Color.parseColor(category.color))
                                    .copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(android.graphics.Color.parseColor(category.color)),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Monthly Limit: ₹${String.format("%.0f", category.monthlyLimit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { onDelete(category) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${String.format("%.2f", category.spent)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOverBudget -> Color(0xFFEF4444)
                            isWarning -> Color(0xFFF59E0B)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${String.format("%.2f", (category.monthlyLimit - category.spent).coerceAtLeast(0.0))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (percentage / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    isOverBudget -> Color(0xFFEF4444)
                    isWarning -> Color(0xFFF59E0B)
                    else -> Color(android.graphics.Color.parseColor(category.color))
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${percentage.toInt()}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isOverBudget) {
                    Text(
                        "Over Budget!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                } else if (isWarning) {
                    Text(
                        "Near Limit",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCategoriesView() {
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
                Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No categories yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add your first category to start budgeting",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetValue by remember { mutableStateOf(currentBudget.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Monthly Budget") },
        text = {
            Column {
                Text(
                    "Set your total monthly budget allocation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetValue,
                    onValueChange = { budgetValue = it },
                    label = { Text("Monthly Budget (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    budgetValue.toDoubleOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4ECDC4") }
    
    val colors = listOf(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
        "#98D8C8", "#F7DC6F", "#BB8FCE", "#85929E"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Groceries") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Monthly Limit (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Column {
                    Text(
                        "Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .then(
                                        if (selectedColor == color)
                                            Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(8.dp)
                                            )
                                        else Modifier
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent)
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && limit.isNotEmpty()) {
                        onConfirm(
                            Category(
                                name = name,
                                monthlyLimit = limit.toDoubleOrNull() ?: 0.0,
                                color = selectedColor,
                                icon = "category"
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
