package com.budgetbuddy.app.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import androidx.annotation.Keep

@Keep
@IgnoreExtraProperties
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val merchant: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isAutoDetected: Boolean = false,
    val bankAccountId: String? = null,
    val userId: String = "" // Added for Firebase multi-user support
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 0.0,
        category = "",
        description = "",
        merchant = null,
        date = System.currentTimeMillis(),
        isAutoDetected = false,
        bankAccountId = null,
        userId = ""
    )
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Keep
@IgnoreExtraProperties
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val monthlyLimit: Double = 0.0,
    val spent: Double = 0.0,
    val color: String = "",
    val icon: String = "",
    val userId: String = "" // Added for Firebase multi-user support
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        name = "",
        monthlyLimit = 0.0,
        spent = 0.0,
        color = "",
        icon = "",
        userId = ""
    )
}

@Keep
@IgnoreExtraProperties
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val totalMonthlyBudget: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val userId: String = "" // Added for Firebase multi-user support
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        totalMonthlyBudget = 0.0,
        month = 0,
        year = 0,
        userId = ""
    )
}

@Keep
@IgnoreExtraProperties
data class BudgetAlert(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val message: String = "",
    val type: AlertType = AlertType.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = "" // Added for Firebase multi-user support
) {
    // No-argument constructor required for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        categoryId = "",
        message = "",
        type = AlertType.INFO,
        timestamp = System.currentTimeMillis(),
        isRead = false,
        userId = ""
    )
}

enum class AlertType {
    WARNING,
    DANGER,
    INFO
}

@Keep
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        uid = "",
        email = "",
        displayName = null,
        createdAt = System.currentTimeMillis(),
        lastLoginAt = System.currentTimeMillis()
    )
}

@Keep
@IgnoreExtraProperties
data class CategoryWithTransactions(
    val category: Category,
    val transactions: List<Transaction>
)

@Keep
@IgnoreExtraProperties
data class FinancialSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netIncome: Double,
    val savingsRate: Double,
    val avgDailySpend: Double,
    val budgetUsedPercentage: Double
)

// SMS Transaction Detection Model
@Keep
@IgnoreExtraProperties
data class SMSTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String?,
    val timestamp: Long,
    val rawMessage: String,
    val bankName: String?
)