package com.chitsmart.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {
    @Test
    fun `amountToIndianWords handles zero`() {
        assertEquals("Zero Rupees Only", CurrencyUtils.amountToIndianWords(0L))
        assertEquals("Zero Rupees Only", CurrencyUtils.amountToIndianWords(0))
        assertEquals("Zero Rupees Only", CurrencyUtils.amountToIndianWords(0.0))
    }

    @Test
    fun `amountToIndianWords handles negative amounts`() {
        assertEquals("Negative amount", CurrencyUtils.amountToIndianWords(-100L))
    }

    @Test
    fun `amountToIndianWords handles thousands and hundreds`() {
        assertEquals("Seven Thousand Five Hundred Fifty Rupees Only", CurrencyUtils.amountToIndianWords(7550L))
        assertEquals("One Thousand Rupees Only", CurrencyUtils.amountToIndianWords(1000L))
    }

    @Test
    fun `amountToIndianWords handles lakhs and crores`() {
        assertEquals("One Lakh Twenty Five Thousand Rupees Only", CurrencyUtils.amountToIndianWords(125000L))
        assertEquals("Ten Lakhs Rupees Only", CurrencyUtils.amountToIndianWords(1000000L))
        assertEquals("One Crore Rupees Only", CurrencyUtils.amountToIndianWords(10000000L))
    }

    @Test
    fun `formatIndianCurrency formats numbers correctly`() {
        assertEquals("₹1,000", CurrencyUtils.formatIndianCurrency(1000.0))
        assertEquals("₹125,000", CurrencyUtils.formatIndianCurrency(125000.0))
        assertEquals("₹10,00,0000", CurrencyUtils.formatIndianCurrency(10000000.0))
        assertEquals("₹500", CurrencyUtils.formatIndianCurrency(500.0))
    }
}
