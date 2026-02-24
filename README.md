# Budget-Buddy-App
Production-ready Android finance app with SMS auto-detection, real-time budget tracking, and Firebase cloud sync built using Kotlin &amp; Jetpack Compose.
# Budget Buddy - Complete Technical Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [App Flow](#app-flow)
5. [Core Features](#core-features)
6. [File Structure](#file-structure)
7. [Data Models](#data-models)
8. [Firebase Integration](#firebase-integration)
9. [Authentication Flow](#authentication-flow)
10. [Database Operations](#database-operations)
11. [ViewModels & State Management](#viewmodels--state-management)
12. [UI Components](#ui-components)
13. [SMS Transaction Detection](#sms-transaction-detection)
14. [Setup & Installation](#setup--installation)
15. [How Everything Works Together](#how-everything-works-together)

---

## Overview

### What is Budget Buddy?
Budget Buddy is a **personal finance management Android application** built with **Kotlin and Jetpack Compose**. It helps users:
- Track income and expenses
- Set monthly budgets for different categories
- Get real-time alerts when approaching budget limits
- Automatically detect transactions from bank SMS messages
- Visualize spending patterns with analytics
- Manage multiple expense categories

### Key Value Propositions
1. **Automatic Transaction Detection**: Reads bank SMS to auto-log transactions
2. **Smart Budget Alerts**: Warns when you're about to exceed category budgets
3. **Multi-user Support**: Each user has isolated, secure data via Firebase
4. **Offline-First**: Works without internet, syncs when connected
5. **Real-time Sync**: Data syncs across all your devices instantly

---

## Architecture

### Design Pattern: MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                       │
│                    (Jetpack Compose Screens)                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ├─> LoginScreen
                       ├─> DashboardScreen
                       ├─> TransactionsScreen
                       ├─> BudgetPlanningScreen
                       └─> AnalyticsScreen
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                        VIEWMODELS                            │
│               (State Management & Business Logic)            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ├─> AuthViewModel
                       ├─> DashboardViewModel
                       ├─> TransactionsViewModel
                       ├─> BudgetPlanningViewModel
                       └─> AnalyticsViewModel
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                       REPOSITORY                             │
│                  (Data Layer Abstraction)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ├─> FirebaseRepository
                       └─> AuthenticationManager
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    FIREBASE SERVICES                         │
│                                                              │
├──────────────────────┬──────────────────────────────────────┤
│  Firebase Auth       │  Firebase Realtime Database          │
│  (User Management)   │  (Data Storage & Sync)               │
└──────────────────────┴──────────────────────────────────────┘
```

### Why This Architecture?

1. **Separation of Concerns**: UI, business logic, and data are separate
2. **Testability**: Each layer can be tested independently
3. **Maintainability**: Changes in one layer don't affect others
4. **Scalability**: Easy to add new features
5. **Reusability**: ViewModels can be shared across screens

---

## Technology Stack

### Core Technologies

#### 1. **Kotlin** (Programming Language)
- Modern, concise, null-safe language
- 100% interoperable with Java
- Coroutines for asynchronous programming
- Extension functions for cleaner code

#### 2. **Jetpack Compose** (UI Framework)
- Declarative UI (describe what you want, not how to build it)
- Less code than XML layouts
- Real-time preview in Android Studio
- Better performance with smart recomposition

#### 3. **Firebase**
- **Firebase Authentication**: User login/signup
- **Firebase Realtime Database**: NoSQL cloud database
- **Firebase Analytics**: User behavior tracking

#### 4. **Hilt** (Dependency Injection)
- Reduces boilerplate code
- Manages object lifecycles
- Makes testing easier
- Automatic dependency graph generation

### Key Libraries

```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
androidx.activity:activity-compose:1.8.1

// Compose
androidx.compose:compose-bom:2024.09.00
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Navigation
androidx.navigation:navigation-compose:2.7.5

// Firebase
com.google.firebase:firebase-bom:32.7.0
com.google.firebase:firebase-auth-ktx
com.google.firebase:firebase-database-ktx

// Dependency Injection
com.google.dagger:hilt-android:2.51.1
androidx.hilt:hilt-navigation-compose:1.1.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3

// Charts
com.github.PhilJay:MPAndroidChart:v3.1.0

// Permissions
com.google.accompanist:accompanist-permissions:0.32.0
```

---

## App Flow

### 1. App Launch Sequence

```
App Starts
    ↓
MainActivity.onCreate()
    ↓
Initialize Hilt (Dependency Injection)
    ↓
BudgetBuddyTheme Applied
    ↓
Navigation Component Initialized
    ↓
Check Authentication Status
    ↓
    ├─> Not Authenticated → LoginScreen
    └─> Authenticated → DashboardScreen
```

### 2. User Journey Flow

```
┌─────────────┐
│ Login/Signup│
└──────┬──────┘
       │
       ├─ New User → Sign Up → Email Verification → Initialize Default Categories
       │
       └─ Existing User → Sign In → Verify Credentials → Load User Data
       │
       ↓
┌──────────────┐
│  Dashboard   │ ← Default landing after login
└──────┬───────┘
       │
       ├─> View Financial Summary (Income, Expenses, Savings)
       ├─> See Recent Transactions (Last 5)
       ├─> Check Budget Alerts
       └─> Quick Access to Other Screens
       │
       ↓
┌────────────────────────────────────────┐
│    Bottom Navigation Bar               │
├────────────────────────────────────────┤
│ Dashboard │ Transactions │ Budget │ Analytics │
└────────────────────────────────────────┘
```

### 3. Transaction Flow

```
User Receives Bank SMS
    ↓
SMSTransactionReceiver Triggered
    ↓
Parse SMS for:
  - Amount (₹500)
  - Type (Debit/Credit)
  - Merchant (Swiggy)
  - Bank Name (HDFC Bank)
    ↓
Create Notification
    ↓
User Clicks Notification
    ↓
Add Transaction Screen Opens
    ↓
Suggest Category (Food & Dining)
    ↓
User Confirms/Edits
    ↓
Save to Firebase
    ↓
Update Category Spending
    ↓
Check Budget Limits
    ↓
    ├─> 80% of Budget → Warning Alert
    └─> 100% of Budget → Danger Alert
```

---

## Core Features

### Feature 1: User Authentication

**What it does:**
- Allows users to create accounts with email/password
- Secure login with Firebase Authentication
- Password reset via email
- User profile management

**How it works:**
```kotlin
// Sign Up Flow
AuthViewModel.signUp(email, password, displayName)
    ↓
AuthenticationManager.signUpWithEmail()
    ↓
Firebase Authentication creates user
    ↓
Save user profile to Firebase Database
    ↓
Initialize default categories for new user
    ↓
Navigate to Dashboard
```

**Files Involved:**
- `AuthenticationManager.kt` - Firebase Auth logic
- `AuthViewModel.kt` - Auth state management
- `LoginScreen.kt` - UI for login/signup

---

### Feature 2: Transaction Management

**What it does:**
- Add income/expense transactions manually
- Auto-detect transactions from SMS
- Categorize transactions (Food, Shopping, etc.)
- Search and filter transactions
- Delete transactions

**Data Model:**
```kotlin
data class Transaction(
    val id: String,                    // Unique ID
    val type: TransactionType,         // INCOME or EXPENSE
    val amount: Double,                // ₹500.00
    val category: String,              // "Food & Dining"
    val description: String,           // "Lunch at restaurant"
    val merchant: String?,             // "Swiggy" (optional)
    val date: Long,                    // Timestamp
    val isAutoDetected: Boolean,       // From SMS?
    val bankAccountId: String?,        // Which bank account
    val userId: String                 // Owner of transaction
)
```

**Transaction Lifecycle:**
```
1. Create Transaction
   ↓
2. Validate Data
   ↓
3. Save to Firebase Database
   ↓
4. If EXPENSE:
   - Update category.spent
   - Check budget limit
   - Create alert if needed
   ↓
5. Update UI (via Flow)
   ↓
6. Sync to all user's devices
```

**Files Involved:**
- `TransactionsViewModel.kt` - Business logic
- `TransactionsScreen.kt` - UI
- `Transaction.kt` - Data model
- `FirebaseRepository.kt` - Database operations

---

### Feature 3: Budget Planning

**What it does:**
- Set monthly budget for each category
- Track spending vs. budget
- Visual progress indicators
- Reset budgets monthly
- Add custom categories

**Data Model:**
```kotlin
data class Category(
    val id: String,
    val name: String,              // "Food & Dining"
    val monthlyLimit: Double,      // ₹3000
    val spent: Double,             // ₹2450 (current spending)
    val color: String,             // "#FF6B6B"
    val icon: String,              // "restaurant"
    val userId: String
)
```

**Budget Tracking Logic:**
```kotlin
// Every time an expense is added:
1. Find the category
2. Calculate total spent this month
3. Update category.spent
4. Calculate percentage: (spent / limit) * 100
5. If percentage >= 100% → DANGER alert
6. If percentage >= 80% → WARNING alert
7. Create notification if needed
```

**Default Categories:**
```kotlin
Food & Dining     - ₹3,000/month
Shopping          - ₹2,000/month
Housing           - ₹8,000/month
Transportation    - ₹1,500/month
Utilities         - ₹2,000/month
Healthcare        - ₹1,500/month
Entertainment     - ₹1,000/month
Others            - ₹2,000/month
```

**Files Involved:**
- `BudgetPlanningViewModel.kt` - Budget logic
- `BudgetPlanningScreen.kt` - UI
- `Category.kt` - Data model

---

### Feature 4: Analytics & Insights

**What it does:**
- Show financial summary (income, expenses, savings)
- Pie chart of expenses by category
- Savings rate calculation
- Categories over budget count
- Average daily spending

**Calculations:**
```kotlin
Total Income = Sum of all INCOME transactions
Total Expenses = Sum of all EXPENSE transactions
Net Income = Total Income - Total Expenses
Savings Rate = (Net Income / Total Income) × 100
Average Daily Spend = Total Expenses / 30
Budget Used = (Total Expenses / Monthly Budget) × 100
```

**Expense Breakdown:**
```kotlin
For each category:
  Amount spent in category
  Percentage of total expenses
  Color code for visualization
  
Sort by amount (highest first)
Display as pie chart or list
```

**Files Involved:**
- `AnalyticsViewModel.kt` - Calculations
- `AnalyticsScreen.kt` - Charts and visualizations
- `CategoryExpenseData.kt` - Display data

---

### Feature 5: SMS Transaction Detection

**What it does:**
- Listens for incoming SMS
- Detects bank transaction messages
- Parses amount, type, merchant
- Suggests category automatically
- Creates notification for user approval

**SMS Patterns Detected:**
```
Examples:
1. "₹500 debited from A/c XX1234 at Swiggy on 01-Feb"
2. "Rs. 1000 credited to your account from SALARY"
3. "Your A/c XX5678 debited with Rs.250 for UPI/Zomato"
```

**Parsing Logic:**
```kotlin
1. Extract Amount:
   - Match patterns: "Rs. 500", "₹500", "INR 500"
   - Remove commas, get number

2. Determine Type:
   - Keywords for EXPENSE: debited, withdrawn, spent, paid
   - Keywords for INCOME: credited, received, deposited

3. Extract Merchant:
   - Look for patterns: "at [MERCHANT]", "to [MERCHANT]"
   - UPI patterns: "UPI/[MERCHANT]"

4. Extract Bank:
   - Sender ID patterns: HDFCBK, ICICIB, SBIINB
   - Message content: "HDFC Bank", "ICICI Bank"

5. Suggest Category:
   - Swiggy/Zomato → Food & Dining
   - Amazon/Flipkart → Shopping
   - Uber/Ola → Transportation
   - etc.
```

**Files Involved:**
- `SMSTransactionService.kt` - SMS receiver & parser
- `TransactionParser.kt` - Parsing logic

---

## File Structure

```
BudgetBuddy2/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/budgetbuddy/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── Models.kt              # All data classes
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── FirebaseRepository.kt  # Database operations
│   │   │   │   │   └── auth/
│   │   │   │   │       └── AuthenticationManager.kt
│   │   │   │   ├── di/
│   │   │   │   │   └── HiltModules.kt             # Dependency injection
│   │   │   │   ├── service/
│   │   │   │   │   └── SMSTransactionService.kt   # SMS detection
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── auth/
│   │   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   │   └── AuthViewModel.kt
│   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   │   └── DashboardViewModel.kt
│   │   │   │   │   │   ├── transactions/
│   │   │   │   │   │   │   ├── TransactionsScreen.kt
│   │   │   │   │   │   │   └── TransactionsViewModel.kt
│   │   │   │   │   │   ├── budget/
│   │   │   │   │   │   │   ├── BudgetPlanningScreen.kt
│   │   │   │   │   │   │   └── BudgetPlanningViewModel.kt
│   │   │   │   │   │   └── analytics/
│   │   │   │   │   │       ├── AnalyticsScreen.kt
│   │   │   │   │   │       └── AnalyticsViewModel.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       └── Theme.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── Navigation.kt              # Screen navigation
│   │   │   │   ├── BudgetBuddyApplication.kt      # App entry point
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                               # Resources
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── colors.xml
│   │   │       └── drawable/
│   │   └── AndroidManifest.xml                    # App permissions
│   ├── build.gradle.kts                           # App dependencies
│   └── google-services.json                       # Firebase config
├── build.gradle.kts                               # Project config
└── settings.gradle.kts
```

---

## Data Models

### Core Entities

#### 1. Transaction
```kotlin
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
    val userId: String = ""
)

enum class TransactionType {
    INCOME,    // Money coming in
    EXPENSE    // Money going out
}
```

**Why each field exists:**
- `id`: Unique identifier for Firebase
- `type`: Distinguish income from expenses
- `amount`: The money value
- `category`: For budget tracking
- `description`: User note
- `merchant`: Where money went
- `date`: When it happened
- `isAutoDetected`: Track SMS vs manual
- `bankAccountId`: Support multiple banks
- `userId`: Multi-user data isolation

#### 2. Category
```kotlin
@IgnoreExtraProperties
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val monthlyLimit: Double = 0.0,
    val spent: Double = 0.0,
    val color: String = "",
    val icon: String = "",
    val userId: String = ""
)
```

**Budget Tracking:**
- `monthlyLimit`: How much you can spend
- `spent`: How much you've spent
- Percentage = (spent / monthlyLimit) × 100

#### 3. Budget
```kotlin
@IgnoreExtraProperties
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val totalMonthlyBudget: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val userId: String = ""
)
```

**Overall Budget:**
- Tracks total budget across all categories
- Month/year for historical tracking
- Used to calculate overall budget usage

#### 4. BudgetAlert
```kotlin
@IgnoreExtraProperties
data class BudgetAlert(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val message: String = "",
    val type: AlertType = AlertType.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = ""
)

enum class AlertType {
    WARNING,  // 80% of budget
    DANGER,   // 100% of budget
    INFO      // General information
}
```

#### 5. User
```kotlin
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
```

### Computed Models

#### FinancialSummary
```kotlin
data class FinancialSummary(
    val totalIncome: Double,          // Sum of all income
    val totalExpenses: Double,        // Sum of all expenses
    val netIncome: Double,            // Income - Expenses
    val savingsRate: Double,          // (Net / Income) × 100
    val avgDailySpend: Double,        // Expenses / 30
    val budgetUsedPercentage: Double  // (Expenses / Budget) × 100
)
```

#### CategoryExpenseData
```kotlin
data class CategoryExpenseData(
    val name: String,        // "Food & Dining"
    val amount: Double,      // ₹2,450
    val percentage: Double,  // 35% of total expenses
    val color: String        // "#FF6B6B" for charts
)
```

---

## Firebase Integration

### Database Structure

```
budget-buddy/
└── users/
    └── {userId}/                    # e.g., "abc123def456"
        ├── profile/
        │   ├── uid: "abc123def456"
        │   ├── email: "user@example.com"
        │   ├── displayName: "John Doe"
        │   ├── createdAt: 1675372800000
        │   └── lastLoginAt: 1706908800000
        │
        ├── transactions/
        │   ├── {transactionId1}/
        │   │   ├── id: "trans_001"
        │   │   ├── type: "EXPENSE"
        │   │   ├── amount: 500.0
        │   │   ├── category: "Food & Dining"
        │   │   ├── description: "Lunch"
        │   │   ├── merchant: "Swiggy"
        │   │   ├── date: 1706908800000
        │   │   ├── isAutoDetected: true
        │   │   └── userId: "abc123def456"
        │   └── {transactionId2}/
        │       └── ...
        │
        ├── categories/
        │   ├── {categoryId1}/
        │   │   ├── id: "cat_001"
        │   │   ├── name: "Food & Dining"
        │   │   ├── monthlyLimit: 3000.0
        │   │   ├── spent: 2450.0
        │   │   ├── color: "#FF6B6B"
        │   │   ├── icon: "restaurant"
        │   │   └── userId: "abc123def456"
        │   └── {categoryId2}/
        │       └── ...
        │
        ├── budgets/
        │   └── {budgetId}/
        │       ├── id: "budget_001"
        │       ├── totalMonthlyBudget: 21000.0
        │       ├── month: 1              # February (0-indexed)
        │       ├── year: 2026
        │       └── userId: "abc123def456"
        │
        └── alerts/
            └── {alertId}/
                ├── id: "alert_001"
                ├── categoryId: "cat_001"
                ├── message: "Budget exceeded in Food & Dining!"
                ├── type: "DANGER"
                ├── timestamp: 1706908800000
                ├── isRead: false
                └── userId: "abc123def456"
```

### Security Rules

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "transactions": {
          ".indexOn": ["type", "category", "date"]
        },
        "categories": {
          ".indexOn": ["name"]
        },
        "budgets": {
          ".indexOn": ["month", "year"]
        },
        "alerts": {
          ".indexOn": ["isRead", "categoryId", "timestamp"]
        }
      }
    }
  }
}
```

**What this means:**
- Users can only read/write their own data
- No user can access another user's data
- Indexes speed up queries
- `.indexOn` makes filtering faster

### Firebase Operations

#### Reading Data (Flow)
```kotlin
fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
    val ref = database.getReference("users/$userId/transactions")
    
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val transactions = ArrayList<Transaction>()
            for (child in snapshot.children) {
                child.getValue(Transaction::class.java)?.let {
                    transactions.add(it)
                }
            }
            trySend(transactions)  // Emit to Flow
        }
        
        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    
    ref.addValueEventListener(listener)
    awaitClose { ref.removeEventListener(listener) }
}
```

**How it works:**
1. Create a Firebase reference to the data path
2. Attach a listener for data changes
3. When data changes, Firebase calls `onDataChange`
4. Parse the data and emit it via Flow
5. UI automatically updates when Flow emits new data
6. Remove listener when Flow is closed

#### Writing Data
```kotlin
suspend fun addTransaction(transaction: Transaction) {
    val ref = database.getReference("users/$userId/transactions")
    ref.child(transaction.id).setValue(transaction).await()
    
    // Update category spending
    if (transaction.type == TransactionType.EXPENSE) {
        updateCategorySpending(transaction.category)
    }
}
```

**How it works:**
1. Get reference to transactions path
2. Create new child with transaction ID
3. Set the transaction data
4. `.await()` waits for completion (coroutine)
5. Update related data (category spending)

#### Querying Data
```kotlin
suspend fun getCurrentMonthBudget(): Budget? {
    val ref = database.getReference("users/$userId/budgets")
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    
    val snapshot = ref
        .orderByChild("month")
        .equalTo(month.toDouble())
        .get()
        .await()
    
    for (child in snapshot.children) {
        val budget = child.getValue(Budget::class.java)
        if (budget != null && budget.year == year) {
            return budget
        }
    }
    return null
}
```

**Query Explained:**
1. `orderByChild("month")` - Sort by month field
2. `equalTo(month.toDouble())` - Filter to current month
3. `.get().await()` - Fetch data once
4. Loop through results
5. Check year matches too
6. Return first match

---

## Authentication Flow

### Sign Up Process

```
User Fills Form
    ↓
Validates Email & Password
    ↓
AuthViewModel.signUp(email, password, displayName)
    ↓
AuthenticationManager.signUpWithEmail()
    ↓
    ┌─────────────────────────────────────┐
    │ Firebase Authentication             │
    ├─────────────────────────────────────┤
    │ 1. Check if email already exists    │
    │ 2. Hash password                    │
    │ 3. Create user account              │
    │ 4. Generate auth token              │
    │ 5. Set display name                 │
    └─────────────────────────────────────┘
    ↓
Success → AuthResult.Success(user)
    ↓
Save User Profile to Database
    ↓
Initialize Default Categories
    ↓
Update UI State: isAuthenticated = true
    ↓
Navigate to Dashboard
```

### Sign In Process

```
User Enters Credentials
    ↓
AuthViewModel.signIn(email, password)
    ↓
AuthenticationManager.signInWithEmail()
    ↓
    ┌─────────────────────────────────────┐
    │ Firebase Authentication             │
    ├─────────────────────────────────────┤
    │ 1. Verify email exists              │
    │ 2. Check password hash              │
    │ 3. Generate session token           │
    └─────────────────────────────────────┘
    ↓
Success → AuthResult.Success(user)
    ↓
Update User Profile (lastLoginAt)
    ↓
Update UI State: isAuthenticated = true
    ↓
Navigate to Dashboard
```

### Password Reset

```
User Clicks "Forgot Password"
    ↓
Dialog Opens - Enter Email
    ↓
AuthViewModel.resetPassword(email)
    ↓
Firebase sends password reset email
    ↓
User clicks link in email
    ↓
Opens password reset page
    ↓
User enters new password
    ↓
Password updated in Firebase
    ↓
User can now sign in with new password
```

### Authentication State Management

```kotlin
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

// In AuthViewModel
val authState: Flow<AuthState> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            trySend(AuthState.Authenticated(user))
        } else {
            trySend(AuthState.Unauthenticated)
        }
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}
```

**How it works:**
1. Firebase Auth monitors authentication state
2. When user signs in/out, listener is called
3. Emit appropriate state to Flow
4. UI observes Flow and updates accordingly

---

## Database Operations

### Transaction Operations

#### Add Transaction
```kotlin
suspend fun addTransaction(transaction: Transaction) {
    // 1. Get database reference
    val ref = getUserRef("transactions")
    
    // 2. Add userId to transaction
    val userId = currentUserId ?: return
    val transactionWithUser = Transaction(
        id = transaction.id,
        type = transaction.type,
        amount = transaction.amount,
        category = transaction.category,
        description = transaction.description,
        merchant = transaction.merchant,
        date = transaction.date,
        isAutoDetected = transaction.isAutoDetected,
        bankAccountId = transaction.bankAccountId,
        userId = userId
    )
    
    // 3. Save to Firebase
    ref.child(transaction.id).setValue(transactionWithUser).await()
    
    // 4. Update category spending
    if (transaction.type == TransactionType.EXPENSE) {
        updateCategorySpending(transaction.category)
    }
}
```

#### Update Category Spending
```kotlin
private suspend fun updateCategorySpending(categoryName: String) {
    // 1. Get all transactions for this category
    val transactions = transactionsRef
        .orderByChild("category")
        .equalTo(categoryName)
        .get()
        .await()
    
    // 2. Calculate total spent
    var totalSpent = 0.0
    for (child in transactions.children) {
        val trans = child.getValue(Transaction::class.java)
        if (trans != null && trans.type == TransactionType.EXPENSE) {
            totalSpent += trans.amount
        }
    }
    
    // 3. Update category
    val category = getCategoryByName(categoryName)
    if (category != null) {
        categoryRef.child(category.id)
            .child("spent")
            .setValue(totalSpent)
            .await()
        
        // 4. Check for budget alerts
        checkAndCreateAlert(category.copy(spent = totalSpent))
    }
}
```

#### Check and Create Alert
```kotlin
private suspend fun checkAndCreateAlert(category: Category) {
    val percentage = (category.spent / category.monthlyLimit) * 100.0
    
    // Delete old alerts for this category
    deleteAlertsForCategory(category.id)
    
    val alertRef = getUserRef("alerts")
    val userId = currentUserId ?: return
    
    when {
        percentage >= 100.0 -> {
            // DANGER: Budget exceeded
            val alert = BudgetAlert(
                categoryId = category.id,
                message = "Budget exceeded in ${category.name}! " +
                         "You've spent ₹${category.spent} of ₹${category.monthlyLimit}",
                type = AlertType.DANGER,
                userId = userId
            )
            alertRef.child(alert.id).setValue(alert).await()
        }
        percentage >= 80.0 -> {
            // WARNING: Approaching limit
            val alert = BudgetAlert(
                categoryId = category.id,
                message = "You're exceeding your budget in ${category.name}. " +
                         "Consider reviewing your spending.",
                type = AlertType.WARNING,
                userId = userId
            )
            alertRef.child(alert.id).setValue(alert).await()
        }
    }
}
```

### Query Optimization

#### Using Indexes
```kotlin
// Firebase indexes speed up queries
// Defined in security rules:

"transactions": {
  ".indexOn": ["type", "category", "date"]
}

// Now these queries are fast:
ref.orderByChild("type").equalTo("EXPENSE")
ref.orderByChild("category").equalTo("Food & Dining")
ref.orderByChild("date").limitToLast(10)
```

#### Offline Persistence
```kotlin
// Enable offline data
FirebaseDatabase.getInstance().apply {
    setPersistenceEnabled(true)
}

// Now the app works offline:
// 1. Reads come from local cache
// 2. Writes queue locally
// 3. Syncs when internet returns
```

---

## ViewModels & State Management

### ViewModel Pattern

```kotlin
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    
    // Input Flows
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                _searchQuery,
                _selectedCategory
            ) { transactions, query, category ->
                // Filter transactions
                // Calculate totals
                // Build UI state
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    // User actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }
}
```

### State Flow

```
User Types in Search Box
    ↓
updateSearchQuery("swiggy")
    ↓
_searchQuery Flow emits "swiggy"
    ↓
combine() re-executes
    ↓
Filters transactions matching "swiggy"
    ↓
Creates new TransactionsUiState
    ↓
_uiState emits new state
    ↓
UI observes uiState Flow
    ↓
Composable recomposes
    ↓
Screen shows filtered results
```

### Combine Multiple Flows

```kotlin
combine(
    repository.getAllTransactions(),  // Flow<List<Transaction>>
    _searchQuery,                     // Flow<String>
    _selectedCategory                 // Flow<String?>
) { transactions, query, category ->
    // This runs whenever ANY flow emits
    val filtered = transactions.filter { transaction ->
        val matchesSearch = transaction.description.contains(query)
        val matchesCategory = category == null || 
                             transaction.category == category
        matchesSearch && matchesCategory
    }
    
    TransactionsUiState(
        filteredTransactions = filtered,
        searchQuery = query,
        selectedCategory = category
    )
}
```

**Benefits:**
1. Automatic updates when data changes
2. No manual refresh needed
3. Reactive programming
4. Clean separation of concerns

---

## UI Components

### Jetpack Compose Basics

#### Simple Composable
```kotlin
@Composable
fun GreetingCard(name: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Hello, $name!",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
```

#### State Management in Compose
```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

### Dashboard Screen Structure

```kotlin
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { TopAppBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Financial Summary Card
            FinancialSummaryCard(uiState.financialSummary)
            
            // Recent Transactions
            RecentTransactionsList(uiState.recentTransactions)
            
            // Budget Alerts
            AlertsList(uiState.alerts)
            
            // Top Categories
            TopCategoriesList(uiState.topCategories)
        }
    }
}
```

### Transactions Screen

```kotlin
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { /* Sort */ }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add */ }) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )
            
            // Category Filter Chips
            CategoryFilterRow(
                categories = uiState.categories,
                selected = uiState.selectedCategory,
                onSelect = { viewModel.filterByCategory(it) }
            )
            
            // Transactions List
            LazyColumn {
                items(uiState.filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(it) }
                    )
                }
            }
        }
    }
}
```

### Budget Planning Screen

```kotlin
@Composable
fun BudgetPlanningScreen(
    navController: NavController,
    viewModel: BudgetPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Budget Planning") }) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Overall Budget Card
            BudgetOverviewCard(
                totalBudget = uiState.monthlyBudget,
                totalSpent = uiState.totalSpent,
                percentage = uiState.budgetUsedPercentage
            )
            
            // Categories List
            LazyColumn {
                items(uiState.categories) { category ->
                    CategoryBudgetItem(
                        category = category,
                        onEdit = { /* Edit dialog */ },
                        onDelete = { viewModel.deleteCategory(it) }
                    )
                }
            }
        }
    }
}
```

### Analytics Screen

```kotlin
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Analytics") }) }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            // Summary Cards
            Row {
                MetricCard("Income", "₹${uiState.totalIncome}")
                MetricCard("Expenses", "₹${uiState.totalExpenses}")
            }
            Row {
                MetricCard("Savings", "₹${uiState.netIncome}")
                MetricCard("Savings Rate", "${uiState.savingsRate}%")
            }
            
            // Expense Pie Chart
            ExpensePieChart(uiState.expensesByCategory)
            
            // Category Breakdown
            CategoryBreakdownList(uiState.expensesByCategory)
        }
    }
}
```

---

## SMS Transaction Detection

### How It Works

#### 1. Receive SMS
```kotlin
@AndroidEntryPoint
class SMSTransactionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var transactionParser: TransactionParser
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            messages.forEach { smsMessage ->
                val messageBody = smsMessage.messageBody
                val sender = smsMessage.originatingAddress
                
                if (isBankSMS(sender, messageBody)) {
                    val transaction = transactionParser.parseTransaction(
                        messageBody, 
                        sender
                    )
                    transaction?.let {
                        showTransactionNotification(context, it)
                    }
                }
            }
        }
    }
}
```

#### 2. Identify Bank SMS
```kotlin
private fun isBankSMS(sender: String?, messageBody: String): Boolean {
    val bankKeywords = listOf(
        "debited", "credited", "withdrawn", "deposited",
        "transaction", "account", "balance", "bank",
        "upi", "paytm", "gpay", "phonepe", "bhim"
    )
    
    return bankKeywords.any { 
        messageBody.lowercase().contains(it) 
    }
}
```

#### 3. Parse Transaction
```kotlin
fun parseTransaction(messageBody: String, sender: String?): SMSTransaction? {
    // Extract amount
    val amount = extractAmount(messageBody) ?: return null
    
    // Determine type (INCOME or EXPENSE)
    val type = determineTransactionType(messageBody)
    
    // Extract merchant name
    val merchant = extractMerchant(messageBody)
    
    // Extract bank name
    val bankName = extractBankName(sender, messageBody)
    
    return SMSTransaction(
        amount = amount,
        type = type,
        merchant = merchant,
        timestamp = System.currentTimeMillis(),
        rawMessage = messageBody,
        bankName = bankName
    )
}
```

#### 4. Extract Amount
```kotlin
private fun extractAmount(message: String): Double? {
    val patterns = listOf(
        "Rs\\.?\\s*([0-9,]+\\.?[0-9]*)",      // Rs. 500
        "INR\\s*([0-9,]+\\.?[0-9]*)",         // INR 500
        "₹\\s*([0-9,]+\\.?[0-9]*)",           // ₹500
        "amount\\s*:?\\s*Rs\\.?\\s*([0-9,]+\\.?[0-9]*)"
    )
    
    for (pattern in patterns) {
        val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            .matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }
    }
    
    return null
}
```

#### 5. Determine Transaction Type
```kotlin
private fun determineTransactionType(message: String): TransactionType {
    val debitKeywords = listOf(
        "debited", "withdrawn", "spent", "paid", "purchase"
    )
    val creditKeywords = listOf(
        "credited", "received", "deposited", "refund"
    )
    
    val messageLower = message.lowercase()
    
    return when {
        debitKeywords.any { messageLower.contains(it) } -> 
            TransactionType.EXPENSE
        creditKeywords.any { messageLower.contains(it) } -> 
            TransactionType.INCOME
        else -> TransactionType.EXPENSE // Default
    }
}
```

#### 6. Suggest Category
```kotlin
fun suggestCategory(merchant: String?, message: String): String {
    val messageLower = message.lowercase()
    val merchantLower = merchant?.lowercase() ?: ""
    
    return when {
        messageLower.contains("swiggy") || 
        messageLower.contains("zomato") ||
        merchantLower.contains("restaurant") -> 
            "Food & Dining"
            
        messageLower.contains("amazon") ||
        messageLower.contains("flipkart") -> 
            "Shopping"
            
        messageLower.contains("uber") ||
        messageLower.contains("ola") -> 
            "Transportation"
            
        messageLower.contains("electricity") ||
        messageLower.contains("water") -> 
            "Utilities"
            
        else -> "Others"
    }
}
```

#### 7. Show Notification
```kotlin
private fun showTransactionNotification(
    context: Context?, 
    transaction: SMSTransaction
) {
    val notificationManager = context?.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager
    
    val notification = NotificationCompat.Builder(
        context, 
        BudgetBuddyApplication.TRANSACTION_CHANNEL_ID
    )
        .setSmallIcon(R.drawable.ic_transaction)
        .setContentTitle("New Transaction Detected")
        .setContentText("₹${transaction.amount} at ${transaction.merchant}")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_add,
            "Add to Budget Buddy",
            getPendingIntent(context, transaction)
        )
        .build()
    
    notificationManager.notify(transaction.timestamp.toInt(), notification)
}
```

### Permission Required

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<receiver
    android:name=".service.SMSTransactionReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

---

## Setup & Installation

### Prerequisites

1. **Android Studio** (Arctic Fox or newer)
2. **JDK 17**
3. **Android SDK** (API 26+)
4. **Firebase Account** (free)
5. **Physical Android Device** (for SMS testing) or Emulator

### Firebase Setup

#### Step 1: Create Firebase Project
```
1. Go to https://console.firebase.google.com
2. Click "Add Project"
3. Enter project name: "Budget Buddy"
4. Disable Google Analytics (optional)
5. Click "Create Project"
```

#### Step 2: Add Android App
```
1. Click "Add app" → Android icon
2. Package name: com.budgetbuddy.app
3. Download google-services.json
4. Place in app/ directory
```

#### Step 3: Enable Authentication
```
1. Go to Authentication → Sign-in method
2. Enable Email/Password
3. Save
```

#### Step 4: Create Realtime Database
```
1. Go to Realtime Database
2. Click "Create Database"
3. Choose location: (closest to you)
4. Start in Test Mode
5. Click "Enable"
```

#### Step 5: Configure Security Rules
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "transactions": {
          ".indexOn": ["type", "category", "date"]
        },
        "categories": {
          ".indexOn": ["name"]
        },
        "budgets": {
          ".indexOn": ["month", "year"]
        },
        "alerts": {
          ".indexOn": ["isRead", "categoryId"]
        }
      }
    }
  }
}
```

