package com.example

import com.example.logic.QuoteCalculator
import com.example.model.ClientQuoteDetails
import com.example.model.Currency
import com.example.model.PaymentMilestone
import com.example.model.QuoteInputState
import com.example.model.SupportedCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.round

class ExampleUnitTest {

    @Test
    fun testDefaultFairQuoteCalculation() {
        // Standard defaults: 20 hrs @ ₹500/hr, ₹2000 direct expenses, ₹1000 overhead, 25% margin, 0% contingency
        val inputs = QuoteInputState(
            projectName = "Standard Freelance Web",
            estimatedHours = "20",
            hourlyLabourCost = "500",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "25",
            contingencyPercent = "0",
            currency = SupportedCurrencies[0] // INR
        )

        val result = QuoteCalculator.calculate(inputs)

        assertTrue(result.isValid)
        assertTrue(result.errorMessages.isEmpty())

        // 1. labourCost = hours × hourlyRate = 20 × 500 = 10,000
        assertEquals(10000.0, result.labourCost, 0.01)

        // 2. baseCost = labourCost + directExpenses + allocatedOverhead = 10000 + 2000 + 1000 = 13,000
        assertEquals(13000.0, result.baseCost, 0.01)

        // 3. contingencyAmount = baseCost × 0 / 100 = 0
        assertEquals(0.0, result.contingencyAmount, 0.01)

        // 4. totalCost = baseCost + contingencyAmount = 13,000
        assertEquals(13000.0, result.totalEstimatedCost, 0.01)

        // 5. recommendedQuote = totalCost / (1 - targetMargin / 100) = 13,000 / 0.75 = 17,333.333
        assertEquals(17333.333, result.targetQuote, 0.01)

        // 6. estimatedProfit = recommendedQuote - totalCost = 17,333.333 - 13,000 = 4,333.333
        assertEquals(4333.333, result.estimatedProfit, 0.01)

        // 7. actualMargin = estimatedProfit / recommendedQuote × 100 = 4333.333 / 17333.333 × 100 = 25.0%
        assertEquals(25.0, result.actualEstimatedMargin, 0.01)

        // 8. breakEvenPrice = totalCost = 13,000
        assertEquals(13000.0, result.breakEvenPrice, 0.01)

        // 9. effectiveHourlyEarnings = estimatedProfit / hours = 4,333.333 / 20 = 216.666
        assertEquals(216.666, result.effectiveHourlyEarnings, 0.01)

        // Markup on cost: 4,333.333 / 13,000 × 100 = 33.333%
        assertEquals(33.333, result.markupOnCost, 0.01)
    }

    @Test
    fun testContingencyCalculation() {
        val inputs = QuoteInputState(
            projectName = "Complex Project",
            estimatedHours = "10",
            hourlyLabourCost = "1000",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "20",
            contingencyPercent = "10",
            currency = SupportedCurrencies[0]
        )

        val result = QuoteCalculator.calculate(inputs)

        assertTrue(result.isValid)
        // Labour: 10 × 1000 = 10,000
        assertEquals(10000.0, result.labourCost, 0.01)
        // Base: 10,000 + 2000 + 1000 = 13,000
        assertEquals(13000.0, result.baseCost, 0.01)
        // Contingency: 13,000 × 10% = 1,300
        assertEquals(1300.0, result.contingencyAmount, 0.01)
        // Total cost: 13,000 + 1,300 = 14,300
        assertEquals(14300.0, result.totalEstimatedCost, 0.01)
        // Quote: 14,300 / (1 - 0.20) = 17,875
        assertEquals(17875.0, result.targetQuote, 0.01)
        // Profit: 17,875 - 14,300 = 3,575
        assertEquals(3575.0, result.estimatedProfit, 0.01)
        // Actual margin: 3,575 / 17,875 = 20.0%
        assertEquals(20.0, result.actualEstimatedMargin, 0.01)
        // Markup: 3,575 / 14,300 = 25.0%
        assertEquals(25.0, result.markupOnCost, 0.01)
    }

