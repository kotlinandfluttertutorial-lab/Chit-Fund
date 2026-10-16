package com.chitsmart.utils

/**
 * Currency utilities for ChitSmart.
 *
 * amountToIndianWords converts a numeric amount to the Indian number-word system,
 * supporting units up to crores.
 */
object CurrencyUtils {
    private val ones =
        listOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen",
        )

    private val tens =
        listOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety",
        )

    /**
     * Converts a non-negative integer amount (in rupees) to Indian words.
     *
     * Examples:
     *   7550   → "Seven Thousand Five Hundred Fifty Rupees Only"
     *   125000 → "One Lakh Twenty Five Thousand Rupees Only"
     *   1000000 → "Ten Lakhs Rupees Only"
     *   10000000 → "One Crore Rupees Only"
     */
    fun amountToIndianWords(amount: Long): String {
        if (amount == 0L) return "Zero Rupees Only"
        if (amount < 0L) return "Negative amount"

        val crores = amount / 1_00_00_000L
        val remainder1 = amount % 1_00_00_000L
        val lakhs = remainder1 / 1_00_000L
        val remainder2 = remainder1 % 1_00_000L
        val thousands = remainder2 / 1_000L
        val remainder3 = remainder2 % 1_000L
        val hundreds = remainder3 / 100L
        val remainder4 = remainder3 % 100L

        val parts = mutableListOf<String>()

        if (crores > 0) parts.add("${convertBelowHundred(crores)} ${if (crores == 1L) "Crore" else "Crores"}")
        if (lakhs > 0) parts.add("${convertBelowHundred(lakhs)} ${if (lakhs == 1L) "Lakh" else "Lakhs"}")
        if (thousands > 0) parts.add("${convertBelowHundred(thousands)} Thousand")
        if (hundreds > 0) parts.add("${ones[hundreds.toInt()]} Hundred")
        if (remainder4 > 0) parts.add(convertBelowHundred(remainder4))

        return "${parts.joinToString(" ")} Rupees Only"
    }

    /** Overload for Double — truncates paise, operates on whole rupees. */
    fun amountToIndianWords(amount: Double): String = amountToIndianWords(amount.toLong())

    /** Overload for Int. */
    fun amountToIndianWords(amount: Int): String = amountToIndianWords(amount.toLong())

    private fun convertBelowHundred(n: Long): String {
        return when {
            n < 20 -> ones[n.toInt()]
            n % 10 == 0L -> tens[(n / 10).toInt()]
            else -> "${tens[(n / 10).toInt()]} ${ones[(n % 10).toInt()]}"
        }
    }

    /**
     * Formats an amount as Indian currency string.
     * E.g. 125000 → "₹1,25,000"
     */
    fun formatIndianCurrency(amount: Double): String {
        val longAmount = amount.toLong()
        val formatted =
            buildString {
                val str = longAmount.toString()
                val len = str.length
                when {
                    len <= 3 -> append(str)
                    len == 4 -> {
                        append(str[0])
                        append(',')
                        append(str.substring(1))
                    }
                    len == 5 -> {
                        append(str.substring(0, 2))
                        append(',')
                        append(str.substring(2))
                    }
                    len == 6 -> {
                        append(str.substring(0, 3))
                        append(',')
                        append(str.substring(3))
                    }
                    len == 7 -> {
                        append(str[0])
                        append(',')
                        append(str.substring(1, 3))
                        append(',')
                        append(str.substring(3))
                    }
                    len == 8 -> {
                        append(str.substring(0, 2))
                        append(',')
                        append(str.substring(2, 4))
                        append(',')
                        append(str.substring(4))
                    }
                    len == 9 -> {
                        append(str.substring(0, 3))
                        append(',')
                        append(str.substring(3, 5))
                        append(',')
                        append(str.substring(5))
                    }
                    else -> append(str) // fallback for very large amounts
                }
            }
        return "₹$formatted"
    }
}