### Build the App

#### Step 1: Clone or Create Project
```bash
# If cloning
git clone https://github.com/yourusername/budget-buddy.git
cd budget-buddy

# If creating new
# Create new Android Studio project
# Copy all files from provided code
```

#### Step 2: Add google-services.json
```
1. Place google-services.json in app/ directory
2. Verify it's in the correct location
```

#### Step 3: Sync Gradle
```
1. Open project in Android Studio
2. Click "Sync Now" when prompted
3. Wait for dependencies to download
```

#### Step 4: Build Project
```
1. Build → Clean Project
2. Build → Rebuild Project
3. Fix any errors (should be none if using provided code)
```

#### Step 5: Run App
```
1. Connect Android device or start emulator
2. Click Run button (green play icon)
3. Select device
4. Wait for installation
```

### Testing

#### Test Authentication
```
1. Launch app
2. Click "Sign up"
3. Enter: test@example.com / password123 / Test User
4. Should create account and navigate to dashboard
5. Sign out
6. Sign in with same credentials
7. Should login successfully
```

#### Test Transactions
```
1. Click "+" FAB on Transactions screen
2. Add transaction:
   - Type: Expense
   - Amount: 500
   - Category: Food & Dining
   - Description: Lunch
3. Save
4. Should appear in list
5. Check Dashboard - should update totals
```

