package com.budgetbuddy.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.app.data.model.*
import com.budgetbuddy.app.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.take

data class DashboardUiState(
    val financialSummary: FinancialSummary = FinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val topCategories: List<Category> = emptyList(),
    val alerts: List<BudgetAlert> = emptyList(),
    val unreadAlerts: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Combine multiple flows
                combine(
                    repository.getFinancialSummary(),
                    repository.getAllTransactions(),
                    repository.getAllCategories(),
                    repository.getUnreadAlerts()
                ) { summary, transactions, categories, alerts ->
                    DashboardUiState(
                        financialSummary = summary,
                        recentTransactions = transactions.take(5),
                        topCategories = categories.sortedByDescending { it.spent }.take(3),
                        alerts = alerts,
                        unreadAlerts = alerts.size,
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

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}