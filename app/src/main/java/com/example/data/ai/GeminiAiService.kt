package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entities.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedAiResult(
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val accountName: String,
    val targetAccountName: String? = null,
    val note: String,
    val aiAdvice: String = ""
)

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun parseNaturalInput(
        userInput: String,
        userLang: String = "bn"
    ): ParsedAiResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key missing or default placeholder. Using smart offline regex fallback.")
            return@withContext fallbackParse(userInput, userLang)
        }

        val promptText = """
            You are SmartHishab AI financial parser. Parse the following user text or voice input into structured transaction data.
            Available Transaction Types: "EXPENSE", "INCOME", "TRANSFER".
            Available Accounts: "Cash", "Bank", "Bkash", "Nagad", "Rocket", "Card".
            Available Categories: "Food & Grocery", "Transport", "Bills & Utilities", "Shopping", "Healthcare", "Education", "Entertainment", "Salary", "Business", "Investment", "Other".

            User Text: "$userInput"

            Respond ONLY with a valid raw JSON object matching this schema without markdown code blocks:
            {
              "amount": 500.0,
              "type": "EXPENSE",
              "category": "Food & Grocery",
              "account": "Bkash",
              "targetAccount": null,
              "note": "Groceries purchase",
              "advice": "Short friendly comment in ${if (userLang == "bn") "Bengali" else "English"} about this expense/income"
            }
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error ${response.code}: $responseBody")
                return@withContext fallbackParse(userInput, userLang)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Clean code fences if present
            val cleanedJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanedJson)

            val amount = parsedObj.optDouble("amount", 0.0)
            val typeStr = parsedObj.optString("type", "EXPENSE")
            val type = when (typeStr.uppercase()) {
                "INCOME" -> TransactionType.INCOME
                "TRANSFER" -> TransactionType.TRANSFER
                else -> TransactionType.EXPENSE
            }
            val category = parsedObj.optString("category", "Other")
            val account = parsedObj.optString("account", "Cash")
            val targetAccount = if (parsedObj.has("targetAccount") && !parsedObj.isNull("targetAccount")) {
                parsedObj.getString("targetAccount")
            } else null
            val note = parsedObj.optString("note", userInput)
            val advice = parsedObj.optString("advice", "")

            ParsedAiResult(
                amount = amount,
                type = type,
                categoryName = category,
                accountName = account,
                targetAccountName = targetAccount,
                note = note,
                aiAdvice = advice
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini parsing exception: ${e.message}", e)
            fallbackParse(userInput, userLang)
        }
    }

    suspend fun generateSpendingAnalysis(
        transactionsSummaryText: String,
        totalIncome: Double,
        totalExpense: Double,
        monthlyBudget: Double,
        currencySymbol: String,
        userLang: String = "bn"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key missing or default placeholder. Using smart offline spending advice fallback.")
            return@withContext fallbackSpendingAdvice(totalIncome, totalExpense, monthlyBudget, currencySymbol, userLang)
        }

        val promptText = """
            You are SmartHishab AI financial advisor. Analyze the following monthly spending habits, categories, and budget data for the user.
            
            Monthly Income: $currencySymbol$totalIncome
            Monthly Expense: $currencySymbol$totalExpense
            Monthly Budget Limit: $currencySymbol$monthlyBudget
            Category Breakdown & Recent Transactions:
            $transactionsSummaryText

            Provide a concise, encouraging, and actionable financial insight report in ${if (userLang == "bn") "Bengali" else "English"}.
            Highlight top spending areas, budget utilization health, and 2-3 practical tips to save money.
            Use bullet points and friendly formatting.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error ${response.code}: $responseBody")
                return@withContext fallbackSpendingAdvice(totalIncome, totalExpense, monthlyBudget, currencySymbol, userLang)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            rawText.ifBlank { fallbackSpendingAdvice(totalIncome, totalExpense, monthlyBudget, currencySymbol, userLang) }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini analysis exception: ${e.message}", e)
            fallbackSpendingAdvice(totalIncome, totalExpense, monthlyBudget, currencySymbol, userLang)
        }
    }

    private fun fallbackSpendingAdvice(
        totalIncome: Double,
        totalExpense: Double,
        monthlyBudget: Double,
        currencySymbol: String,
        userLang: String
    ): String {
        val savings = totalIncome - totalExpense
        val isBn = userLang == "bn"
        val budgetRatio = if (monthlyBudget > 0) totalExpense / monthlyBudget else 0.0

        return if (isBn) {
            """
                📊 স্মার্ট আর্থিক বিশ্লেষণ রিপোর্ট:
                
                • মোট আয়: $currencySymbol$totalIncome
                • মোট খরচ: $currencySymbol$totalExpense
                • মাসিক বাজেট খরচের হার: ${(budgetRatio * 100).toInt()}%
                
                💡 এআই পরামর্শ:
                1. ${if (budgetRatio >= 0.8) "আপনার বাজেট প্রায় শেষ হতে চলেছে! অপ্রয়োজনীয় কেনাকাটা এবং আউটডোর ফুড খরচ কিছুটা কমান।" else "আপনার খরচ বাজেটের নিয়ন্ত্রণের ভেতরে আছে। দারুণ!"}
                2. ${if (savings > 0) "প্রতি মাসে আয়ের অন্তত ২০% সঞ্চয় বা ইমার্জেন্সি ফান্ডে রাখার চেষ্টা করুন।" else "আয় অপেক্ষা খরচ বেশি হচ্ছে, কেনাকাটার তালিকা আগে থেকেই ঠিক করে রাখুন।"}
            """.trimIndent()
        } else {
            """
                📊 Smart Spending Analysis Report:
                
                • Total Income: $currencySymbol$totalIncome
                • Total Expense: $currencySymbol$totalExpense
                • Budget Utilization: ${(budgetRatio * 100).toInt()}%
                
                💡 Actionable Tips:
                1. ${if (budgetRatio >= 0.8) "Your budget utilization is high! Try cutting down non-essential dining and shopping." else "Great job! Your spending is well within the allocated budget."}
                2. ${if (savings > 0) "Aim to save at least 20% of your income into an emergency fund." else "Your expenses exceed income this month. Plan purchases ahead with a priority list."}
            """.trimIndent()
        }
    }

    private fun fallbackParse(input: String, userLang: String = "bn"): ParsedAiResult {
        // Extract digits/numbers from string (e.g. 500, 500.00, ৫০০)
        val bnDigits = mapOf('০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4', '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9')
        var convertedInput = input
        bnDigits.forEach { (bn, en) -> convertedInput = convertedInput.replace(bn, en) }

        val numberRegex = Regex("""\d+(\.\d+)?""")
        val match = numberRegex.find(convertedInput)
        val amount = match?.value?.toDoubleOrNull() ?: 100.0

        val lower = convertedInput.lowercase()

        // Account detection
        val account = when {
            lower.contains("bkash") || lower.contains("বিকাশ") -> "Bkash (বিকাশ)"
            lower.contains("nagad") || lower.contains("নগদ") -> "Nagad (নগদ)"
            lower.contains("rocket") || lower.contains("রকেট") -> "Rocket (রকেট)"
            lower.contains("bank") || lower.contains("ব্যাংক") -> "Bank Account"
            else -> "Cash (ক্যাশ)"
        }

        // Type & Category
        val isIncome = lower.contains("salary") || lower.contains("বেতন") || lower.contains("আয়") || lower.contains("প পেলাম") || lower.contains("received") || lower.contains("got")
        val isTransfer = lower.contains("transfer") || lower.contains("ট্রান্সফার") || lower.contains("পাঠালাম") || lower.contains("সেন্ড")

        val (type, category) = when {
            isTransfer -> TransactionType.TRANSFER to "Internal Transfer"
            isIncome -> TransactionType.INCOME to "Salary"
            lower.contains("rickshaw") || lower.contains("bus") || lower.contains("যাতায়াত") || lower.contains("ভাড়া") || lower.contains("uber") -> TransactionType.EXPENSE to "Transport"
            lower.contains("bill") || lower.contains("বিল") || lower.contains("কারেন্ট") || lower.contains("wifi") -> TransactionType.EXPENSE to "Bills & Utilities"
            lower.contains("shop") || lower.contains("কাপড়") || lower.contains("জামাকাপড়") -> TransactionType.EXPENSE to "Shopping"
            lower.contains("doctor") || lower.contains("ওষুধ") || lower.contains("মেডিসিন") -> TransactionType.EXPENSE to "Healthcare"
            else -> TransactionType.EXPENSE to "Food & Grocery"
        }

        val offlineAdvice = if (userLang == "bn")
            "📶 (অফলাইন প্রসেসিং) ইন্টারনেট না থাকায় লোকাল স্মার্ট অ্যালগরিদম দিয়ে ইনপুট চিহ্নিত করা হয়েছে।"
        else
            "📶 (Offline Processing) Internet unavailable. Input recognized using local smart algorithm."

        return ParsedAiResult(
            amount = amount,
            type = type,
            categoryName = category,
            accountName = account,
            targetAccountName = if (type == TransactionType.TRANSFER) "Cash (ক্যাশ)" else null,
            note = input,
            aiAdvice = offlineAdvice
        )
    }
}