#### Test Budget
```
1. Go to Budget Planning
2. Click on Food & Dining category
3. Change limit to 1000
4. Add expense of 900
5. Should show WARNING alert (90%)
6. Add expense of 200
7. Should show DANGER alert (110%)
```

#### Test SMS (Requires physical device)
```
1. Grant SMS permissions
2. Send SMS to device:
   "Rs.500 debited from A/c XX1234 at Swiggy"
3. Should receive notification
4. Click "Add to Budget Buddy"
5. Should pre-fill transaction form
```

---

## How Everything Works Together

### Complete Flow Example: Adding an Expense

```
1. USER ACTION
   User opens app → DashboardScreen displays

2. NAVIGATION
   User clicks Transactions tab
   BottomNavigationBar onClick handler
   → navController.navigate("transactions")
   → TransactionsScreen composable loads

3. SCREEN INITIALIZATION
   TransactionsScreen created
   → @Composable function executes
   → hiltViewModel() creates/retrieves TransactionsViewModel
   → ViewModel.init{} block runs
   → loadTransactions() called
   → loadCategories() called

4. DATA LOADING
   TransactionsViewModel.loadTransactions()
   → Calls repository.getAllTransactions()
   → FirebaseRepository creates Flow<List<Transaction>>
   → Firebase listener attached to "users/{uid}/transactions"
   → Data streams to ViewModel
   → combine() with search/filter flows
   → Creates TransactionsUiState
   → Emits to _uiState Flow

5. UI RENDERING
   Screen observes uiState.collectAsState()
   → Recomposes when state changes
   → LazyColumn displays transactions
   → Search bar shows current query
   → Category chips show filters

6. USER ADDS TRANSACTION
   User clicks FAB (+)
   → Dialog/Bottom sheet opens
   → User enters:
      * Amount: 500
      * Category: Food & Dining
      * Description: Lunch at cafe
   → User clicks Save

7. SAVE TRANSACTION
   Dialog calls viewModel.addTransaction(transaction)
   → ViewModel.viewModelScope.launch{}
   → repository.addTransaction(transaction)
   
8. REPOSITORY PROCESSING
   FirebaseRepository.addTransaction()
   → Adds userId to transaction object
   → Saves to Firebase:
      database.ref("users/{uid}/transactions/{id}")
        .setValue(transaction)
        .await()

9. UPDATE CATEGORY SPENDING
   → updateCategorySpending(transaction.category)
   → Query all transactions for category
   → Calculate total spent
   → Update category.spent field
   → database.ref("users/{uid}/categories/{catId}/spent")
       .setValue(totalSpent)

10. CHECK BUDGET ALERT
    → checkAndCreateAlert(category)
    → Calculate percentage: (spent / limit) * 100
    → If >= 100%:
       - Create BudgetAlert with type DANGER
       - Save to database
    → If >= 80%:
       - Create BudgetAlert with type WARNING
       - Save to database

11. FIREBASE SYNC
    Firebase broadcasts change events
    → All listeners receive update
    → getAllTransactions() Flow emits new list
    → getAllCategories() Flow emits updated category
    → getAllAlerts() Flow emits new alert

12. VIEWMODEL UPDATES
    TransactionsViewModel receives new data
    → combine() re-executes
    → Filters transactions with search/category
    → Calculates new totals
    → Creates new TransactionsUiState
    → Emits to _uiState

13. UI RECOMPOSES
    TransactionsScreen observes new state
    → Composable function re-executes
    → LazyColumn updates with new transaction
    → Totals display updated values

14. DASHBOARD UPDATES
    (If user switches to Dashboard)
    DashboardViewModel also receives update
    → Shows transaction in recent list
    → Updates financial summary
    → Displays new alert badge

15. CROSS-DEVICE SYNC
    Other devices logged in as same user
    → Firebase pushes update
    → Their listeners receive new data
    → Their UI updates automatically
```