    @Test
    fun testMultipleMargins() {
        // Total cost: 10h @ ₹1000 = 10,000
        val baseState = QuoteInputState(
            estimatedHours = "10",
            hourlyLabourCost = "1000",
            directExpenses = "0",
            allocatedOverhead = "0",
            contingencyPercent = "0"
        )

        // 0% Margin: Quote == TotalCost, Profit == 0
        val zeroMargin = QuoteCalculator.calculate(baseState.copy(targetMarginPercent = "0"))
        assertTrue(zeroMargin.isValid)
        assertEquals(10000.0, zeroMargin.targetQuote, 0.01)
        assertEquals(0.0, zeroMargin.estimatedProfit, 0.01)
        assertEquals(0.0, zeroMargin.actualEstimatedMargin, 0.01)

        // 15% Margin: Quote = 10,000 / 0.85 = 11,764.70
        val fifteenMargin = QuoteCalculator.calculate(baseState.copy(targetMarginPercent = "15"))
        assertTrue(fifteenMargin.isValid)
        assertEquals(11764.705, fifteenMargin.targetQuote, 0.01)
        assertEquals(15.0, fifteenMargin.actualEstimatedMargin, 0.01)

        // 50% Margin: Quote = 10,000 / 0.50 = 20,000, Profit = 10,000, Markup = 100%
        val fiftyMargin = QuoteCalculator.calculate(baseState.copy(targetMarginPercent = "50"))
        assertTrue(fiftyMargin.isValid)
        assertEquals(20000.0, fiftyMargin.targetQuote, 0.01)
        assertEquals(10000.0, fiftyMargin.estimatedProfit, 0.01)
        assertEquals(50.0, fiftyMargin.actualEstimatedMargin, 0.01)
        assertEquals(100.0, fiftyMargin.markupOnCost, 0.01)

        // 99% Margin: Quote = 10,000 / 0.01 = 1,000,000
        val ninetyNineMargin = QuoteCalculator.calculate(baseState.copy(targetMarginPercent = "99"))
        assertTrue(ninetyNineMargin.isValid)
        assertEquals(1000000.0, ninetyNineMargin.targetQuote, 0.01)
        assertEquals(990000.0, ninetyNineMargin.estimatedProfit, 0.01)
        assertEquals(99.0, ninetyNineMargin.actualEstimatedMargin, 0.01)
    }

    @Test
    fun testZeroHoursScenario() {
        // Zero hours but with direct expenses and overhead
        val inputs = QuoteInputState(
            estimatedHours = "0",
            hourlyLabourCost = "500",
            directExpenses = "5000",
            allocatedOverhead = "1000",
            targetMarginPercent = "20",
            contingencyPercent = "0"
        )

        val result = QuoteCalculator.calculate(inputs)

        assertTrue(result.isValid)
        assertEquals(0.0, result.labourCost, 0.01)
        assertEquals(6000.0, result.baseCost, 0.01)
        assertEquals(6000.0, result.totalEstimatedCost, 0.01)
        // Quote: 6000 / 0.8 = 7500
        assertEquals(7500.0, result.targetQuote, 0.01)
        assertEquals(1500.0, result.estimatedProfit, 0.01)
        // No division by zero for effective hourly earnings
        assertEquals(0.0, result.effectiveHourlyEarnings, 0.01)
    }

    @Test
    fun testZeroCostsScenario() {
        // All inputs zero
        val inputs = QuoteInputState(
            estimatedHours = "0",
            hourlyLabourCost = "0",
            directExpenses = "0",
            allocatedOverhead = "0",
            targetMarginPercent = "25",
            contingencyPercent = "0"
        )

        val result = QuoteCalculator.calculate(inputs)

        assertTrue(result.isValid)
        assertEquals(0.0, result.totalEstimatedCost, 0.01)
        assertEquals(0.0, result.targetQuote, 0.01)
        assertEquals(0.0, result.estimatedProfit, 0.01)
        assertEquals(0.0, result.actualEstimatedMargin, 0.01)
        assertEquals(0.0, result.effectiveHourlyEarnings, 0.01)
    }

