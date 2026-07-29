package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyApiService
import com.example.data.localization.CurrencyItem
import com.example.data.localization.CurrencyManager
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.languageCode.collectAsState()
    val defaultCurrencyCode by viewModel.currencyCode.collectAsState()
    val ratesMap by viewModel.conversionRates.collectAsState()
    val isLiveRates by viewModel.isLiveExchangeRates.collectAsState()
    val isLoadingRates by viewModel.isLoadingExchangeRates.collectAsState()

    var amountInput by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf(CurrencyManager.supportedCurrencies.find { it.code == defaultCurrencyCode } ?: CurrencyManager.supportedCurrencies.first()) }
    var toCurrency by remember { mutableStateOf(CurrencyManager.supportedCurrencies.find { it.code == "USD" && it.code != defaultCurrencyCode } ?: CurrencyManager.supportedCurrencies[1]) }

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ratesMap.isEmpty()) {
            viewModel.fetchExchangeRates(fromCurrency.code)
        }
    }

    val amount = amountInput.toDoubleOrNull() ?: 0.0

    val convertedValue = remember(amount, fromCurrency, toCurrency, ratesMap) {
        if (ratesMap.isEmpty()) {
            // Fallback before initial fetch
            val fromRate = CurrencyApiService.fallbackRatesToUsd[fromCurrency.code] ?: 1.0
            val toRate = CurrencyApiService.fallbackRatesToUsd[toCurrency.code] ?: 1.0
            (amount / fromRate) * toRate
        } else {
            CurrencyApiService.convertAmount(amount, fromCurrency.code, toCurrency.code, ratesMap)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Currency Converter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn") "লাইভ কারেন্সি কনভার্টার" else "Live Currency Converter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Live / Offline Status Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLiveRates) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLiveRates) "🟢 " else "🟠 ",
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isLiveRates)
                                    (if (currentLang == "bn") "রিয়েল-টাইম এক্সচেঞ্জ রেট সক্রিয়" else "Live API Exchange Rates Active")
                                else
                                    (if (currentLang == "bn") "অফলাইন এক্সচেঞ্জ রেট ব্যবহৃত" else "Offline Fallback Rates"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLiveRates) Color(0xFF1B5E20) else Color(0xFFE65100)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.fetchExchangeRates(fromCurrency.code) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isLoadingRates) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Rates",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text(if (currentLang == "bn") "পরিমাণ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("converter_amount_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // From Currency Selector & To Currency Selector Row with Swap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // From Currency Tile
                    CurrencySelectBox(
                        label = if (currentLang == "bn") "হতে" else "From",
                        currencyItem = fromCurrency,
                        onClick = { showFromDropdown = true },
                        modifier = Modifier.weight(1f)
                    )

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = fromCurrency
                            fromCurrency = toCurrency
                            toCurrency = temp
                            viewModel.fetchExchangeRates(fromCurrency.code)
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("swap_currencies_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Currencies",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // To Currency Tile
                    CurrencySelectBox(
                        label = if (currentLang == "bn") "তে" else "To",
                        currencyItem = toCurrency,
                        onClick = { showToDropdown = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Converted Result Display Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentLang == "bn") "রূপান্তরিত পরিমাণ" else "Converted Amount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${toCurrency.flag} ${String.format("%.2f", convertedValue)} ${toCurrency.symbol}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "1 ${fromCurrency.code} = ${String.format("%.4f", if (amount > 0) convertedValue / amount else 0.0)} ${toCurrency.code}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to Set Selected Currency as Default App Currency
                Button(
                    onClick = {
                        viewModel.setCurrency(toCurrency.code, toCurrency.symbol)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_default_currency_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Set Default",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn")
                            "${toCurrency.code} ডিফল্ট কারেন্সি হিসেবে সেভ করুন"
                        else
                            "Set ${toCurrency.code} (${toCurrency.symbol}) as Default Currency",
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "bn") "বন্ধ করুন" else "Close")
            }
        }
    )

    // From Currency Dialog
    if (showFromDropdown) {
        CurrencySelectionListDialog(
            title = if (currentLang == "bn") "উৎসের কারেন্সি বেছে নিন" else "Select From Currency",
            onSelect = { selected ->
                fromCurrency = selected
                showFromDropdown = false
                viewModel.fetchExchangeRates(selected.code)
            },
            onDismiss = { showFromDropdown = false }
        )
    }

    // To Currency Dialog
    if (showToDropdown) {
        CurrencySelectionListDialog(
            title = if (currentLang == "bn") "লক্ষ্য কারেন্সি বেছে নিন" else "Select Target Currency",
            onSelect = { selected ->
                toCurrency = selected
                showToDropdown = false
            },
            onDismiss = { showToDropdown = false }
        )
    }
}

@Composable
private fun CurrencySelectBox(
    label: String,
    currencyItem: CurrencyItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${currencyItem.flag} ${currencyItem.code}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currencyItem.symbol,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CurrencySelectionListDialog(
    title: String,
    onSelect: (CurrencyItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CurrencyManager.supportedCurrencies.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = item.code, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = item.name, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Text(text = item.symbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