### State Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      FIREBASE DATABASE                       │
│                    (Source of Truth)                         │
└───────────────┬────────────────────────┬────────────────────┘
                │                        │
         Change Events              Change Events
                │                        │
                ▼                        ▼
┌────────────────────────┐   ┌─────────────────────────┐
│  TransactionsViewModel │   │   DashboardViewModel    │
│                        │   │                         │
│  getAllTransactions()  │   │  getFinancialSummary()  │
│          ↓             │   │          ↓              │
│    combine with        │   │    combine with         │
│  search & filters      │   │  categories & alerts    │
│          ↓             │   │          ↓              │
│  TransactionsUiState   │   │   DashboardUiState      │
│          ↓             │   │          ↓              │
│    emit to Flow        │   │    emit to Flow         │
└────────────┬───────────┘   └──────────┬──────────────┘
             │                          │
             ▼                          ▼
┌────────────────────────┐   ┌─────────────────────────┐
│  TransactionsScreen    │   │    DashboardScreen      │
│                        │   │                         │
│  collectAsState()      │   │   collectAsState()      │
│          ↓             │   │          ↓              │
│    Recompose UI        │   │     Recompose UI        │
└────────────────────────┘   └─────────────────────────┘
```

### Dependency Injection Graph

```
┌─────────────────────────────────────────────────┐
│           @HiltAndroidApp                       │
│         BudgetBuddyApplication                  │
└───────────────────┬─────────────────────────────┘
                    │
                    ├── Provides FirebaseAuth
                    ├── Provides FirebaseDatabase
                    ├── Provides AuthenticationManager
                    └── Provides FirebaseRepository
                    │