    @Test
    fun testVeryLargeNumbers() {
        val inputs = QuoteInputState(
            estimatedHours = "1000",
            hourlyLabourCost = "50000",
            directExpenses = "5000000",
            allocatedOverhead = "2000000",
            targetMarginPercent = "30",
            contingencyPercent = "10"
        )

        val result = QuoteCalculator.calculate(inputs)

        assertTrue(result.isValid)
        assertFalse(result.targetQuote.isInfinite())
        assertFalse(result.targetQuote.isNaN())
        assertTrue(result.targetQuote > 0)
    }

    @Test
    fun testInputValidation() {
        // Empty fields
        val emptyHours = QuoteCalculator.calculate(QuoteInputState(estimatedHours = ""))
        assertFalse(emptyHours.isValid)
        assertTrue(emptyHours.errorMessages.any { it.contains("cannot be empty") })

        val emptyRate = QuoteCalculator.calculate(QuoteInputState(hourlyLabourCost = ""))
        assertFalse(emptyRate.isValid)

        // Malformed numbers
        val malformed = QuoteCalculator.calculate(QuoteInputState(estimatedHours = "abc"))
        assertFalse(malformed.isValid)
        assertTrue(malformed.errorMessages.any { it.contains("valid number") })

        val doubleDots = QuoteCalculator.calculate(QuoteInputState(directExpenses = "10..5"))
        assertFalse(doubleDots.isValid)

        // Negative numbers
        val negHours = QuoteCalculator.calculate(QuoteInputState(estimatedHours = "-1"))
        assertFalse(negHours.isValid)
        assertTrue(negHours.errorMessages.any { it.contains("negative") })

        val negExpenses = QuoteCalculator.calculate(QuoteInputState(directExpenses = "-500"))
        assertFalse(negExpenses.isValid)

        val negOverhead = QuoteCalculator.calculate(QuoteInputState(allocatedOverhead = "-200"))
        assertFalse(negOverhead.isValid)

        val negContingency = QuoteCalculator.calculate(QuoteInputState(contingencyPercent = "-5"))
        assertFalse(negContingency.isValid)

        val negMargin = QuoteCalculator.calculate(QuoteInputState(targetMarginPercent = "-1"))
        assertFalse(negMargin.isValid)

        // Contingency > 100%
        val highContingency = QuoteCalculator.calculate(QuoteInputState(contingencyPercent = "105"))
        assertFalse(highContingency.isValid)
        assertTrue(highContingency.errorMessages.any { it.contains("exceed 100%") })

        // Margin >= 100%
        val margin100 = QuoteCalculator.calculate(QuoteInputState(targetMarginPercent = "100"))
        assertFalse(margin100.isValid)
        assertTrue(margin100.errorMessages.any { it.contains("strictly less than 100%") })

        val margin110 = QuoteCalculator.calculate(QuoteInputState(targetMarginPercent = "110"))
        assertFalse(margin110.isValid)

        // Excessive hours
        val excessiveHours = QuoteCalculator.calculate(QuoteInputState(estimatedHours = "999999"))
        assertFalse(excessiveHours.isValid)
        assertTrue(excessiveHours.errorMessages.any { it.contains("unreasonably high") })
    }

