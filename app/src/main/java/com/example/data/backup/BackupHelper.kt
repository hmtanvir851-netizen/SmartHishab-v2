package com.example.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.data.local.entities.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupParseResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = 0L,
    val accountsCount: Int = 0,
    val categoriesCount: Int = 0,
    val transactionsCount: Int = 0,
    val debtsCount: Int = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList()
)

object BackupHelper {

    private const val TAG = "BackupHelper"
    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "SmartHishab"

    fun generateBackupString(
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        debts: List<DebtEntity>
    ): String {
        val rootJson = JSONObject().apply {
            put("app", APP_IDENTIFIER)
            put("version", BACKUP_VERSION)
            put("timestamp", System.currentTimeMillis())

            // Accounts
            val accountsArray = JSONArray()
            accounts.forEach { acc ->
                accountsArray.put(JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("type", acc.type.name)
                    put("balance", acc.balance)
                    put("accountNumber", acc.accountNumber)
                    put("colorHex", acc.colorHex)
                    put("iconName", acc.iconName)
                })
            }
            put("accounts", accountsArray)

            // Categories
            val categoriesArray = JSONArray()
            categories.forEach { cat ->
                categoriesArray.put(JSONObject().apply {
                    put("id", cat.id)
                    put("nameEn", cat.nameEn)
                    put("nameBn", cat.nameBn)
                    put("type", cat.type.name)
                    put("monthlyBudget", cat.monthlyBudget)
                    put("colorHex", cat.colorHex)
                    put("iconName", cat.iconName)
                })
            }
            put("categories", categoriesArray)

            // Transactions
            val transactionsArray = JSONArray()
            transactions.forEach { tx ->
                transactionsArray.put(JSONObject().apply {
                    put("id", tx.id)
                    put("amount", tx.amount)
                    put("type", tx.type.name)
                    put("accountId", tx.accountId)
                    if (tx.targetAccountId != null) put("targetAccountId", tx.targetAccountId)
                    if (tx.categoryId != null) put("categoryId", tx.categoryId)
                    put("categoryName", tx.categoryName)
                    put("note", tx.note)
                    put("timestamp", tx.timestamp)
                    put("dateString", tx.dateString)
                })
            }
            put("transactions", transactionsArray)

            // Debts
            val debtsArray = JSONArray()
            debts.forEach { debt ->
                debtsArray.put(JSONObject().apply {
                    put("id", debt.id)
                    put("personName", debt.personName)
                    put("phoneNumber", debt.phoneNumber)
                    put("amount", debt.amount)
                    put("repaidAmount", debt.repaidAmount)
                    put("type", debt.type.name)
                    put("dueDate", debt.dueDate)
                    put("note", debt.note)
                    put("isSettled", debt.isSettled)
                    put("createdAt", debt.createdAt)
                })
            }
            put("debts", debtsArray)
        }

        val jsonString = rootJson.toString()
        val checksum = calculateChecksum(jsonString)

        // Wrapper JSON with secure checksum signature and base64 encoded payload
        val securePayload = JSONObject().apply {
            put("app", APP_IDENTIFIER)
            put("version", BACKUP_VERSION)
            put("checksum", checksum)
            put("payloadBase64", Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
        }

        return securePayload.toString(2)
    }

