package com.example.data.currency

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CurrencyApiService {

    private const val TAG = "CurrencyApiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Default static fallback rates against USD in case of network unavailability
    val fallbackRatesToUsd = mapOf(
        "USD" to 1.0,
        "BDT" to 121.5,
        "EUR" to 0.92,
        "GBP" to 0.78,
        "INR" to 83.8,
        "SAR" to 3.75,
        "AED" to 3.67,
        "MYR" to 4.42,
        "QAR" to 3.64,
        "CAD" to 1.38,
        "AUD" to 1.52,
        "SGD" to 1.34,
        "KWD" to 0.31,
        "OMR" to 0.38,
        "BHD" to 0.38,
        "JPY" to 154.2,
        "CNY" to 7.24,
        "RUB" to 97.5,
        "TRY" to 34.2,
        "IDR" to 15800.0,
        "THB" to 34.5,
        "PKR" to 278.5,
        "EGP" to 49.2,
        "ZAR" to 18.1,
        "KRW" to 1380.0,
        "BRL" to 5.72,
        "MXN" to 20.1,
        "CHF" to 0.88,
        "NZD" to 1.68,
        "LKR" to 293.0
    )

    suspend fun fetchLatestRates(baseCurrency: String): Pair<Map<String, Double>, Boolean> = withContext(Dispatchers.IO) {
        val url = "https://open.er-api.com/v6/latest/$baseCurrency"
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (response.isSuccessful && bodyString.isNotBlank()) {
                val json = JSONObject(bodyString)
                val result = json.optString("result")
                if (result == "success") {
                    val ratesObj = json.optJSONObject("rates")
                    if (ratesObj != null) {
                        val resultMap = mutableMapOf<String, Double>()
                        val keys = ratesObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            resultMap[key] = ratesObj.optDouble(key, 1.0)
                        }
                        return@withContext Pair(resultMap, true) // Live rates fetched
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching live exchange rates for $baseCurrency: ${e.message}")
        }

        // Fallback calculation if network call fails
        val baseUsdRate = fallbackRatesToUsd[baseCurrency] ?: 1.0
        val derivedRates = fallbackRatesToUsd.mapValues { (_, usdRate) ->
            usdRate / baseUsdRate
        }
        return@withContext Pair(derivedRates, false) // Offline/fallback rates
    }

    fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        ratesMap: Map<String, Double>
    ): Double {
        val fromRate = ratesMap[fromCurrency] ?: 1.0
        val toRate = ratesMap[toCurrency] ?: 1.0
        if (fromRate == 0.0) return 0.0
        // Rates in ratesMap are with respect to baseCurrency
        return (amount / fromRate) * toRate
    }
}