    @Test
    fun testRoundingAndCurrencyFormatting() {
        val inr = SupportedCurrencies[0] // ₹
        val usd = SupportedCurrencies[1] // $
        val eur = SupportedCurrencies[2] // €

        // Whole numbers have no trailing .00
        assertEquals("₹15,000", QuoteCalculator.formatMoney(15000.0, inr))
        assertEquals("$2,500", QuoteCalculator.formatMoney(2500.0, usd))
        assertEquals("€1,000", QuoteCalculator.formatMoney(1000.0, eur))

        // Decimals formatted to 2 places
        assertEquals("₹17,333.33", QuoteCalculator.formatMoney(17333.33333, inr))
        assertEquals("$4,333.33", QuoteCalculator.formatMoney(4333.3333, usd))
        assertEquals("₹15,000.50", QuoteCalculator.formatMoney(15000.5, inr))

        // Negative or NaN fallback
        assertEquals("₹0", QuoteCalculator.formatMoney(Double.NaN, inr))
        assertEquals("₹0", QuoteCalculator.formatMoney(-50.0, inr))

        // Percent formatting
        assertEquals("25%", QuoteCalculator.formatPercent(25.0))
        assertEquals("33.3%", QuoteCalculator.formatPercent(33.3333))
        assertEquals("20%", QuoteCalculator.formatPercent(20.00001))
    }

    @Test
    fun testPaymentMilestonesRoundingAndBalance() {
        // Case 1: Total quote ₹17,333.33 with 3 milestones: 50%, 25%, 25% (total 100%)
        val totalQuote = 17333.33
        val milestones = listOf(
            PaymentMilestone("1", "Deposit", 50.0),
            PaymentMilestone("2", "Beta", 25.0),
            PaymentMilestone("3", "Final", 25.0)
        )

        val balanced = QuoteCalculator.calculateMilestones(totalQuote, milestones)

        assertEquals(3, balanced.size)
        // M1: 17333.33 * 0.50 = 8666.67
        assertEquals(8666.67, balanced[0].amount, 0.001)
        // M2: 17333.33 * 0.25 = 4333.33
        assertEquals(4333.33, balanced[1].amount, 0.001)
        // M3: must balance so sum exactly equals 17333.33!
        val sumAmounts = balanced.sumOf { it.amount }
        assertEquals(17333.33, sumAmounts, 0.001)

        // Case 2: Total quote ₹10,000.00 with 3 equal shares: 33.33%, 33.33%, 33.34% (total 100%)
        val milestones3 = listOf(
            PaymentMilestone("1", "M1", 33.33),
            PaymentMilestone("2", "M2", 33.33),
            PaymentMilestone("3", "M3", 33.34)
        )
        val balanced3 = QuoteCalculator.calculateMilestones(10000.0, milestones3)
        val sum3 = balanced3.sumOf { it.amount }
        assertEquals(10000.0, sum3, 0.001)

        // Case 3: Milestones not summing to 100% (e.g. 50% deposit only)
        val partialMilestones = listOf(
            PaymentMilestone("1", "Deposit", 50.0)
        )
        val partialBalanced = QuoteCalculator.calculateMilestones(10000.0, partialMilestones)
        assertEquals(5000.0, partialBalanced[0].amount, 0.001)
    }

    @Test
    fun testQuotationExportGenerators() {
        val inputs = QuoteInputState(
            projectName = "Mobile App Design",
            estimatedHours = "30",
            hourlyLabourCost = "800",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "25",
            contingencyPercent = "5",
            currency = SupportedCurrencies[0]
        )
        val result = QuoteCalculator.calculate(inputs)
        val details = ClientQuoteDetails(
            clientName = "Acme Corp",
            projectTitle = "Mobile App Design"
        )

        // Plain text quotation
        val text = QuoteCalculator.generateClientQuotationText(inputs, result, details)
        assertTrue(text.contains("Mobile App Design"))
        assertTrue(text.contains("Acme Corp"))
        assertTrue(text.contains("FINANCIAL SUMMARY"))
        assertTrue(text.contains("PAYMENT SCHEDULE & MILESTONES"))

        // HTML quotation for print/PDF
        val html = QuoteCalculator.generatePrintHtml(inputs, result, details)
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("FairQuote Lite"))
        assertTrue(html.contains("Acme Corp"))
        assertTrue(html.contains("@page"))
        assertTrue(html.contains("Scope of Work &amp; Deliverables"))
    }
}
