package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.data.security.BiometricAuthHelper
import com.example.data.localization.LanguageManager
import com.example.ui.components.BudgetAlertModal
import com.example.ui.components.OnboardingScreen
import com.example.ui.components.PinLockScreen
import com.example.ui.components.SmartTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.SmartHishabTheme
import com.example.ui.viewmodel.MainViewModel

enum class NavScreen(val titleKey: String, val icon: ImageVector, val tag: String) {
    HOME("app_title", Icons.Default.Home, "nav_home"),
    AI_ASSISTANT("ai_input_title", Icons.Default.AutoAwesome, "nav_ai"),
    ANALYTICS("analytics", Icons.Default.PieChart, "nav_analytics"),
    ACCOUNTS("accounts", Icons.Default.AccountBalanceWallet, "nav_accounts"),
    DEBTS("debt_tracker_title", Icons.Default.ReceiptLong, "nav_debts"),
    SETTINGS("settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val themePreset by viewModel.themePreset.collectAsState()

            SmartHishabTheme(
                themeMode = themeMode,
                themePreset = themePreset
            ) {
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val isLoginSecurityEnabled by viewModel.isLoginSecurityEnabled.collectAsState()
                val isPinEnabled by viewModel.isPinLockEnabled.collectAsState()
                val isPinUnlocked by viewModel.isPinUnlocked.collectAsState()
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
                val currentLang by viewModel.languageCode.collectAsState()
                val currencySymbol by viewModel.currencySymbol.collectAsState()

                val show80Alert by viewModel.show80PercentAlert.collectAsState()
                val show100Alert by viewModel.show100PercentAlert.collectAsState()
                val monthExpense by viewModel.currentMonthExpenses.collectAsState()
                val monthlyBudget by viewModel.totalMonthlyBudget.collectAsState()

                var currentScreen by remember { mutableStateOf(NavScreen.HOME) }

                fun triggerBiometricAuth() {
                    if (BiometricAuthHelper.isBiometricAvailable(this@MainActivity)) {
                        BiometricAuthHelper.promptBiometric(
                            activity = this@MainActivity,
                            title = if (currentLang == "bn") "স্মার্ট-হিসাব আনলক করুন" else "Unlock SmartHishab",
                            subtitle = if (currentLang == "bn") "ফিঙ্গারপ্রিন্ট বা ফেস আইডি যাচাই করুন" else "Verify using Fingerprint or Face ID",
                            negativeButtonText = if (currentLang == "bn") "পিন কোড" else "Use PIN",
                            onSuccess = {
                                viewModel.unlockWithBiometric()
                            },
                            onError = { _ -> }
                        )
                    }
                }

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = { lang, currencyCode, currencySym ->
                            viewModel.completeOnboarding(lang, currencyCode, currencySym)
                        }
                    )
                } else if (!isLoggedIn) {
                    SignInScreen(
                        currentLang = currentLang,
                        onSignInSuccess = { email, password ->
                            viewModel.login(email, password)
                        }
                    )
                } else if ((isLoginSecurityEnabled || isPinEnabled) && !isPinUnlocked) {
                    PinLockScreen(
                        currentLang = currentLang,
                        onPinEntered = { inputPin ->
                            viewModel.verifyPin(inputPin)
                        },
                        onBiometricClick = if (isBiometricEnabled && BiometricAuthHelper.isBiometricAvailable(this@MainActivity)) {
                            { triggerBiometricAuth() }
                        } else null
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            SmartTopAppBar(
                                currentLang = currentLang,
                                currencySymbol = currencySymbol,
                                isPinEnabled = isPinEnabled,
                                isPinUnlocked = isPinUnlocked,
                                onQuickToggleLang = { viewModel.toggleQuickLanguage() },
                                onLockClick = { viewModel.isPinUnlocked.value = false },
                                onCurrencyClick = { currentScreen = NavScreen.SETTINGS }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                NavScreen.values().forEach { screen ->
                                    NavigationBarItem(
                                        selected = currentScreen == screen,
                                        onClick = { currentScreen = screen },
                                        icon = { Icon(screen.icon, contentDescription = screen.name) },
                                        label = {
                                            Text(
                                                text = when (screen) {
                                                    NavScreen.HOME -> if (currentLang == "bn") "হোম" else "Home"
                                                    NavScreen.AI_ASSISTANT -> if (currentLang == "bn") "এআই" else "AI"
                                                    NavScreen.ANALYTICS -> if (currentLang == "bn") "চার্ট" else "Charts"
                                                    NavScreen.ACCOUNTS -> if (currentLang == "bn") "ওয়ালেট" else "Wallets"
                                                    NavScreen.DEBTS -> if (currentLang == "bn") "দেনা-পাওনা" else "Debts"
                                                    NavScreen.SETTINGS -> if (currentLang == "bn") "সেটিংস" else "Settings"
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.testTag(screen.tag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                NavScreen.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToAi = { currentScreen = NavScreen.AI_ASSISTANT },
                                    onNavigateToAccounts = { currentScreen = NavScreen.ACCOUNTS },
                                    onNavigateToAnalytics = { currentScreen = NavScreen.ANALYTICS }
                                )
                                NavScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                                NavScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                                NavScreen.ACCOUNTS -> AccountsScreen(viewModel = viewModel)
                                NavScreen.DEBTS -> DebtTrackerScreen(viewModel = viewModel)
                                NavScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }

                            // 80% or 100% Budget Warning Popup Alert Dialog
                            if (show100Alert) {
                                BudgetAlertModal(
                                    is100Percent = true,
                                    currentExpense = monthExpense,
                                    monthlyBudget = monthlyBudget,
                                    currencySymbol = currencySymbol,
                                    currentLang = currentLang,
                                    onDismiss = { viewModel.dismiss100PercentAlert() }
                                )
                            } else if (show80Alert) {
                                BudgetAlertModal(
                                    is100Percent = false,
                                    currentExpense = monthExpense,
                                    monthlyBudget = monthlyBudget,
                                    currencySymbol = currencySymbol,
                                    currentLang = currentLang,
                                    onDismiss = { viewModel.dismiss80PercentAlert() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
