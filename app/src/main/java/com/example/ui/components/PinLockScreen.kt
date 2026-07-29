package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.LanguageManager

@Composable
fun PinLockScreen(
    currentLang: String,
    onPinEntered: (String) -> Boolean,
    onBiometricClick: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    var inputPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onBiometricClick?.invoke()
    }

    val handleNumClick = { num: String ->
        if (inputPin.length < 4) {
            inputPin += num
            isError = false
            if (inputPin.length == 4) {
                val success = onPinEntered(inputPin)
                if (!success) {
                    isError = true
                    inputPin = ""
                }
            }
        }
    }

    val handleBackspace = {
        if (inputPin.isNotEmpty()) {
            inputPin = inputPin.dropLast(1)
            isError = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = LanguageManager.getString("app_title", currentLang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isError) {
                    if (currentLang == "bn") "ভুল পিন কোড! আবার চেষ্টা করুন।" else "Incorrect PIN! Try again."
                } else {
                    if (currentLang == "bn") "৪ ডিজিটের সিকিউরিটি পিন বা বায়োমেট্রিক দিন" else "Enter 4-Digit Security PIN or Biometrics"
                },
                fontSize = 13.sp,
                color = if (isError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < inputPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Number Pad 1-9, 0, Backspace
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    row.forEach { key ->
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable {
                                    when (key) {
                                        "DEL" -> handleBackspace()
                                        "C" -> {
                                            inputPin = ""
                                            isError = false
                                        }
                                        else -> handleNumClick(key)
                                    }
                                }
                                .testTag("pin_key_$key"),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Biometric Fingerprint Trigger Button
            if (onBiometricClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onBiometricClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                        .testTag("biometric_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Unlock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn") "ফিঙ্গারপ্রিন্ট / ফেস আনলক" else "Use Fingerprint / Face",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onCancel) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        }
    }
}
