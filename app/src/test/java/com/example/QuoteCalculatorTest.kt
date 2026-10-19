package com.example

import com.example.logic.QuoteCalculator
import com.example.model.AppThemeMode
import com.example.model.Currency
import com.example.model.PaymentMilestone
import com.example.model.QuoteInputState
import com.example.model.SupportedCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteCalculatorTest {

    private val inr = SupportedCurrencies[0]

    @Test
    fun testDefaultCalculations() {
        val input = QuoteInputState(
            estimatedHours = "20",
            hourlyLabourCost = "500",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "25",
            contingencyPercent = "0",
            currency = inr
        )

        val result = QuoteCalculator.calculate(input)

        assertTrue("Expected valid result", result.isValid)
        assertEquals("Labour cost = 20 * 500 = 10,000", 10000.0, result.labourCost, 0.01)
        assertEquals("Base cost = 10,000 + 2,000 + 1,000 = 13,000", 13000.0, result.baseCost, 0.01)
        assertEquals("Contingency = 0", 0.0, result.contingencyAmount, 0.01)
        assertEquals("Total cost = 13,000", 13000.0, result.totalEstimatedCost, 0.01)
        // Target quote = 13,000 / (1 - 0.25) = 13,000 / 0.75 = 17,333.33
        assertEquals("Recommended quote", 17333.33, result.targetQuote, 0.01)
        // Profit = 17,333.33 - 13,000 = 4,333.33
        assertEquals("Estimated profit", 4333.33, result.estimatedProfit, 0.01)
        // Margin = 4,333.33 / 17,333.33 * 100 = 25%
        assertEquals("Actual estimated margin", 25.0, result.actualEstimatedMargin, 0.01)
        // Effective hourly = 4,333.33 / 20 = 216.66
        assertEquals("Effective hourly earnings", 216.67, result.effectiveHourlyEarnings, 0.01)
        // Effective hourly rate = 17,333.33 / 20 = 866.67
        assertEquals("Effective hourly rate", 866.67, result.effectiveHourlyRate, 0.01)
    }

    @Test
    fun testZeroWorkHoursSafeHandling() {
        val input = QuoteInputState(
            estimatedHours = "0",
            hourlyLabourCost = "500",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "25",
            contingencyPercent = "0",
            currency = inr
        )
        val result = QuoteCalculator.calculate(input)
        assertTrue("Zero hours is non-negative and valid", result.isValid)
        assertEquals("Net profit per hour must safely be 0 for zero hours", 0.0, result.effectiveHourlyEarnings, 0.001)
        assertEquals("Effective hourly rate must safely be 0 for zero hours", 0.0, result.effectiveHourlyRate, 0.001)
    }

    @Test
    fun testWithContingencyBuffer() {
        val input = QuoteInputState(
            estimatedHours = "10",
            hourlyLabourCost = "1000",
            directExpenses = "5000",
            allocatedOverhead = "1000",
            targetMarginPercent = "20",
            contingencyPercent = "10",
            currency = inr
        )

        val result = QuoteCalculator.calculate(input)

        // Labour = 10,000, Base = 10,000 + 5,000 + 1,000 = 16,000
        assertEquals(16000.0, result.baseCost, 0.01)
        // Contingency = 16,000 * 10% = 1,600
        assertEquals(1600.0, result.contingencyAmount, 0.01)
        // Total cost = 17,600
        assertEquals(17600.0, result.totalEstimatedCost, 0.01)
        // Target quote = 17,600 / 0.8 = 22,000
        assertEquals(22000.0, result.targetQuote, 0.01)
        // Profit = 22,000 - 17,600 = 4,400
        assertEquals(4400.0, result.estimatedProfit, 0.01)
        // Margin = 4,400 / 22,000 = 20%
        assertEquals(20.0, result.actualEstimatedMargin, 0.01)
        // Effective hourly earnings = 4400 / 10 = 440
        assertEquals(440.0, result.effectiveHourlyEarnings, 0.01)
        // Effective hourly rate = 22000 / 10 = 2200
        assertEquals(2200.0, result.effectiveHourlyRate, 0.01)
    }

    @Test
    fun testPaymentMilestonesReconciliationWhen100Percent() {
        val input = QuoteInputState(
            estimatedHours = "10",
            hourlyLabourCost = "1000",
            directExpenses = "0",
            allocatedOverhead = "0",
            targetMarginPercent = "0",
            contingencyPercent = "0",
            currency = inr
        )
        val result = QuoteCalculator.calculate(input)
        assertEquals(10000.0, result.targetQuote, 0.01)

        val milestones = listOf(
            PaymentMilestone("1", "Deposit", 33.33),
            PaymentMilestone("2", "Beta", 33.33),
            PaymentMilestone("3", "Final", 33.34)
        )
        val totalPct = milestones.sumOf { it.percentage }
        assertEquals(100.0, totalPct, 0.001)

        // Reconciled simulation matching QuoteCalculator / UI
        val total = result.targetQuote
        var runningSum = 0.0
        val amounts = milestones.mapIndexed { idx, m ->
            if (idx == milestones.lastIndex) {
                (total - runningSum).coerceAtLeast(0.0)
            } else {
                val amt = total * (m.percentage / 100.0)
                runningSum += amt
                amt
            }
        }
        val sumAmounts = amounts.sum()
        assertEquals("Total reconciled milestone amounts must equal target quote", 10000.0, sumAmounts, 0.001)
    }

    @Test
    fun testPaymentMilestonesWhenUnderOrOver100Percent() {
        val totalQuote = 10000.0
        val underMilestones = listOf(
            PaymentMilestone("1", "Deposit", 40.0),
            PaymentMilestone("2", "Delivery", 30.0)
        )
        val underTotalPct = underMilestones.sumOf { it.percentage }
        assertEquals(70.0, underTotalPct, 0.001)

        // When not 100%, amounts are calculated without forcing last to absorb remainder
        val underAmounts = underMilestones.map { totalQuote * (it.percentage / 100.0) }
        assertEquals(4000.0, underAmounts[0], 0.01)
        assertEquals(3000.0, underAmounts[1], 0.01)
        assertEquals(7000.0, underAmounts.sum(), 0.01)

        val overMilestones = listOf(
            PaymentMilestone("1", "Deposit", 60.0),
            PaymentMilestone("2", "Delivery", 60.0)
        )
        val overAmounts = overMilestones.map { totalQuote * (it.percentage / 100.0) }
        assertEquals(6000.0, overAmounts[0], 0.01)
        assertEquals(6000.0, overAmounts[1], 0.01)
        assertEquals(12000.0, overAmounts.sum(), 0.01)
    }

    @Test
    fun testDynamicCurrencyFormattingAllCurrencies() {
        val amount = 10000.0
        SupportedCurrencies.forEach { curr ->
            val formatted = QuoteCalculator.formatCurrency(amount, curr)
            assertTrue("Formatted must contain currency symbol ${curr.symbol}", formatted.contains(curr.symbol))
            assertTrue("Formatted must contain 10,000", formatted.contains("10,000"))
        }
    }

    @Test
    fun testNegativeInputRejection() {
        val input = QuoteInputState(
            estimatedHours = "-5",
            hourlyLabourCost = "500",
            directExpenses = "1000",
            allocatedOverhead = "500",
            targetMarginPercent = "20"
        )
        val result = QuoteCalculator.calculate(input)
        assertFalse("Negative hours must be invalid", result.isValid)
    }

    @Test
    fun testMarginAboveOrEqual100Rejection() {
        val input100 = QuoteInputState(
            estimatedHours = "10",
            hourlyLabourCost = "500",
            targetMarginPercent = "100"
        )
        val result100 = QuoteCalculator.calculate(input100)
        assertFalse("100% margin must be rejected", result100.isValid)

        val input150 = QuoteInputState(
            estimatedHours = "10",
            hourlyLabourCost = "500",
            targetMarginPercent = "150"
        )
        val result150 = QuoteCalculator.calculate(input150)
        assertFalse("150% margin must be rejected", result150.isValid)
    }

    @Test
    fun testThemeModesAvailability() {
        val modes = AppThemeMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AppThemeMode.CLASSIC))
        assertTrue(modes.contains(AppThemeMode.MONOCHROME_LIGHT))
        assertTrue(modes.contains(AppThemeMode.MONOCHROME_DARK))
    }
}
