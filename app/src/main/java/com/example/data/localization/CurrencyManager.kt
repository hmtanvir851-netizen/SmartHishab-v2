package com.example.data.localization

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

object CurrencyManager {
    val supportedCurrencies = listOf(
        CurrencyItem("BDT", "৳", "Bangladeshi Taka", "🇧🇩"),
        CurrencyItem("USD", "$", "US Dollar", "🇺🇸"),
        CurrencyItem("SAR", "SAR", "Saudi Riyal", "🇸🇦"),
        CurrencyItem("AED", "AED", "UAE Dirham", "🇦🇪"),
        CurrencyItem("INR", "₹", "Indian Rupee", "🇮🇳"),
        CurrencyItem("EUR", "€", "Euro", "🇪🇺"),
        CurrencyItem("GBP", "£", "British Pound", "🇬🇧"),
        CurrencyItem("MYR", "RM", "Malaysian Ringgit", "🇲🇾"),
        CurrencyItem("QAR", "QAR", "Qatari Riyal", "🇶🇦"),
        CurrencyItem("CAD", "C$", "Canadian Dollar", "🇨🇦"),
        CurrencyItem("AUD", "A$", "Australian Dollar", "🇦🇺"),
        CurrencyItem("SGD", "S$", "Singapore Dollar", "🇸🇬"),
        CurrencyItem("KWD", "KWD", "Kuwaiti Dinar", "🇰🇼"),
        CurrencyItem("OMR", "OMR", "Omani Rial", "🇴🇲"),
        CurrencyItem("BHD", "BHD", "Bahraini Dinar", "🇧🇭"),
        CurrencyItem("JPY", "¥", "Japanese Yen", "🇯🇵"),
        CurrencyItem("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        CurrencyItem("RUB", "₽", "Russian Ruble", "🇷🇺"),
        CurrencyItem("TRY", "₺", "Turkish Lira", "🇹🇷"),
        CurrencyItem("IDR", "Rp", "Indonesian Rupiah", "🇮🇩"),
        CurrencyItem("THB", "฿", "Thai Baht", "🇹🇭"),
        CurrencyItem("PKR", "Rs", "Pakistani Rupee", "🇵🇰"),
        CurrencyItem("EGP", "E£", "Egyptian Pound", "🇪🇬"),
        CurrencyItem("ZAR", "R", "South African Rand", "🇿🇦"),
        CurrencyItem("KRW", "₩", "South Korean Won", "🇰🇷"),
        CurrencyItem("BRL", "R$", "Brazilian Real", "🇧🇷"),
        CurrencyItem("MXN", "Mex$", "Mexican Peso", "🇲🇽"),
        CurrencyItem("CHF", "CHF", "Swiss Franc", "🇨🇭"),
        CurrencyItem("NZD", "NZ$", "New Zealand Dollar", "🇳🇿"),
        CurrencyItem("LKR", "Rs", "Sri Lankan Rupee", "🇱🇰")
    )

    fun format(amount: Double, symbol: String): String {
        return String.format("%.2f %s", amount, symbol)
    }

    fun formatCompact(amount: Double, symbol: String): String {
        return if (amount >= 1_000_000) {
            String.format("%.1fM %s", amount / 1_000_000, symbol)
        } else if (amount >= 1_000) {
            String.format("%.1fK %s", amount / 1_000, symbol)
        } else {
            String.format("%.0f %s", amount, symbol)
        }
    }
}