┌───────────────────▼─────────────────────────────┐
│              @AndroidEntryPoint                  │
│                MainActivity                      │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│           Navigation Component                   │
│                                                  │
│  ├── LoginScreen                                │
│  │   └── @HiltViewModel AuthViewModel           │
│  │       └── Injects: AuthManager, Repository   │
│  │                                               │
│  ├── DashboardScreen                            │
│  │   └── @HiltViewModel DashboardViewModel      │
│  │       └── Injects: Repository                │
│  │                                               │
│  ├── TransactionsScreen                         │
│  │   └── @HiltViewModel TransactionsViewModel   │
│  │       └── Injects: Repository                │
│  │                                               │
│  ├── BudgetPlanningScreen                       │
│  │   └── @HiltViewModel BudgetPlanningViewModel │
│  │       └── Injects: Repository                │
│  │                                               │
│  └── AnalyticsScreen                            │
│      └── @HiltViewModel AnalyticsViewModel      │
│          └── Injects: Repository                │
└──────────────────────────────────────────────────┘
```

### Data Flow Sequence

```
1. USER INTERACTION
   User types in search box
   
2. EVENT HANDLER
   SearchBar.onQueryChange(query)
   
3. VIEWMODEL
   viewModel.updateSearchQuery(query)
   _searchQuery.value = query
   
