package com.example.data.localization

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
)

object LanguageManager {
    val supportedLanguages = listOf(
        LanguageItem("bn", "বাংলা", "Bengali", "🇧🇩"),
        LanguageItem("en", "English", "English", "🇺🇸"),
        LanguageItem("ar", "العربية", "Arabic", "🇸🇦"),
        LanguageItem("hi", "हिन्दी", "Hindi", "🇮🇳"),
        LanguageItem("es", "Español", "Spanish", "🇪🇸"),
        LanguageItem("fr", "Français", "French", "🇫🇷"),
        LanguageItem("de", "Deutsch", "German", "🇩🇪"),
        LanguageItem("zh", "中文", "Chinese", "🇨🇳"),
        LanguageItem("ja", "日本語", "Japanese", "🇯🇵"),
        LanguageItem("pt", "Português", "Portuguese", "🇧🇷"),
        LanguageItem("ur", "اردو", "Urdu", "🇵🇰"),
        LanguageItem("tr", "Türkçe", "Turkish", "🇹🇷"),
        LanguageItem("ru", "Русский", "Russian", "🇷🇺"),
        LanguageItem("id", "Bahasa Indonesia", "Indonesian", "🇮🇩"),
        LanguageItem("it", "Italiano", "Italian", "🇮🇹")
    )

    private val translations = mapOf(
        "app_title" to mapOf(
            "bn" to "স্মার্ট-হিসাব", "en" to "SMART HISHAB", "ar" to "الحساب الذكي",
            "hi" to "स्मार्ट हिसाब", "es" to "SmartHishab", "fr" to "SmartHishab"
        ),
        "total_net_worth" to mapOf(
            "bn" to "মোট নেট ওয়ার্থ (সম্পদ)", "en" to "Total Net Worth", "ar" to "صافي الثروة الإجمالية",
            "hi" to "कुल शुद्ध मूल्य", "es" to "Patrimonio Neto Total", "fr" to "Valeur Nette Totale"
        ),
        "this_month_expense" to mapOf(
            "bn" to "চলতি মাসের খরচ", "en" to "This Month Expense", "ar" to "مصاريف هذا الشهر",
            "hi" to "इस महीने का खर्च", "es" to "Gasto de este mes", "fr" to "Dépenses du mois"
        ),
        "this_month_income" to mapOf(
            "bn" to "চলতি মাসের আয়", "en" to "This Month Income", "ar" to "دخل هذا الشهر",
            "hi" to "इस महीने की आय", "es" to "Ingresos de este mes", "fr" to "Revenus du mois"
        ),
        "accounts" to mapOf(
            "bn" to "অ্যাসেট ও ওয়ালেট", "en" to "Assets & Wallets", "ar" to "الحسابات والمحافظ",
            "hi" to "खाते और वॉलेट", "es" to "Cuentas y Monederos", "fr" to "Comptes & Portefeuilles"
        ),
        "ai_input_title" to mapOf(
            "bn" to "স্মার্ট এআই ভয়েস/টেক্সট ইনপুট", "en" to "AI Smart Input Engine", "ar" to "إدخال الذكاء الاصطناعي الذكي",
            "hi" to "एआई स्मार्ट इनपुट", "es" to "Entrada Inteligente IA", "fr" to "Saisie IA Intelligente"
        ),
        "ai_prompt_hint" to mapOf(
            "bn" to "বলুন বা লিখুন: 'আজকে ৫০০ টাকার গ্রোসারি কিনলাম বিকাশ থেকে'...",
            "en" to "Speak or type: 'Spent 500 on groceries using Bkash'...",
            "ar" to "تحدث أو اكتب: 'أنفقت 500 على البقالة من خلال الحساب'...",
            "hi" to "बोलें या लिखें: 'विकास से 500 रुपये की ग्रोसरी खरीदी'..."
        ),
        "budget_warnings" to mapOf(
            "bn" to "বাজেট সতর্কতা ও লিমিট", "en" to "Budget Alert & Limits", "ar" to "تنبيه الميزانية",
            "hi" to "बजट अलर्ट", "es" to "Alerta de Presupuesto", "fr" to "Alerte Budget"
        ),
        "debt_tracker_title" to mapOf(
            "bn" to "দেনা-পাওনা খাতা", "en" to "Debt & Loan Tracker", "ar" to "سجل الديون والقروض",
            "hi" to "देनदारी और लेनदारी", "es" to "Registro de Deudas", "fr" to "Gestion des Dettes"
        ),
        "i_lent" to mapOf(
            "bn" to "পাওনা (I Lent)", "en" to "Receivable (I Lent)", "ar" to "مستحقات لي",
            "hi" to "प्राप्य (मैंने दिया)", "es" to "Por Cobrar", "fr" to "À Recouvrer"
        ),
        "i_borrowed" to mapOf(
            "bn" to "দেনা (I Borrowed)", "en" to "Payable (I Borrowed)", "ar" to "ديون علي",
            "hi" to "देय (मैंने लिया)", "es" to "Por Pagar", "fr" to "À Payer"
        ),
        "analytics" to mapOf(
            "bn" to "পাই চার্ট ও অ্যানালিটিক্স", "en" to "Charts & Analytics", "ar" to "التحليلات والرسوم البيانية",
            "hi" to "विश्लेषण और चार्ट", "es" to "Análisis y Gráficos", "fr" to "Analyses & Graphiques"
        ),
        "pin_security" to mapOf(
            "bn" to "পিন লক সিকিউরিটি", "en" to "PIN Security", "ar" to "حماية برمز PIN",
            "hi" to "पिन सुरक्षा", "es" to "Seguridad PIN", "fr" to "Sécurité PIN"
        ),
        "internal_transfer" to mapOf(
            "bn" to "ইন্টারনাল ফান্ড ট্রান্সফার", "en" to "Internal Fund Transfer", "ar" to "تحويل داخلي",
            "hi" to "आंतरिक ट्रांसफर", "es" to "Transferencia Interna", "fr" to "Transfert Interne"
        ),
        "settings" to mapOf(
            "bn" to "সেটিংস ও সেটআপ", "en" to "Settings & Preferences", "ar" to "الإعدادات",
            "hi" to "सेटिंग्स", "es" to "Ajustes", "fr" to "Paramètres"
        )
    )

    fun getString(key: String, langCode: String): String {
        val keyMap = translations[key] ?: return key
        return keyMap[langCode] ?: keyMap["bn"] ?: keyMap["en"] ?: key
    }
}
