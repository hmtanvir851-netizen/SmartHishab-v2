package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smarthishab_prefs", Context.MODE_PRIVATE)

    var languageCode: String
        get() = prefs.getString("lang_code", "bn") ?: "bn"
        set(value) = prefs.edit().putString("lang_code", value).apply()

    var currencyCode: String
        get() = prefs.getString("currency_code", "BDT") ?: "BDT"
        set(value) = prefs.edit().putString("currency_code", value).apply()

    var currencySymbol: String
        get() = prefs.getString("currency_symbol", "৳") ?: "৳"
        set(value) = prefs.edit().putString("currency_symbol", value).apply()

    var isPinLockEnabled: Boolean
        get() = prefs.getBoolean("pin_lock_enabled", false)
        set(value) = prefs.edit().putBoolean("pin_lock_enabled", value).apply()

    var pinCode: String
        get() = prefs.getString("pin_code", "") ?: ""
        set(value) = prefs.edit().putString("pin_code", value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) = prefs.edit().putBoolean("onboarding_completed", value).apply()

    var totalMonthlyBudget: Double
        get() = prefs.getFloat("monthly_budget", 30000f).toDouble()
        set(value) = prefs.edit().putFloat("monthly_budget", value.toFloat()).apply()

    var isBalanceHidden: Boolean
        get() = prefs.getBoolean("balance_hidden", false)
        set(value) = prefs.edit().putBoolean("balance_hidden", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var loggedInUserId: String
        get() = prefs.getString("logged_in_user_id", "") ?: ""
        set(value) = prefs.edit().putString("logged_in_user_id", value).apply()

    var isLoginSecurityEnabled: Boolean
        get() = prefs.getBoolean("login_security_enabled", true)
        set(value) = prefs.edit().putBoolean("login_security_enabled", value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", true)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()
}
