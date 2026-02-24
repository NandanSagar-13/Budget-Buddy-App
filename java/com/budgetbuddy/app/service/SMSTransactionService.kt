package com.budgetbuddy.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.budgetbuddy.app.data.model.SMSTransaction
import com.budgetbuddy.app.data.model.TransactionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

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
                
                // Check if SMS is from a bank
                if (isBankSMS(sender, messageBody)) {
                    val transaction = transactionParser.parseTransaction(messageBody, sender)
                    transaction?.let {
                        // Notify user and ask for categorization
                        showTransactionNotification(context, it)
                    }
                }
            }
        }
    }
    
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
    
    private fun showTransactionNotification(context: Context?, transaction: SMSTransaction) {
        // Implementation for showing notification
        // This will trigger a notification asking user to categorize the transaction
    }
}

class TransactionParser @Inject constructor() {
    
    fun parseTransaction(messageBody: String, sender: String?): SMSTransaction? {
        val amount = extractAmount(messageBody) ?: return null
        val type = determineTransactionType(messageBody)
        val merchant = extractMerchant(messageBody)
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
    
    private fun extractAmount(message: String): Double? {
        // Patterns for Indian currency
        val patterns = listOf(
            "Rs\\.?\\s*([0-9,]+\\.?[0-9]*)",
            "INR\\s*([0-9,]+\\.?[0-9]*)",
            "₹\\s*([0-9,]+\\.?[0-9]*)",
            "amount\\s*:?\\s*Rs\\.?\\s*([0-9,]+\\.?[0-9]*)",
            "of\\s*Rs\\.?\\s*([0-9,]+\\.?[0-9]*)"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "")
                return amountStr?.toDoubleOrNull()
            }
        }
        
        return null
    }
    
    private fun determineTransactionType(message: String): TransactionType {
        val debitKeywords = listOf("debited", "withdrawn", "spent", "paid", "purchase")
        val creditKeywords = listOf("credited", "received", "deposited", "refund")
        
        val messageLower = message.lowercase()
        
        return when {
            debitKeywords.any { messageLower.contains(it) } -> TransactionType.EXPENSE
            creditKeywords.any { messageLower.contains(it) } -> TransactionType.INCOME
            else -> TransactionType.EXPENSE // Default to expense
        }
    }
    
    private fun extractMerchant(message: String): String? {
        // Patterns to extract merchant name
        val patterns = listOf(
            "at\\s+([A-Z][A-Za-z0-9\\s]+?)(?:on|\\.|$)",
            "to\\s+([A-Z][A-Za-z0-9\\s]+?)(?:on|\\.|$)",
            "for\\s+([A-Z][A-Za-z0-9\\s]+?)(?:on|\\.|$)"
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern).matcher(message)
            if (matcher.find()) {
                return matcher.group(1)?.trim()
            }
        }
        
        // Check for UPI patterns
        val upiPattern = Pattern.compile("UPI/([A-Za-z0-9@]+)")
        val upiMatcher = upiPattern.matcher(message)
        if (upiMatcher.find()) {
            return upiMatcher.group(1)
        }
        
        return null
    }
    
    private fun extractBankName(sender: String?, message: String): String? {
        // Common bank identifiers
        val banks = mapOf(
            "HDFCBK" to "HDFC Bank",
            "SBIINB" to "State Bank of India",
            "ICICIB" to "ICICI Bank",
            "AXISBK" to "Axis Bank",
            "KOTAKB" to "Kotak Mahindra Bank",
            "PNBSMS" to "Punjab National Bank",
            "BOISMS" to "Bank of India"
        )
        
        sender?.let { senderNum ->
            banks.entries.forEach { (code, name) ->
                if (senderNum.contains(code, ignoreCase = true)) {
                    return name
                }
            }
        }
        
        // Check message body for bank name
        banks.values.forEach { bankName ->
            if (message.contains(bankName, ignoreCase = true)) {
                return bankName
            }
        }
        
        return null
    }
    
    fun suggestCategory(merchant: String?, message: String): String {
        val messageLower = message.lowercase()
        val merchantLower = merchant?.lowercase() ?: ""
        
        return when {
            // Food & Dining
            messageLower.contains("swiggy") || 
            messageLower.contains("zomato") ||
            merchantLower.contains("restaurant") ||
            merchantLower.contains("cafe") ||
            merchantLower.contains("food") -> "Food & Dining"
            
            // Shopping
            messageLower.contains("amazon") ||
            messageLower.contains("flipkart") ||
            merchantLower.contains("mall") ||
            merchantLower.contains("store") -> "Shopping"
            
            // Transportation
            messageLower.contains("uber") ||
            messageLower.contains("ola") ||
            messageLower.contains("petrol") ||
            messageLower.contains("fuel") -> "Transportation"
            
            // Utilities
            messageLower.contains("electricity") ||
            messageLower.contains("water") ||
            messageLower.contains("gas") ||
            messageLower.contains("internet") -> "Utilities"
            
            // Entertainment
            messageLower.contains("netflix") ||
            messageLower.contains("prime") ||
            messageLower.contains("movie") ||
            merchantLower.contains("cinema") -> "Entertainment"
            
            // Healthcare
            messageLower.contains("pharmacy") ||
            messageLower.contains("hospital") ||
            messageLower.contains("doctor") ||
            merchantLower.contains("medical") -> "Healthcare"
            
            else -> "Others"
        }
    }
}

// Notification Helper
class TransactionNotificationHelper @Inject constructor(
    private val context: Context
) {
    fun showCategorizeNotification(transaction: SMSTransaction) {
        // Implementation to show notification with action buttons
        // for user to categorize the detected transaction
    }
}
