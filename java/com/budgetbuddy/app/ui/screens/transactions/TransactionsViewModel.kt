package com.budgetbuddy.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.app.data.model.Transaction
import com.budgetbuddy.app.data.model.TransactionType
import com.budgetbuddy.app.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val currentState1 = _uiState.value
            val newState1 = currentState1.copy(isLoading = true)
            _uiState.update { newState1 }

            combine(
                repository.getAllTransactions(),
                _searchQuery,
                _selectedCategory
            ) { transactions, query, category ->
                val filtered = ArrayList<Transaction>()
                for (transaction in transactions) {
                    val description = transaction.description
                    val merchant = transaction.merchant
                    val descriptionMatches = description.contains(query, ignoreCase = true)
                    val merchantMatches = if (merchant != null) {
                        merchant.contains(query, ignoreCase = true)
                    } else {
                        false
                    }
                    val queryEmpty = query.isEmpty()
                    val matchesSearch = queryEmpty || descriptionMatches || merchantMatches

                    val transCategory = transaction.category
                    val matchesCategory = if (category == null) {
                        true
                    } else {
                        transCategory == category
                    }

                    if (matchesSearch && matchesCategory) {
                        filtered.add(transaction)
                    }
                }

                var income = 0.0
                var expenses = 0.0
                for (transaction in filtered) {
                    val transType = transaction.type
                    val transAmount = transaction.amount
                    when (transType) {
                        TransactionType.INCOME -> income = income + transAmount
                        TransactionType.EXPENSE -> expenses = expenses + transAmount
                    }
                }

                TransactionsUiState(
                    allTransactions = transactions,
                    filteredTransactions = filtered.toList(),
                    searchQuery = query,
                    selectedCategory = category,
                    totalIncome = income,
                    totalExpenses = expenses,
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
                val currentCategories = _uiState.value.categories
                val newState3 = state.copy(categories = currentCategories)
                _uiState.update { newState3 }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories()
                .catch { exception ->
                    // Handle error silently or log it
                }
                .collect { categories ->
                    val categoryNames = ArrayList<String>()
                    for (cat in categories) {
                        val catName = cat.name
                        categoryNames.add(catName)
                    }
                    val currentState = _uiState.value
                    val newState = currentState.copy(categories = categoryNames.toList())
                    _uiState.update { newState }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.addTransaction(transaction)
            } catch (exception: Exception) {
                val currentState = _uiState.value
                val errorMessage = exception.message
                val errorMsg = errorMessage ?: "Failed to add transaction"
                val newState = currentState.copy(error = errorMsg)
                _uiState.update { newState }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (exception: Exception) {
                val currentState = _uiState.value
                val errorMessage = exception.message
                val errorMsg = errorMessage ?: "Failed to delete transaction"
                val newState = currentState.copy(error = errorMsg)
                _uiState.update { newState }
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        val newState = currentState.copy(error = null)
        _uiState.update { newState }
    }
}