4. FLOW EMISSION
   _searchQuery Flow emits new value
   
5. COMBINE OPERATOR
   combine(transactions, searchQuery, category) {}
   Re-executes with new search value
   
6. FILTERING
   transactions.filter { it.description.contains(query) }
   
7. STATE CREATION
   TransactionsUiState(filteredTransactions = filtered)
   
8. STATE EMISSION
   _uiState.value = newState
   
9. COLLECTION
   val uiState by viewModel.uiState.collectAsState()
   
10. RECOMPOSITION
    Composable re-executes with new state
    
11. UI UPDATE
    LazyColumn displays filtered results
```

---

## Summary

Budget Buddy is a comprehensive personal finance app that demonstrates modern Android development:

### Architecture Highlights
- **MVVM Pattern**: Clean separation of UI, business logic, and data
- **Jetpack Compose**: Modern, declarative UI framework
- **Firebase Integration**: Cloud database and authentication
- **Dependency Injection**: Hilt for managing dependencies
- **Reactive Programming**: Kotlin Flows for real-time updates

### Key Features
- ✅ User authentication (email/password)
- ✅ Transaction tracking (income/expenses)
- ✅ Budget management (per-category limits)
- ✅ Real-time alerts (budget warnings)
- ✅ Analytics & insights (spending patterns)
- ✅ SMS auto-detection (automatic transaction logging)
- ✅ Multi-device sync (via Firebase)
- ✅ Offline support (local caching)

### Technical Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Backend**: Firebase (Auth + Realtime Database)
- **DI**: Hilt
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose

### Data Security
- User data isolated by userId
- Firebase Security Rules enforce access control
- Password hashing via Firebase Auth
- HTTPS encryption for all data transfer

This app serves as an excellent example of:
- Modern Android development practices
- Clean architecture principles
- Reactive state management
- Cloud-native mobile apps
- Real-world feature implementation

---

## Next Steps

To extend this app, you could add:
1. **Recurring Transactions**: Auto-create monthly bills
2. **Multiple Accounts**: Track different bank accounts
3. **Export Reports**: Generate PDF/Excel reports
4. **Goal Setting**: Savings goals with progress tracking
5. **Bill Reminders**: Notifications for upcoming bills
6. **Shared Budgets**: Family budget management
7. **Dark Mode**: Theme switching
8. **Biometric Auth**: Fingerprint/Face unlock
9. **Cloud Backup**: Export/import data
10. **AI Insights**: Spending predictions with ML

The app is designed to be extensible and maintainable for future enhancements!
