package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.ai.ParsedAiResult
import com.example.data.local.AppDatabase
import com.example.data.local.ThemeDataStore
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.entities.*
import com.example.data.localization.CurrencyManager
import com.example.data.notification.BudgetNotificationManager
import com.example.data.repository.MoneyRepository
import com.example.ui.state.DashboardUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MoneyRepository(db.moneyDao(), UserPreferences(application))
    private val themeDataStore = ThemeDataStore(application)

    // Theme DataStore Flow
    val themeMode: StateFlow<String> = themeDataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM.value)

    val themePreset: StateFlow<String> = themeDataStore.themePresetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EMERALD_GREEN")

    // Preferences state
    val languageCode = MutableStateFlow(repository.preferences.languageCode)
    val currencyCode = MutableStateFlow(repository.preferences.currencyCode)
    val currencySymbol = MutableStateFlow(repository.preferences.currencySymbol)
    val isPinLockEnabled = MutableStateFlow(repository.preferences.isPinLockEnabled)
    val isPinUnlocked = MutableStateFlow(!repository.preferences.isPinLockEnabled)
    val isOnboardingCompleted = MutableStateFlow(repository.preferences.isOnboardingCompleted)
    val totalMonthlyBudget = MutableStateFlow(repository.preferences.totalMonthlyBudget)
    val isBalanceHidden = MutableStateFlow(repository.preferences.isBalanceHidden)

    // Session and Security State
    val isLoggedIn = MutableStateFlow(repository.preferences.isLoggedIn)
    val loggedInUserId = MutableStateFlow(repository.preferences.loggedInUserId)
    val isLoginSecurityEnabled = MutableStateFlow(repository.preferences.isLoginSecurityEnabled)
    val isBiometricEnabled = MutableStateFlow(repository.preferences.isBiometricEnabled)

    // Room DB Flows
    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Balances
    val totalNetWorth = accounts.map { list -> list.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthExpenses = transactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthIncome = transactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // AI Parsing State
    val isAiParsing = MutableStateFlow(false)
    val pendingAiResult = MutableStateFlow<ParsedAiResult?>(null)
    val aiErrorMessage = MutableStateFlow<String?>(null)
    val aiSpendingInsight = MutableStateFlow<String?>(null)
    val isAnalyzingHabits = MutableStateFlow(false)

    // Budget warning alert state
    val show80PercentAlert = MutableStateFlow(false)
    val show100PercentAlert = MutableStateFlow(false)

    // Currency Converter State
    val conversionRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val isLiveExchangeRates = MutableStateFlow(false)
    val isLoadingExchangeRates = MutableStateFlow(false)
    val lastExchangeRateFetchTime = MutableStateFlow<Long?>(null)

    fun fetchExchangeRates(baseCurrency: String = currencyCode.value) {
        viewModelScope.launch {
            isLoadingExchangeRates.value = true
            val (rates, isLive) = com.example.data.currency.CurrencyApiService.fetchLatestRates(baseCurrency)
            conversionRates.value = rates
            isLiveExchangeRates.value = isLive
            lastExchangeRateFetchTime.value = System.currentTimeMillis()
            isLoadingExchangeRates.value = false
        }
    }

    // Unified Reactive Dashboard UI State Flow
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        combine(
            totalNetWorth,
            currentMonthExpenses,
            currentMonthIncome,
            totalMonthlyBudget,
            accounts
        ) { netWorth, expense, income, budget, accts ->
            listOf(netWorth, expense, income, budget, accts)
        },
        combine(
            transactions,
            isBalanceHidden,
            isAiParsing,
            pendingAiResult,
            currencySymbol
        ) { txs, hidden, parsing, aiResult, symbol ->
            listOf(txs, hidden, parsing, aiResult, symbol)
        },
        combine(
            languageCode,
            show80PercentAlert,
            show100PercentAlert,
            aiErrorMessage
        ) { lang, alert80, alert100, err ->
            listOf(lang, alert80, alert100, err)
        }
    ) { group1, group2, group3 ->
        @Suppress("UNCHECKED_CAST")
        DashboardUiState(
            totalNetWorth = group1[0] as Double,
            monthExpense = group1[1] as Double,
            monthIncome = group1[2] as Double,
            monthlyBudget = group1[3] as Double,
            accounts = group1[4] as List<AccountEntity>,
            recentTransactions = group2[0] as List<TransactionEntity>,
            isBalanceHidden = group2[1] as Boolean,
            isAiParsing = group2[2] as Boolean,
            pendingAiResult = group2[3] as ParsedAiResult?,
            currencySymbol = group2[4] as String,
            languageCode = group3[0] as String,
            show80PercentAlert = group3[1] as Boolean,
            show100PercentAlert = group3[2] as Boolean,
            errorMessage = group3[3] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        BudgetNotificationManager.createNotificationChannel(application)
        checkBudgetLimits()
    }

    private fun checkBudgetLimits() {
        viewModelScope.launch {
            combine(currentMonthExpenses, totalMonthlyBudget, currencySymbol, languageCode) { expense, budget, symbol, lang ->
                if (budget > 0) {
                    val ratio = expense / budget
                    if (ratio >= 1.0) {
                        show100PercentAlert.value = true
                        show80PercentAlert.value = false
                    } else if (ratio >= 0.8) {
                        show80PercentAlert.value = true
                        show100PercentAlert.value = false
                    } else {
                        show80PercentAlert.value = false
                        show100PercentAlert.value = false
                    }

                    BudgetNotificationManager.checkAndTriggerBudgetNotification(
                        context = getApplication(),
                        currentExpense = expense,
                        monthlyBudget = budget,
                        currencySymbol = symbol,
                        languageCode = lang
                    )
                }
            }.collect()
        }
    }

    fun setLanguage(code: String) {
        repository.preferences.languageCode = code
        languageCode.value = code
    }

    fun toggleQuickLanguage() {
        val newLang = if (languageCode.value == "bn") "en" else "bn"
        setLanguage(newLang)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            themeDataStore.setThemeMode(mode)
        }
    }

    fun setThemePreset(preset: String) {
        viewModelScope.launch {
            themeDataStore.setThemePreset(preset)
        }
    }

    fun setCurrency(code: String, symbol: String) {
        repository.preferences.currencyCode = code
        repository.preferences.currencySymbol = symbol
        currencyCode.value = code
        currencySymbol.value = symbol
    }

    fun toggleBalanceVisibility() {
        val newHidden = !isBalanceHidden.value
        repository.preferences.isBalanceHidden = newHidden
        isBalanceHidden.value = newHidden
    }

    fun login(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.isBlank()) return false
        val cleanUserId = email.trim()
        repository.preferences.isLoggedIn = true
        repository.preferences.loggedInUserId = cleanUserId
        isLoggedIn.value = true
        loggedInUserId.value = cleanUserId
        return true
    }

    fun logout() {
        repository.preferences.isLoggedIn = false
        repository.preferences.loggedInUserId = ""
        isLoggedIn.value = false
        loggedInUserId.value = ""
        isPinUnlocked.value = false
    }

    fun toggleLoginSecurity(enabled: Boolean) {
        repository.preferences.isLoginSecurityEnabled = enabled
        isLoginSecurityEnabled.value = enabled
        if (!enabled) {
            repository.preferences.isPinLockEnabled = false
            isPinLockEnabled.value = false
            isPinUnlocked.value = true
        } else {
            repository.preferences.isPinLockEnabled = true
            isPinLockEnabled.value = true
            if (repository.preferences.pinCode.isEmpty()) {
                repository.preferences.pinCode = "1234"
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        repository.preferences.isBiometricEnabled = enabled
        isBiometricEnabled.value = enabled
    }

    fun unlockWithBiometric() {
        isPinUnlocked.value = true
    }

    fun setPinCode(pin: String) {
        repository.preferences.pinCode = pin
        repository.preferences.isPinLockEnabled = true
        isPinLockEnabled.value = true
        isPinUnlocked.value = true
    }

    fun disablePinLock() {
        repository.preferences.isPinLockEnabled = false
        repository.preferences.pinCode = ""
        isPinLockEnabled.value = false
        isPinUnlocked.value = true
    }

    fun verifyPin(inputPin: String): Boolean {
        return if (repository.preferences.pinCode == inputPin) {
            isPinUnlocked.value = true
            true
        } else false
    }

    fun completeOnboarding(lang: String, currencyCode: String, currencySymbol: String) {
        setLanguage(lang)
        setCurrency(currencyCode, currencySymbol)
        repository.preferences.isOnboardingCompleted = true
        isOnboardingCompleted.value = true
    }

    fun setMonthlyBudget(budget: Double) {
        repository.preferences.totalMonthlyBudget = budget
        totalMonthlyBudget.value = budget
    }

    // AI Natural Language Parsing
    fun parseInputWithAi(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            isAiParsing.value = true
            aiErrorMessage.value = null
            try {
                val result = GeminiAiService.parseNaturalInput(input, languageCode.value)
                pendingAiResult.value = result
            } catch (e: Exception) {
                aiErrorMessage.value = "AI parsing failed: ${e.message}"
            } finally {
                isAiParsing.value = false
            }
        }
    }

    fun analyzeSpendingHabitsWithGemini() {
        viewModelScope.launch {
            isAnalyzingHabits.value = true
            try {
                val txs = transactions.value
                val income = currentMonthIncome.value
                val expense = currentMonthExpenses.value
                val budget = totalMonthlyBudget.value
                val symbol = currencySymbol.value
                val lang = languageCode.value

                val categoryBreakdown = txs.filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryName }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .entries.joinToString("\n") { "• ${it.key}: $symbol${it.value}" }

                val summaryText = if (categoryBreakdown.isBlank()) "No categorized expense history yet." else "Top Expense Categories:\n$categoryBreakdown"

                val insightResult = GeminiAiService.generateSpendingAnalysis(
                    transactionsSummaryText = summaryText,
                    totalIncome = income,
                    totalExpense = expense,
                    monthlyBudget = budget,
                    currencySymbol = symbol,
                    userLang = lang
                )

                aiSpendingInsight.value = insightResult
            } catch (e: Exception) {
                aiSpendingInsight.value = "Failed to analyze spending habits: ${e.message}"
            } finally {
                isAnalyzingHabits.value = false
            }
        }
    }

    fun confirmPendingAiTransaction(
        amount: Double,
        type: TransactionType,
        accountName: String,
        targetAccountName: String?,
        categoryName: String,
        note: String
    ) {
        viewModelScope.launch {
            val accountList = accounts.value
            val matchedAccount = accountList.find { it.name.lowercase().contains(accountName.lowercase()) }
                ?: accountList.firstOrNull() ?: return@launch

            val targetAccount = if (targetAccountName != null) {
                accountList.find { it.name.lowercase().contains(targetAccountName.lowercase()) }
            } else null

            val newTx = TransactionEntity(
                amount = amount,
                type = type,
                accountId = matchedAccount.id,
                targetAccountId = targetAccount?.id,
                categoryName = categoryName,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            repository.addTransaction(newTx)
            pendingAiResult.value = null
        }
    }

    fun clearPendingAi() {
        pendingAiResult.value = null
    }

    fun addManualTransaction(
        amount: Double,
        type: TransactionType,
        accountId: Long,
        targetAccountId: Long? = null,
        categoryName: String,
        note: String
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                amount = amount,
                type = type,
                accountId = accountId,
                targetAccountId = targetAccountId,
                categoryName = categoryName,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addAccount(name: String, type: AccountType, balance: Double, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(AccountEntity(name = name, type = type, balance = balance, colorHex = colorHex))
        }
    }

    fun addDebt(personName: String, phone: String, amount: Double, type: DebtType, dueDate: String, note: String) {
        viewModelScope.launch {
            repository.addDebt(DebtEntity(personName = personName, phoneNumber = phone, amount = amount, type = type, dueDate = dueDate, note = note))
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            val nextSettled = !debt.isSettled
            val nextRepaid = if (nextSettled) debt.amount else 0.0
            repository.updateDebt(debt.copy(isSettled = nextSettled, repaidAmount = nextRepaid))
        }
    }

    fun recordDebtRepayment(debt: DebtEntity, additionalRepayment: Double) {
        viewModelScope.launch {
            val effectiveCurrentRepaid = if (debt.isSettled) debt.amount else debt.repaidAmount
            val newRepaid = (effectiveCurrentRepaid + additionalRepayment).coerceAtMost(debt.amount)
            val isNowSettled = newRepaid >= debt.amount
            repository.updateDebt(debt.copy(repaidAmount = newRepaid, isSettled = isNowSettled))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Backup & Restore State
    val isExportingBackup = MutableStateFlow(false)
    val isRestoringBackup = MutableStateFlow(false)
    val backupOperationStatus = MutableStateFlow<String?>(null)
    val pendingRestoreResult = MutableStateFlow<com.example.data.backup.BackupParseResult?>(null)

    fun exportBackup(onExportComplete: (String) -> Unit) {
        viewModelScope.launch {
            isExportingBackup.value = true
            backupOperationStatus.value = null
            try {
                val payload = repository.exportBackupData()
                backupOperationStatus.value = if (languageCode.value == "bn") "ব্যাকআপ সফলভাবে তৈরি করা হয়েছে।" else "Backup generated successfully."
                onExportComplete(payload)
            } catch (e: Exception) {
                backupOperationStatus.value = if (languageCode.value == "bn") "ব্যাকআপ তৈরিতে ত্রুটি: ${e.message}" else "Failed to generate backup: ${e.message}"
            } finally {
                isExportingBackup.value = false
            }
        }
    }

    fun parseBackupForRestore(content: String) {
        viewModelScope.launch {
            backupOperationStatus.value = null
            val result = com.example.data.backup.BackupHelper.parseAndValidateBackup(content)
            if (result.isValid) {
                pendingRestoreResult.value = result
            } else {
                pendingRestoreResult.value = null
                backupOperationStatus.value = result.errorMessage
            }
        }
    }

    fun confirmRestoreBackup() {
        val result = pendingRestoreResult.value ?: return
        viewModelScope.launch {
            isRestoringBackup.value = true
            try {
                val success = repository.restoreBackupData(result)
                if (success) {
                    backupOperationStatus.value = if (languageCode.value == "bn") "ব্যাকআপ থেকে ডেটা সফলভাবে রিস্টোর হয়েছে!" else "Data restored successfully from backup!"
                } else {
                    backupOperationStatus.value = if (languageCode.value == "bn") "রিস্টোর ব্যর্থ হয়েছে।" else "Restore failed."
                }
            } catch (e: Exception) {
                backupOperationStatus.value = if (languageCode.value == "bn") "রিস্টোর ত্রুটি: ${e.message}" else "Restore error: ${e.message}"
            } finally {
                isRestoringBackup.value = false
                pendingRestoreResult.value = null
            }
        }
    }

    fun clearBackupStatus() {
        backupOperationStatus.value = null
        pendingRestoreResult.value = null
    }

    fun dismiss80PercentAlert() {
        show80PercentAlert.value = false
    }

    fun dismiss100PercentAlert() {
        show100PercentAlert.value = false
    }
}
