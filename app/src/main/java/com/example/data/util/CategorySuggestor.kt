package com.example.data.util

object CategorySuggestor {

    private val keywordMap = mapOf(
        "Food & Grocery" to listOf(
            "food", "grocery", "lunch", "dinner", "breakfast", "rice", "milk", "burger", "pizza",
            "restaurant", "cafe", "coffee", "tea", "bazaar", "vegetables", "fish", "meat", "fruits",
            "snack", "biryani", "kacchi", "noodle", "bread", "egg", "sweets", "khabar", "bazar",
            "খাবার", "বাজার", "ভাত", "দুধ", "বার্গার", "পিজা", "রেস্তোরাঁ", "কফি", "চা", "শাকসবজি",
            "মাছ", "মাংস", "ফল", "নাশতা", "কাঁচাবাজার", "ডিম", "মিষ্টি"
        ),
        "Transport" to listOf(
            "bus", "train", "taxi", "uber", "pathao", "cng", "rickshaw", "auto", "fuel", "petrol",
            "diesel", "octane", "parking", "toll", "fare", "flight", "ticket", "metro", "cab",
            "যাতায়াত", "বাস", "ট্রেন", "ট্যাক্সি", "উবার", "পাঠাও", "সিএনজি", "রিকশা", "জ্বালানি",
            "পেট্রোল", "অকটেন", "পার্কিং", "টোল", "ভাড়া", "টিকিট", "মেট্রোরেল"
        ),
        "Bills & Utilities" to listOf(
            "bill", "electricity", "water", "gas", "internet", "wifi", "desco", "dpdc", "wasa",
            "mobile", "recharge", "flexiload", "gpay", "bkash charge", "rent", "house rent", "utility",
            "বিল", "বিদ্যুৎ", "পানি", "গ্যাস", "ইন্টারনেট", "ওয়াইফাই", "মোবাইল", "রিচার্জ", "ফ্লেক্সিলোড",
            "ভাড়া", "বাসা ভাড়া", "কারেন্ট"
        ),
        "Shopping" to listOf(
            "shirt", "pant", "dress", "shoes", "clothes", "cloth", "amazon", "daraz", "electronics",
            "laptop", "phone", "watch", "bag", "cosmetics", "tshirt", "jacket", "jeans", "shopping",
            "শপিং", "জামা", "কাপড়", "জুতা", "পোশাক", "জামাকাপড়", "ঘড়ি", "ব্যাগ", "প্রসাধন"
        ),
        "Healthcare" to listOf(
            "doctor", "hospital", "medicine", "pharmacy", "clinic", "pharma", "test", "lab", "dental",
            "medical", "health", "syrup", "tablet", "dentist", "prescription",
            "ওষুধ", "ডাক্তার", "হাসপাতাল", "ফার্মেসি", "ক্লিনিক", "পরীক্ষা", "মেডিসিন", "ডেন্টাল"
        ),
        "Education" to listOf(
            "school", "college", "university", "tuition", "fee", "book", "books", "pen", "course",
            "exam", "coaching", "stationery", "tutor", "tuition fee",
            "শিক্ষা", "স্কুল", "কলেজ", "বিশ্ববিদ্যালয়", "টিউশন", "ফি", "বই", "খাতা", "কলম", "কোর্স", "পরীক্ষা"
        ),
        "Entertainment" to listOf(
            "movie", "cinema", "netflix", "spotify", "game", "park", "concert", "show", "tour",
            "travel", "resort", "hotel", "vacation", "trip", "amusement",
            "বিনোদন", "সিনেমা", "মুভি", "গেম", "পার্ক", "কনসার্ট", "ভ্রমণ", "রিসোর্ট", "ট্যুর"
        ),
        "Salary" to listOf(
            "salary", "wages", "stipend", "bonus", "payroll", "remuneration", "paycheck",
            "বেতন", "বোনাস", "স্টাইপেন্ড", "মাসিক বেতন"
        ),
        "Business" to listOf(
            "profit", "sales", "client", "customer", "project", "freelance", "vendor", "revenue",
            "ব্যবসা", "লাভ", "বিক্রি", "ক্লায়েন্ট", "প্রজেক্ট", "ফ্রিল্যান্সিং"
        ),
        "Investment" to listOf(
            "stock", "share", "crypto", "profit", "dividend", "interest", "dps", "fdr", "savings",
            "সঞ্চয়", "ডিপিএস", "এফডিআর", "শেয়ার", "শেয়ারবাজার", "বিনিয়োগ"
        )
    )

    fun suggestCategory(descriptionText: String): String? {
        if (descriptionText.isBlank()) return null
        val tokens = descriptionText.lowercase().split("\\s+|[.,;!?#/-]".toRegex())

        for ((category, keywords) in keywordMap) {
            for (keyword in keywords) {
                if (tokens.any { token -> token.contains(keyword) || keyword.contains(token) && token.length >= 3 }) {
                    return category
                }
            }
        }
        return null
    }
}
