package com.budgetbuddy.app.data.repository

import com.budgetbuddy.app.data.model.Transaction
import com.budgetbuddy.app.data.model.Category
import com.budgetbuddy.app.data.model.Budget
import com.budgetbuddy.app.data.model.BudgetAlert
import com.budgetbuddy.app.data.model.User
import com.budgetbuddy.app.data.model.TransactionType
import com.budgetbuddy.app.data.model.FinancialSummary
import com.budgetbuddy.app.data.model.AlertType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun getUserRef(path: String): DatabaseReference? {
        val userId = currentUserId
        return if (userId != null) {
            database.getReference("users/$userId/$path")
        } else {
            null
        }
    }

    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val ref = getUserRef("transactions")
        if (ref == null) {
            val emptyList = emptyList<Transaction>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionsList = ArrayList<Transaction>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val trans = child.getValue(Transaction::class.java)
                        if (trans != null) {
                            transactionsList.add(trans)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                // Sort manually by date descending
                transactionsList.sortWith(compareByDescending { t -> t.date })
                val resultList = transactionsList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = callbackFlow {
        val ref = getUserRef("transactions")
        if (ref == null) {
            val emptyList = emptyList<Transaction>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val typeName = type.name
        val query = ref.orderByChild("type").equalTo(typeName)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionsList = ArrayList<Transaction>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val trans = child.getValue(Transaction::class.java)
                        if (trans != null) {
                            transactionsList.add(trans)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                transactionsList.sortWith(compareByDescending { t -> t.date })
                val resultList = transactionsList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = callbackFlow {
        val ref = getUserRef("transactions")
        if (ref == null) {
            val emptyList = emptyList<Transaction>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val query = ref.orderByChild("category").equalTo(category)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionsList = ArrayList<Transaction>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val trans = child.getValue(Transaction::class.java)
                        if (trans != null) {
                            transactionsList.add(trans)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                transactionsList.sortWith(compareByDescending { t -> t.date })
                val resultList = transactionsList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val ref = getUserRef("transactions")
        if (ref == null) return

        val userId = currentUserId
        if (userId == null) return

        // Create new transaction with userId
        val transId = transaction.id
        val transType = transaction.type
        val transAmount = transaction.amount
        val transCategory = transaction.category
        val transDescription = transaction.description
        val transMerchant = transaction.merchant
        val transDate = transaction.date
        val transAutoDetected = transaction.isAutoDetected
        val transBankAccount = transaction.bankAccountId

        val transactionWithUser = Transaction(
            id = transId,
            type = transType,
            amount = transAmount,
            category = transCategory,
            description = transDescription,
            merchant = transMerchant,
            date = transDate,
            isAutoDetected = transAutoDetected,
            bankAccountId = transBankAccount,
            userId = userId
        )

        ref.child(transId).setValue(transactionWithUser).await()

        // Update category spent amount if it's an expense
        if (transType == TransactionType.EXPENSE) {
            updateCategorySpending(transCategory)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val ref = getUserRef("transactions")
        if (ref == null) return

        val transId = transaction.id
        ref.child(transId).removeValue().await()

        val transType = transaction.type
        if (transType == TransactionType.EXPENSE) {
            val transCategory = transaction.category
            updateCategorySpending(transCategory)
        }
    }

    suspend fun resetMonthlyTransactions() {
        val transactionsRef = getUserRef("transactions")
        if (transactionsRef != null) {
            transactionsRef.removeValue().await()
        }

        // Reset all category spending
        val categoriesRef = getUserRef("categories")
        if (categoriesRef != null) {
            val snapshot = categoriesRef.get().await()
            val children = snapshot.children
            for (child in children) {
                child.ref.child("spent").setValue(0.0).await()
            }
        }
    }

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val ref = getUserRef("categories")
        if (ref == null) {
            val emptyList = emptyList<Category>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = ArrayList<Category>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val cat = child.getValue(Category::class.java)
                        if (cat != null) {
                            categoriesList.add(cat)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                categoriesList.sortWith(compareBy { c -> c.name })
                val resultList = categoriesList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getCategoryById(id: String): Category? {
        val ref = getUserRef("categories")
        if (ref == null) return null

        val snapshot = ref.child(id).get().await()
        return try {
            snapshot.getValue(Category::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addCategory(category: Category) {
        val ref = getUserRef("categories")
        if (ref == null) return

        val userId = currentUserId
        if (userId == null) return

        // Create new category with userId
        val catId = category.id
        val catName = category.name
        val catLimit = category.monthlyLimit
        val catSpent = category.spent
        val catColor = category.color
        val catIcon = category.icon

        val categoryWithUser = Category(
            id = catId,
            name = catName,
            monthlyLimit = catLimit,
            spent = catSpent,
            color = catColor,
            icon = catIcon,
            userId = userId
        )

        ref.child(catId).setValue(categoryWithUser).await()
    }

    suspend fun updateCategory(category: Category) {
        val ref = getUserRef("categories")
        if (ref == null) return

        val catId = category.id
        ref.child(catId).setValue(category).await()
    }

    suspend fun deleteCategory(category: Category) {
        val ref = getUserRef("categories")
        if (ref != null) {
            val catId = category.id
            ref.child(catId).removeValue().await()
        }
        val catId2 = category.id
        deleteAlertsForCategory(catId2)
    }

    private suspend fun updateCategorySpending(categoryName: String) {
        val transactionsRef = getUserRef("transactions")
        val categoriesRef = getUserRef("categories")

        if (transactionsRef == null || categoriesRef == null) return

        // Get all transactions for this category
        val transactionsSnapshot = transactionsRef
            .orderByChild("category")
            .equalTo(categoryName)
            .get()
            .await()

        val categoryTransactions = ArrayList<Transaction>()
        val transChildren = transactionsSnapshot.children
        for (child in transChildren) {
            try {
                val trans = child.getValue(Transaction::class.java)
                if (trans != null) {
                    val transType = trans.type
                    if (transType == TransactionType.EXPENSE) {
                        categoryTransactions.add(trans)
                    }
                }
            } catch (e: Exception) {
                // Skip invalid data
            }
        }

        var totalSpent = 0.0
        for (trans in categoryTransactions) {
            val amt = trans.amount
            totalSpent = totalSpent + amt
        }

        // Get category and update spent amount
        val categoriesSnapshot = categoriesRef.get().await()
        val catChildren = categoriesSnapshot.children
        for (child in catChildren) {
            try {
                val cat = child.getValue(Category::class.java)
                if (cat != null) {
                    val catName = cat.name
                    if (catName == categoryName) {
                        child.ref.child("spent").setValue(totalSpent).await()

                        // Create updated category for alert check
                        val catId = cat.id
                        val catLimit = cat.monthlyLimit
                        val catColor = cat.color
                        val catIcon = cat.icon
                        val catUserId = cat.userId

                        val updatedCat = Category(
                            id = catId,
                            name = catName,
                            monthlyLimit = catLimit,
                            spent = totalSpent,
                            color = catColor,
                            icon = catIcon,
                            userId = catUserId
                        )
                        checkAndCreateAlert(updatedCat)
                    }
                }
            } catch (e: Exception) {
                // Skip invalid data
            }
        }
    }

    // Budget operations
    suspend fun getCurrentMonthBudget(): Budget? {
        val ref = getUserRef("budgets")
        if (ref == null) return null

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val monthDouble = month.toDouble()

        val snapshot = ref
            .orderByChild("month")
            .equalTo(monthDouble)
            .get()
            .await()

        val children = snapshot.children
        for (child in children) {
            try {
                val budget = child.getValue(Budget::class.java)
                if (budget != null) {
                    val budgetYear = budget.year
                    if (budgetYear == year) {
                        return budget
                    }
                }
            } catch (e: Exception) {
                // Skip invalid data
            }
        }
        return null
    }

    suspend fun setBudget(budget: Budget) {
        val ref = getUserRef("budgets")
        if (ref == null) return

        val userId = currentUserId
        if (userId == null) return

        // Create new budget with userId
        val budgetId = budget.id
        val budgetTotal = budget.totalMonthlyBudget
        val budgetMonth = budget.month
        val budgetYear = budget.year

        val budgetWithUser = Budget(
            id = budgetId,
            totalMonthlyBudget = budgetTotal,
            month = budgetMonth,
            year = budgetYear,
            userId = userId
        )

        ref.child(budgetId).setValue(budgetWithUser).await()
    }

    // Alert operations
    fun getAllAlerts(): Flow<List<BudgetAlert>> = callbackFlow {
        val ref = getUserRef("alerts")
        if (ref == null) {
            val emptyList = emptyList<BudgetAlert>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertsList = ArrayList<BudgetAlert>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val alert = child.getValue(BudgetAlert::class.java)
                        if (alert != null) {
                            alertsList.add(alert)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                alertsList.sortWith(compareByDescending { a -> a.timestamp })
                val resultList = alertsList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getUnreadAlerts(): Flow<List<BudgetAlert>> = callbackFlow {
        val ref = getUserRef("alerts")
        if (ref == null) {
            val emptyList = emptyList<BudgetAlert>()
            trySend(emptyList)
            close()
            return@callbackFlow
        }

        val query = ref.orderByChild("isRead").equalTo(false)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertsList = ArrayList<BudgetAlert>()
                val children = snapshot.children
                for (child in children) {
                    try {
                        val alert = child.getValue(BudgetAlert::class.java)
                        if (alert != null) {
                            alertsList.add(alert)
                        }
                    } catch (e: Exception) {
                        // Skip invalid data
                    }
                }
                alertsList.sortWith(compareByDescending { a -> a.timestamp })
                val resultList = alertsList.toList()
                trySend(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun markAlertAsRead(alertId: String) {
        val ref = getUserRef("alerts")
        if (ref == null) return

        ref.child(alertId).child("isRead").setValue(true).await()
    }

    private suspend fun deleteAlertsForCategory(categoryId: String) {
        val ref = getUserRef("alerts")
        if (ref == null) return

        val snapshot = ref.orderByChild("categoryId").equalTo(categoryId).get().await()
        val children = snapshot.children
        for (child in children) {
            child.ref.removeValue().await()
        }
    }

    private suspend fun checkAndCreateAlert(category: Category) {
        val monthlyLimit = category.monthlyLimit
        if (monthlyLimit <= 0.0) return

        val spent = category.spent
        val percentage = (spent / monthlyLimit) * 100.0

        // Delete old alerts for this category
        val catId = category.id
        deleteAlertsForCategory(catId)

        val alertRef = getUserRef("alerts")
        if (alertRef == null) return

        val userId = currentUserId
        if (userId == null) return

        val catName = category.name

        val alert: BudgetAlert? = when {
            percentage >= 100.0 -> {
                BudgetAlert(
                    categoryId = catId,
                    message = "Budget exceeded in $catName! You've spent ₹$spent of ₹$monthlyLimit",
                    type = AlertType.DANGER,
                    userId = userId
                )
            }
            percentage >= 80.0 -> {
                BudgetAlert(
                    categoryId = catId,
                    message = "You're exceeding your budget in $catName. Consider reviewing your spending.",
                    type = AlertType.WARNING,
                    userId = userId
                )
            }
            else -> null
        }

        if (alert != null) {
            val alertId = alert.id
            alertRef.child(alertId).setValue(alert).await()
        }
    }

    // Financial summary
    fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            getAllTransactions(),
            flow {
                val budget = getCurrentMonthBudget()
                emit(budget)
            }
        ) { transactions, budget ->
            var totalIncome = 0.0
            var totalExpenses = 0.0

            for (trans in transactions) {
                val transType = trans.type
                val transAmount = trans.amount
                when (transType) {
                    TransactionType.INCOME -> totalIncome = totalIncome + transAmount
                    TransactionType.EXPENSE -> totalExpenses = totalExpenses + transAmount
                }
            }

            val netIncome = totalIncome - totalExpenses
            val savingsRate = if (totalIncome > 0.0) {
                (netIncome / totalIncome) * 100.0
            } else {
                0.0
            }
            val avgDailySpend = totalExpenses / 30.0
            val budgetUsedPercentage = if (budget != null) {
                val budgetTotal = budget.totalMonthlyBudget
                if (budgetTotal > 0.0) {
                    (totalExpenses / budgetTotal) * 100.0
                } else {
                    0.0
                }
            } else {
                0.0
            }

            FinancialSummary(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                netIncome = netIncome,
                savingsRate = savingsRate,
                avgDailySpend = avgDailySpend,
                budgetUsedPercentage = budgetUsedPercentage
            )
        }
    }

    // Initialize default categories
    suspend fun initializeDefaultCategories() {
        val ref = getUserRef("categories")
        if (ref == null) return

        val snapshot = ref.get().await()

        val hasChildren = snapshot.hasChildren()
        if (!hasChildren) {
            val userId = currentUserId
            if (userId == null) return

            val defaultCategories = listOf(
                Category(name = "Food & Dining", monthlyLimit = 3000.0, color = "#FF6B6B", icon = "restaurant", userId = userId),
                Category(name = "Shopping", monthlyLimit = 2000.0, color = "#4ECDC4", icon = "shopping_bag", userId = userId),
                Category(name = "Housing", monthlyLimit = 8000.0, color = "#45B7D1", icon = "home", userId = userId),
                Category(name = "Transportation", monthlyLimit = 1500.0, color = "#FFA07A", icon = "directions_car", userId = userId),
                Category(name = "Utilities", monthlyLimit = 2000.0, color = "#98D8C8", icon = "bolt", userId = userId),
                Category(name = "Healthcare", monthlyLimit = 1500.0, color = "#F7DC6F", icon = "favorite", userId = userId),
                Category(name = "Entertainment", monthlyLimit = 1000.0, color = "#BB8FCE", icon = "movie", userId = userId),
                Category(name = "Others", monthlyLimit = 2000.0, color = "#85929E", icon = "credit_card", userId = userId)
            )

            for (cat in defaultCategories) {
                val catId = cat.id
                ref.child(catId).setValue(cat).await()
            }
        }
    }

    // User profile operations
    suspend fun saveUserProfile(user: User) {
        val userId = user.uid
        val userIdEmpty = userId.isEmpty()
        if (!userIdEmpty) {
            val userPath = "users/$userId/profile"
            val ref = database.getReference(userPath)
            ref.setValue(user).await()
        }
    }

    fun getUserProfile(): Flow<User?> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            val nullValue: User? = null
            trySend(nullValue)
            close()
            return@callbackFlow
        }

        val userPath = "users/$userId/profile"
        val ref = database.getReference(userPath)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val user = snapshot.getValue(User::class.java)
                    trySend(user)
                } catch (e: Exception) {
                    val nullValue: User? = null
                    trySend(nullValue)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val exception = error.toException()
                close(exception)
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}