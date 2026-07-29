package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.ui.viewmodel.MainViewModel

private data class ThemePresetOption(
    val key: String,
    val nameEn: String,
    val nameBn: String,
    val primaryColor: Color,
    val bgColor: Color
)

private val themePresetOptions = listOf(
    ThemePresetOption("EMERALD_GREEN", "1. Emerald Green (Current)", "১. ইমারেল্ড গ্রিন (বর্তমান)", Color(0xFF00897B), Color(0xFFF7F9FB)),
    ThemePresetOption("DEEP_BLUE", "2. Deep Blue", "২. ডিপ ব্লু", Color(0xFF1565C0), Color(0xFFF0F4F8)),
    ThemePresetOption("MIDNIGHT_BLACK", "3. Midnight Black (Dark)", "৩. মিডনাইট ব্ল্যাক (ডার্ক)", Color(0xFF00E5FF), Color(0xFF05070A)),
    ThemePresetOption("ROYAL_PURPLE", "4. Royal Purple", "৪. রয়্যাল পার্পল", Color(0xFF7B1FA2), Color(0xFFF6F2FA)),
    ThemePresetOption("CLASSIC_TEAL", "5. Classic Teal", "৫. ক্লাসিক টিল", Color(0xFF00838F), Color(0xFFF0F7F7))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val currentLang by viewModel.languageCode.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isPinEnabled by viewModel.isPinLockEnabled.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()
    val isLoginSecurityEnabled by viewModel.isLoginSecurityEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentThemePreset by viewModel.themePreset.collectAsState()

    var showLangDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCurrencyConverterDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showThemePresetDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = LanguageManager.getString("settings", currentLang),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logged In User ID Card & Sign Out
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (currentLang == "bn") "লগইনকৃত ইউজার আইডি" else "Logged-in User ID",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (loggedInUserId.isNotEmpty()) loggedInUserId else "hmtanvir851@gmail.com",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("logged_in_user_id_text")
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.logout() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLang == "bn") "সাইন আউট" else "Sign Out",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PIN / Login Security Screen Toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (currentLang == "bn") "সিকিউরিটি স্ক্রিন / পিন লক" else "PIN / Login Security Screen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isLoginSecurityEnabled || isPinEnabled)
                                (if (currentLang == "bn") "অ্যাপ চালুর সময় সিকিউরিটি স্ক্রিন চালু" else "Security screen ON (App launch lock)")
                            else
                                (if (currentLang == "bn") "সিকিউরিটি স্ক্রিন বন্ধ" else "Security screen OFF (Direct dashboard)"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isLoginSecurityEnabled || isPinEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            viewModel.toggleLoginSecurity(true)
                            showPinSetupDialog = true
                        } else {
                            viewModel.toggleLoginSecurity(false)
                        }
                    },
                    modifier = Modifier.testTag("pin_security_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Biometric Security Toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Lock",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (currentLang == "bn") "বায়োমেট্রিক আনলক (ফিঙ্গারপ্রিন্ট / ফেস)" else "Biometric Unlock (Fingerprint / Face)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isBiometricEnabled)
                                (if (currentLang == "bn") "দ্রুত বায়োমেট্রিক আনলক সক্রিয়" else "Quick fingerprint / face verification active")
                            else
                                (if (currentLang == "bn") "বায়োমেট্রিক আনলক বন্ধ" else "Biometric verification disabled"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleBiometric(enabled)
                    },
                    modifier = Modifier.testTag("biometric_security_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Language Option
        SettingsTileItem(
            icon = Icons.Default.Translate,
            title = if (currentLang == "bn") "ভাষা পরিবর্তন করুন" else "Change Language",
            subtitle = LanguageManager.supportedLanguages.find { it.code == currentLang }?.nativeName ?: "English",
            onClick = { showLangDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Currency Option
        SettingsTileItem(
            icon = Icons.Default.Payments,
            title = if (currentLang == "bn") "গ্লোবাল কারেন্সি" else "Global Currency",
            subtitle = "$currencyCode ($currencySymbol)",
            onClick = { showCurrencyDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Currency Converter Utility Option
        SettingsTileItem(
            icon = Icons.Default.CurrencyExchange,
            title = if (currentLang == "bn") "লাইভ কারেন্সি কনভার্টার" else "Live Currency Converter",
            subtitle = if (currentLang == "bn") "রিয়েল-টাইম এক্সচেঞ্জ রেট এবং অটো কনভার্সন" else "Real-time exchange rates & currency converter",
            onClick = { showCurrencyConverterDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Backup & Restore Option
        SettingsTileItem(
            icon = Icons.Default.CloudSync,
            title = if (currentLang == "bn") "ম্যানুয়াল ক্লাউড ও লোকাল ব্যাকআপ" else "Manual Cloud & Local Backup",
            subtitle = if (currentLang == "bn") "ডেটা এক্সপোর্ট, সেভ এবং রিস্টোর সুবিধা" else "Export, backup & restore financial data",
            onClick = { showBackupDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Theme Mode Option (Light / Dark / System Default)
        val themeSubtitle = when (currentThemeMode) {
            "LIGHT" -> if (currentLang == "bn") "লাইট মোড" else "Light Theme"
            "DARK" -> if (currentLang == "bn") "ডার্ক মোড" else "Dark Theme"
            else -> if (currentLang == "bn") "সিস্টেম ডিফল্ট" else "System Default"
        }

        SettingsTileItem(
            icon = when (currentThemeMode) {
                "LIGHT" -> Icons.Default.LightMode
                "DARK" -> Icons.Default.DarkMode
                else -> Icons.Default.SettingsSuggest
            },
            title = if (currentLang == "bn") "অ্যাপ থিম / ডার্ক মোড" else "App Theme Mode",
            subtitle = themeSubtitle,
            onClick = { showThemeDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Color Theme Selection Option (5 Preset Colors)
        val presetSubtitle = themePresetOptions.find { it.key == currentThemePreset }?.let {
            if (currentLang == "bn") it.nameBn else it.nameEn
        } ?: "1. Emerald Green"

        SettingsTileItem(
            icon = Icons.Default.Palette,
            title = if (currentLang == "bn") "কালার থিম সিলেক্টর (৫টি প্রিসেট)" else "Theme Selection (5 Color Presets)",
            subtitle = presetSubtitle,
            onClick = { showThemePresetDialog = true }
        )
    }

    if (showThemePresetDialog) {
        AlertDialog(
            onDismissRequest = { showThemePresetDialog = false },
            title = {
                Text(
                    text = if (currentLang == "bn") "কালার থিম নির্বাচন করুন" else "Select Preset Color Theme",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themePresetOptions.forEach { option ->
                        val isSelected = (currentThemePreset == option.key)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemePreset(option.key)
                                    showThemePresetDialog = false
                                }
                                .testTag("theme_preset_${option.key}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setThemePreset(option.key)
                                            showThemePresetDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentLang == "bn") option.nameBn else option.nameEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(option.primaryColor, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(option.bgColor, CircleShape)
                                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemePresetDialog = false }) {
                    Text(if (currentLang == "bn") "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    if (showThemeDialog) {
        val themeOptions = listOf(
            "SYSTEM" to (if (currentLang == "bn") "সিস্টেম ডিফল্ট (System Default)" else "System Default"),
            "LIGHT" to (if (currentLang == "bn") "লাইট থিম (Light Mode)" else "Light Theme"),
            "DARK" to (if (currentLang == "bn") "ডার্ক থিম (Dark Mode)" else "Dark Theme")
        )

        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(if (currentLang == "bn") "অ্যাপ থিম নির্বাচন করুন" else "Select App Theme Mode") },
            text = {
                Column {
                    themeOptions.forEach { (modeKey, modeLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(modeKey)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (currentThemeMode == modeKey),
                                onClick = {
                                    viewModel.setThemeMode(modeKey)
                                    showThemeDialog = false
                                },
                                modifier = Modifier.testTag("theme_radio_$modeKey")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when (modeKey) {
                                    "LIGHT" -> Icons.Default.LightMode
                                    "DARK" -> Icons.Default.DarkMode
                                    else -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = modeLabel,
                                fontWeight = if (currentThemeMode == modeKey) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(if (currentLang == "bn") "বন্ধ করুন" else "Close") }
            }
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Select Language (15+ Supported)") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(LanguageManager.supportedLanguages) { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang.code)
                                    showLangDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.flagEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(lang.nativeName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLangDialog = false }) { Text("Close") }
            }
        )
    }

    if (showCurrencyConverterDialog) {
        com.example.ui.components.CurrencyConverterDialog(
            viewModel = viewModel,
            onDismiss = { showCurrencyConverterDialog = false }
        )
    }

    if (showBackupDialog) {
        com.example.ui.components.BackupDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Global Currency (30+)") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(CurrencyManager.supportedCurrencies) { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(curr.code, curr.symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${curr.code} (${curr.symbol}) - ${curr.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPinSetupDialog) {
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.length <= 4) newPinInput = it },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            viewModel.setPinCode(newPinInput)
                            showPinSetupDialog = false
                        }
                    }
                ) { Text("Set PIN") }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsTileItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
