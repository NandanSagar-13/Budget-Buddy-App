package com.budgetbuddy.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.app.data.model.TransactionType
import com.budgetbuddy.app.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryExpenseData(
    val name: String,
    val amount: Double,
    val percentage: Double,
    val color: String
)

data class AnalyticsUiState(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netIncome: Double = 0.0,
    val savingsRate: Double = 0.0,
    val avgDailySpend: Double = 0.0,
    val categoriesOverBudget: Int = 0,
    val totalCategories: Int = 0,
    val expensesByCategory: List<CategoryExpenseData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val currentState1 = _uiState.value
            val newState1 = currentState1.copy(isLoading = true)
            _uiState.update { newState1 }

            try {
                combine(
                    repository.getAllTransactions(),
                    repository.getAllCategories()
                ) { transactions, categories ->
                    var income = 0.0
                    var expenses = 0.0

                    for (transaction in transactions) {
                        val transType = transaction.type
                        val transAmount = transaction.amount
                        when (transType) {
                            TransactionType.INCOME -> income = income + transAmount
                            TransactionType.EXPENSE -> expenses = expenses + transAmount
                        }
                    }

                    val netIncome = income - expenses
                    val savingsRate = if (income > 0.0) {
                        (netIncome / income) * 100.0
                    } else {
                        0.0
                    }
                    val avgDaily = expenses / 30.0

                    var categoriesOver = 0
                    for (category in categories) {
                        val monthlyLimit = category.monthlyLimit
                        if (monthlyLimit > 0.0) {
                            val spent = category.spent
                            val percentage = (spent / monthlyLimit) * 100.0
                            if (percentage >= 100.0) {
                                categoriesOver = categoriesOver + 1
                            }
                        }
                    }

                    // Calculate expenses by category
                    val categoryExpenses = ArrayList<CategoryExpenseData>()
                    for (category in categories) {
                        val spent = category.spent
                        if (spent > 0.0) {
                            val percentage = if (expenses > 0.0) {
                                (spent / expenses) * 100.0
                            } else {
                                0.0
                            }

                            val catName = category.name
                            val catColor = category.color
                            val expenseData = CategoryExpenseData(
                                name = catName,
                                amount = spent,
                                percentage = percentage,
                                color = catColor
                            )
                            categoryExpenses.add(expenseData)
                        }
                    }
                    categoryExpenses.sortWith(compareByDescending { expData -> expData.amount })

                    val catCount = categories.size
                    AnalyticsUiState(
                        totalIncome = income,
                        totalExpenses = expenses,
                        netIncome = netIncome,
                        savingsRate = savingsRate,
                        avgDailySpend = avgDaily,
                        categoriesOverBudget = categoriesOver,
                        totalCategories = catCount,
                        expensesByCategory = categoryExpenses.toList(),
                        isLoading = false
                    )
                }.catch { exception ->
                    val currentState2 = _uiState.value
                    val errorMessage = exception.message
                    val errorMsg = errorMessage ?: "An error occurred"
                    val newState2 = currentState2.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                    _uiState.update { newState2 }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (exception: Exception) {
                val currentState3 = _uiState.value
                val errorMessage = exception.message
                val errorMsg = errorMessage ?: "An error occurred"
                val newState3 = currentState3.copy(
                    isLoading = false,
                    error = errorMsg
                )
                _uiState.update { newState3 }
            }
        }
    }

    fun refresh() {
        loadAnalytics()
    }

    fun clearError() {
        val currentState = _uiState.value
        val newState = currentState.copy(error = null)
        _uiState.update { newState }
    }
}