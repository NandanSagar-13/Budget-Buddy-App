package com.budgetbuddy.app.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.app.data.model.Budget
import com.budgetbuddy.app.data.model.Category
import com.budgetbuddy.app.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BudgetPlanningUiState(
    val monthlyBudget: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val totalSpent: Double = 0.0,
    val budgetUsedPercentage: Double = 0.0,
    val categoriesOverBudget: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetPlanningViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetPlanningUiState())
    val uiState: StateFlow<BudgetPlanningUiState> = _uiState.asStateFlow()

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    repository.getAllCategories(),
                    flow { emit(repository.getCurrentMonthBudget()) }
                ) { categories, budget ->
                    val totalSpent = categories.sumOf { it.spent }
                    val monthlyBudget = budget?.totalMonthlyBudget ?: 21000.0
                    val budgetUsed = if (monthlyBudget > 0)
                        (totalSpent / monthlyBudget) * 100
                    else 0.0
                    val overBudget = categories.count {
                        (it.spent / it.monthlyLimit) * 100 >= 100
                    }

                    BudgetPlanningUiState(
                        monthlyBudget = monthlyBudget,
                        categories = categories,
                        totalSpent = totalSpent,
                        budgetUsedPercentage = budgetUsed,
                        categoriesOverBudget = overBudget,
                        isLoading = false
                    )
                }.catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred"
                        )
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val budget = Budget(
                    totalMonthlyBudget = amount,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR)
                )
                repository.setBudget(budget)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update budget")
                }
            }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.addCategory(category)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to add category")
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete category")
                }
            }
        }
    }

    fun resetMonth() {
        viewModelScope.launch {
            try {
                repository.resetMonthlyTransactions()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to reset month")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}