package com.example.data.util

enum class ReminderLanguageOption {
    BANGLA,
    ENGLISH,
    COMBINED
}

object PaymentReminderHelper {

    fun generateReminderText(
        contactName: String,
        dueDate: String,
        amountText: String,
        userName: String,
        option: ReminderLanguageOption = ReminderLanguageOption.BANGLA
    ): String {
        val nameDisplay = contactName.ifBlank { "সুধী" }
        val dateDisplay = dueDate.ifBlank { "আজকের" }
        val userDisplay = userName.ifBlank { "Smart Hishab User" }

        val banglaTemplate = "প্রিয় $nameDisplay, আপনাকে $dateDisplay তারিখে প্রদত্ত ৳$amountText টাকা পরিশোধের জন্য বিনম্র অনুরোধ করা হচ্ছে। - $userDisplay"
        val englishTemplate = "Dear $nameDisplay, You are kindly requested to repay the amount of ৳$amountText provided on $dateDisplay. - $userDisplay"

        return when (option) {
            ReminderLanguageOption.BANGLA -> banglaTemplate
            ReminderLanguageOption.ENGLISH -> englishTemplate
            ReminderLanguageOption.COMBINED -> "$banglaTemplate\n\n$englishTemplate"
        }
    }

    // Overload method for backward compatibility
    fun generateReminderText(
        option: ReminderLanguageOption,
        amountText: String
    ): String {
        return generateReminderText("সুধী", "আজকের", amountText, "Smart Hishab User", option)
    }
}