    fun parseAndValidateBackup(inputContent: String): BackupParseResult {
        try {
            val trimmed = inputContent.trim()
            if (trimmed.isEmpty()) {
                return BackupParseResult(isValid = false, errorMessage = "Backup content is empty.")
            }

            var jsonStringPayload = ""

            // Check if wrapper JSON or direct raw JSON
            if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                if (root.has("payloadBase64") && root.has("checksum")) {
                    val base64Str = root.getString("payloadBase64")
                    val expectedChecksum = root.getString("checksum")
                    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                    jsonStringPayload = String(decodedBytes, Charsets.UTF_8)

                    val actualChecksum = calculateChecksum(jsonStringPayload)
                    if (actualChecksum != expectedChecksum) {
                        return BackupParseResult(isValid = false, errorMessage = "Backup file checksum verification failed (Corrupted or modified).")
                    }
                } else {
                    jsonStringPayload = trimmed
                }
            } else {
                return BackupParseResult(isValid = false, errorMessage = "Invalid backup file format.")
            }

            val dataJson = JSONObject(jsonStringPayload)
            val app = dataJson.optString("app", "")
            if (app != APP_IDENTIFIER && !dataJson.has("transactions")) {
                return BackupParseResult(isValid = false, errorMessage = "Unrecognized backup file source.")
            }

            val timestamp = dataJson.optLong("timestamp", System.currentTimeMillis())

            // Parse Accounts
            val accountsList = mutableListOf<AccountEntity>()
            val accountsArray = dataJson.optJSONArray("accounts") ?: JSONArray()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                accountsList.add(
                    AccountEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        type = AccountType.valueOf(obj.optString("type", AccountType.CASH.name)),
                        balance = obj.optDouble("balance", 0.0),
                        accountNumber = obj.optString("accountNumber", ""),
                        colorHex = obj.optString("colorHex", "#00897B"),
                        iconName = obj.optString("iconName", "wallet")
                    )
                )
            }

            // Parse Categories
            val categoriesList = mutableListOf<CategoryEntity>()
            val categoriesArray = dataJson.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until categoriesArray.length()) {
                val obj = categoriesArray.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = obj.optLong("id", 0L),
                        nameEn = obj.getString("nameEn"),
                        nameBn = obj.optString("nameBn", obj.getString("nameEn")),
                        type = TransactionType.valueOf(obj.optString("type", TransactionType.EXPENSE.name)),
                        monthlyBudget = obj.optDouble("monthlyBudget", 0.0),
                        colorHex = obj.optString("colorHex", "#1E88E5"),
                        iconName = obj.optString("iconName", "category")
                    )
                )
            }

            // Parse Transactions
            val transactionsList = mutableListOf<TransactionEntity>()
            val transactionsArray = dataJson.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                transactionsList.add(
                    TransactionEntity(
                        id = obj.optLong("id", 0L),
                        amount = obj.getDouble("amount"),
                        type = TransactionType.valueOf(obj.optString("type", TransactionType.EXPENSE.name)),
                        accountId = obj.getLong("accountId"),
                        targetAccountId = if (obj.has("targetAccountId") && !obj.isNull("targetAccountId")) obj.getLong("targetAccountId") else null,
                        categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.getLong("categoryId") else null,
                        categoryName = obj.optString("categoryName", ""),
                        note = obj.optString("note", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        dateString = obj.optString("dateString", "")
                    )
                )
            }

            // Parse Debts
            val debtsList = mutableListOf<DebtEntity>()
            val debtsArray = dataJson.optJSONArray("debts") ?: JSONArray()
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                val isSettled = obj.optBoolean("isSettled", false)
                val amt = obj.getDouble("amount")
                val defaultRepaid = if (isSettled) amt else 0.0
                debtsList.add(
                    DebtEntity(
                        id = obj.optLong("id", 0L),
                        personName = obj.getString("personName"),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        amount = amt,
                        repaidAmount = obj.optDouble("repaidAmount", defaultRepaid),
                        type = DebtType.valueOf(obj.optString("type", DebtType.LENT.name)),
                        dueDate = obj.optString("dueDate", ""),
                        note = obj.optString("note", ""),
                        isSettled = isSettled,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            return BackupParseResult(
                isValid = true,
                timestamp = timestamp,
                accountsCount = accountsList.size,
                categoriesCount = categoriesList.size,
                transactionsCount = transactionsList.size,
                debtsCount = debtsList.size,
                accounts = accountsList,
                categories = categoriesList,
                transactions = transactionsList,
                debts = debtsList
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing backup content: ${e.message}", e)
            return BackupParseResult(isValid = false, errorMessage = "Failed to parse backup: ${e.message}")
        }
    }

    fun readContentFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    fun writeContentToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing backup to Uri: ${e.message}")
            false
        }
    }

    private fun calculateChecksum(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun getDefaultBackupFileName(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "SmartHishab_Backup_$dateStr.json"
    }
}
