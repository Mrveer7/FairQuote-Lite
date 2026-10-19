package com.example.logic

import com.example.model.ClientQuoteDetails
import com.example.model.Currency
import com.example.model.PaymentMilestone
import com.example.model.QuoteCalculationResult
import com.example.model.QuoteInputState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

object QuoteCalculator {

    fun calculate(inputs: QuoteInputState): QuoteCalculationResult {
        val errors = mutableListOf<String>()

        val hours = inputs.estimatedHours.toDoubleOrNull()
        if (inputs.estimatedHours.isBlank()) {
            errors.add("Estimated hours cannot be empty.")
        } else if (hours == null) {
            errors.add("Estimated hours must be a valid number.")
        } else if (hours < 0) {
            errors.add("Estimated hours cannot be negative.")
        } else if (hours > 10000) {
            errors.add("Estimated hours seems unreasonably high (max 10,000).")
        }

        val rate = inputs.hourlyLabourCost.toDoubleOrNull()
        if (inputs.hourlyLabourCost.isBlank()) {
            errors.add("Hourly labour cost cannot be empty.")
        } else if (rate == null) {
            errors.add("Hourly labour cost must be a valid number.")
        } else if (rate < 0) {
            errors.add("Hourly labour cost cannot be negative.")
        } else if (rate > 1000000) {
            errors.add("Hourly labour cost seems unreasonably high.")
        }

        val expenses = inputs.directExpenses.toDoubleOrNull()
        if (inputs.directExpenses.isBlank()) {
            errors.add("Direct expenses cannot be empty.")
        } else if (expenses == null) {
            errors.add("Direct expenses must be a valid number.")
        } else if (expenses < 0) {
            errors.add("Direct expenses cannot be negative.")
        } else if (expenses > 50000000) {
            errors.add("Direct expenses exceeds reasonable limit.")
        }

        val overhead = inputs.allocatedOverhead.toDoubleOrNull()
        if (inputs.allocatedOverhead.isBlank()) {
            errors.add("Allocated overhead cannot be empty.")
        } else if (overhead == null) {
            errors.add("Allocated overhead must be a valid number.")
        } else if (overhead < 0) {
            errors.add("Allocated overhead cannot be negative.")
        } else if (overhead > 50000000) {
            errors.add("Allocated overhead exceeds reasonable limit.")
        }

        val margin = inputs.targetMarginPercent.toDoubleOrNull()
        if (inputs.targetMarginPercent.isBlank()) {
            errors.add("Target profit margin cannot be empty.")
        } else if (margin == null) {
            errors.add("Target profit margin must be a valid number.")
        } else if (margin < 0) {
            errors.add("Target margin cannot be negative.")
        } else if (margin >= 100) {
            errors.add("Target margin must be strictly less than 100%. (100% margin implies infinite quote).")
        }

        val contingency = inputs.contingencyPercent.toDoubleOrNull()
        if (inputs.contingencyPercent.isBlank()) {
            errors.add("Contingency percentage cannot be empty.")
        } else if (contingency == null) {
            errors.add("Contingency percentage must be a valid number.")
        } else if (contingency < 0) {
            errors.add("Contingency cannot be negative.")
        } else if (contingency > 100) {
            errors.add("Contingency buffer cannot exceed 100%.")
        }

        val validHours = (hours ?: 0.0).coerceAtLeast(0.0)
        val validRate = (rate ?: 0.0).coerceAtLeast(0.0)
        val validExpenses = (expenses ?: 0.0).coerceAtLeast(0.0)
        val validOverhead = (overhead ?: 0.0).coerceAtLeast(0.0)
        val validMargin = (margin ?: 0.0).coerceIn(0.0, 99.99)
        val validContingency = (contingency ?: 0.0).coerceIn(0.0, 100.0)

        // Financial Calculation Rules
        val labourCost = validHours * validRate
        val baseCost = labourCost + validExpenses + validOverhead
        val contingencyAmount = baseCost * (validContingency / 100.0)
        val totalEstimatedCost = baseCost + contingencyAmount

        val targetQuote = if (validMargin < 100.0 && (1.0 - validMargin / 100.0) > 0.0) {
            totalEstimatedCost / (1.0 - (validMargin / 100.0))
        } else {
            totalEstimatedCost
        }

        val estimatedProfit = (targetQuote - totalEstimatedCost).coerceAtLeast(0.0)

        val actualEstimatedMargin = if (targetQuote > 0.0) {
            (estimatedProfit / targetQuote) * 100.0
        } else {
            0.0
        }

        val markupOnCost = if (totalEstimatedCost > 0.0) {
            (estimatedProfit / totalEstimatedCost) * 100.0
        } else {
            0.0
        }

        val breakEvenPrice = totalEstimatedCost

        val effectiveHourlyEarnings = if (validHours > 0.0) {
            estimatedProfit / validHours
        } else {
            0.0
        }

        val effectiveHourlyRate = if (validHours > 0.0) {
            targetQuote / validHours
        } else {
            0.0
        }

        return QuoteCalculationResult(
            estimatedHours = validHours,
            hourlyLabourCost = validRate,
            labourCost = labourCost,
            directExpenses = validExpenses,
            allocatedOverhead = validOverhead,
            baseCost = baseCost,
            contingencyPercent = validContingency,
            contingencyAmount = contingencyAmount,
            totalEstimatedCost = totalEstimatedCost,
            targetMarginPercent = validMargin,
            targetQuote = targetQuote,
            estimatedProfit = estimatedProfit,
            actualEstimatedMargin = actualEstimatedMargin,
            markupOnCost = markupOnCost,
            breakEvenPrice = breakEvenPrice,
            effectiveHourlyEarnings = effectiveHourlyEarnings,
            effectiveHourlyRate = effectiveHourlyRate,
            isValid = errors.isEmpty(),
            errorMessages = errors
        )
    }

    fun formatCurrency(amount: Double, currency: Currency): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val rounded = (safeAmount * 100.0).roundToLong() / 100.0
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (rounded % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        return "${currency.symbol} ${formatter.format(rounded)}"
    }

    fun formatMoney(amount: Double, currency: Currency): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite() || amount < 0) 0.0 else amount
        val rounded = (safeAmount * 100.0).roundToLong() / 100.0
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (rounded % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        val space = if (currency.symbol.length > 1) " " else ""
        return "${currency.symbol}$space${formatter.format(rounded)}"
    }

    fun calculateMilestones(
        totalQuote: Double,
        milestones: List<PaymentMilestone>
    ): List<PaymentMilestone> {
        val totalPct = milestones.sumOf { it.percentage }
        val isReconciled = kotlin.math.abs(totalPct - 100.0) < 0.01
        var runningTotal = 0.0
        return milestones.mapIndexed { index, m ->
            val amount = if (isReconciled && index == milestones.lastIndex) {
                (totalQuote - runningTotal).coerceAtLeast(0.0)
            } else {
                val calculated = (totalQuote * (m.percentage / 100.0) * 100.0).roundToLong() / 100.0
                runningTotal += calculated
                calculated
            }
            m.copy(amount = (amount * 100.0).roundToLong() / 100.0)
        }
    }

    fun generatePrintHtml(
        inputs: QuoteInputState,
        result: QuoteCalculationResult,
        details: ClientQuoteDetails
    ): String = generateClientQuotationHtml(inputs, result, details)

    fun formatPercent(value: Double): String {
        val safe = if (value.isNaN() || value.isInfinite()) 0.0 else value
        val rounded = (safe * 10.0).roundToLong() / 10.0
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (rounded % 1.0 == 0.0) 0 else 1
            maximumFractionDigits = 1
        }
        return "${formatter.format(rounded)}%"
    }

    fun generateClientQuotationText(
        inputs: QuoteInputState,
        result: QuoteCalculationResult,
        details: ClientQuoteDetails
    ): String {
        val sb = StringBuilder()
        val currency = inputs.currency
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())

        sb.appendLine("==================================================")
        sb.appendLine("                 PROJECT QUOTATION                ")
        sb.appendLine("==================================================")
        sb.appendLine("Date: $dateStr")
        if (details.clientName.isNotBlank()) {
            sb.appendLine("Prepared For: ${details.clientName}")
        }
        sb.appendLine("Project Title: ${details.projectTitle}")
        sb.appendLine("Validity: ${details.validityPeriod}")
        sb.appendLine("Estimated Timeline: ${details.deliveryTimeline}")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("FINANCIAL SUMMARY / TOTAL INVESTMENT:")
        sb.appendLine("  ${formatCurrency(result.targetQuote, currency)}")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("\nSCOPE OF WORK & DELIVERABLES:")
        sb.appendLine(details.deliverables)

        if (details.milestones.isNotEmpty()) {
            sb.appendLine("\nPAYMENT SCHEDULE & MILESTONES:")
            val totalPct = details.milestones.sumOf { it.percentage }
            val isReconciled = kotlin.math.abs(totalPct - 100.0) < 0.01
            var runningTotal = 0.0
            val total = result.targetQuote
            details.milestones.forEachIndexed { index, m ->
                val amount = if (isReconciled && index == details.milestones.lastIndex) {
                    (total - runningTotal).coerceAtLeast(0.0)
                } else {
                    val calculated = (total * (m.percentage / 100.0))
                    runningTotal += calculated
                    calculated
                }
                sb.appendLine("• ${m.title} (${formatPercent(m.percentage)}): ${formatCurrency(amount, currency)}")
            }
            if (!isReconciled) {
                sb.appendLine("  [Note: Milestone percentages total ${formatPercent(totalPct)}, not 100%]")
            }
        }

        if (details.additionalTerms.isNotBlank()) {
            sb.appendLine("\nTERMS & CONDITIONS:")
            sb.appendLine(details.additionalTerms)
        }

        sb.appendLine("\n--------------------------------------------------")
        sb.appendLine("Generated by FairQuote Lite - Know what to charge.")
        sb.appendLine("==================================================")

        return sb.toString()
    }

    fun generateClientQuotationHtml(
        inputs: QuoteInputState,
        result: QuoteCalculationResult,
        details: ClientQuoteDetails
    ): String {
        val currency = inputs.currency
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
        val totalFormatted = formatCurrency(result.targetQuote, currency)

        val milestonesHtml = StringBuilder()
        val totalPct = details.milestones.sumOf { it.percentage }
        val isReconciled = kotlin.math.abs(totalPct - 100.0) < 0.01
        var runningTotal = 0.0
        val total = result.targetQuote
        details.milestones.forEachIndexed { index, m ->
            val amount = if (isReconciled && index == details.milestones.lastIndex) {
                (total - runningTotal).coerceAtLeast(0.0)
            } else {
                val calc = (total * (m.percentage / 100.0))
                runningTotal += calc
                calc
            }
            milestonesHtml.append("""
                <tr>
                    <td style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-size: 14px; color: #1E293B;">${m.title}</td>
                    <td style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-size: 14px; color: #64748B; text-align: center;">${formatPercent(m.percentage)}</td>
                    <td style="padding: 10px 14px; border-bottom: 1px solid #E2E8F0; font-size: 14px; color: #0F172A; text-align: right; font-weight: 600;">${formatCurrency(amount, currency)}</td>
                </tr>
            """.trimIndent())
        }

        val deliverablesHtml = details.deliverables
            .split("\n")
            .filter { it.isNotBlank() }
            .joinToString("") { line ->
                val cleanLine = line.removePrefix("•").trim()
                "<li style='margin-bottom: 6px; color: #334155; font-size: 14px; line-height: 1.5;'>$cleanLine</li>"
            }

        val termsHtml = details.additionalTerms
            .split("\n")
            .filter { it.isNotBlank() }
            .joinToString("") { line ->
                val cleanLine = line.removePrefix("•").trim()
                "<li style='margin-bottom: 4px; color: #64748B; font-size: 12px; line-height: 1.5;'>$cleanLine</li>"
            }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Quotation - ${details.projectTitle}</title>
    <style>
        @page {
            size: A4;
            margin: 20mm;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            color: #0F172A;
            background: #FFFFFF;
            margin: 0;
            padding: 24px;
            line-height: 1.4;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            border-bottom: 2px solid #2563EB;
            padding-bottom: 16px;
            margin-bottom: 24px;
        }
        .brand {
            font-size: 24px;
            font-weight: 800;
            color: #2563EB;
            letter-spacing: -0.5px;
        }
        .quote-label {
            font-size: 14px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: #64748B;
            text-align: right;
        }
        .meta-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
            margin-bottom: 24px;
            background: #F8FAFC;
            padding: 16px;
            border-radius: 8px;
            border: 1px solid #E2E8F0;
        }
        .meta-item {
            font-size: 13px;
        }
        .meta-label {
            color: #64748B;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 2px;
        }
        .meta-value {
            font-weight: 600;
            color: #0F172A;
        }
        .total-box {
            background: #EFF6FF;
            border: 1px solid #BFDBFE;
            border-radius: 8px;
            padding: 16px 20px;
            margin-bottom: 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .total-title {
            font-size: 15px;
            font-weight: 700;
            color: #1E3A8A;
        }
        .total-amount {
            font-size: 26px;
            font-weight: 800;
            color: #1D4ED8;
        }
        .section-title {
            font-size: 14px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #0F172A;
            margin-top: 20px;
            margin-bottom: 10px;
            border-bottom: 1px solid #E2E8F0;
            padding-bottom: 6px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        th {
            background: #F1F5F9;
            color: #475569;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            padding: 8px 14px;
            text-align: left;
            border-bottom: 1px solid #CBD5E1;
        }
        .footer {
            margin-top: 36px;
            border-top: 1px solid #E2E8F0;
            padding-top: 12px;
            font-size: 11px;
            color: #94A3B8;
            text-align: center;
        }
        @media print {
            body {
                padding: 0;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div>
            <div class="brand">FairQuote</div>
            <div style="font-size: 13px; color: #64748B;">Client Quotation Document</div>
        </div>
        <div>
            <div class="quote-label">Date: $dateStr</div>
            <div style="font-size: 12px; color: #64748B; text-align: right;">Valid for: ${details.validityPeriod}</div>
        </div>
    </div>

    <div class="meta-grid">
        <div class="meta-item">
            <div class="meta-label">Project Title</div>
            <div class="meta-value">${details.projectTitle}</div>
        </div>
        ${if (details.clientName.isNotBlank()) """
        <div class="meta-item">
            <div class="meta-label">Prepared For</div>
            <div class="meta-value">${details.clientName}</div>
        </div>
        """ else """
        <div class="meta-item">
            <div class="meta-label">Client</div>
            <div class="meta-value">Private Client</div>
        </div>
        """}
        <div class="meta-item">
            <div class="meta-label">Delivery Timeline</div>
            <div class="meta-value">${details.deliveryTimeline}</div>
        </div>
        <div class="meta-item">
            <div class="meta-label">Quote Validity</div>
            <div class="meta-value">${details.validityPeriod}</div>
        </div>
    </div>

    <div class="total-box">
        <div>
            <div class="total-title">Total Estimated Project Investment</div>
            <div style="font-size: 12px; color: #3B82F6;">Fixed price quote based on agreed scope</div>
        </div>
        <div class="total-amount">$totalFormatted</div>
    </div>

    <div class="section-title">Scope of Work &amp; Deliverables</div>
    <ul style="margin: 0 0 20px 20px; padding: 0;">
        $deliverablesHtml
    </ul>

    <div class="section-title">Payment Milestones</div>
    <table>
        <thead>
            <tr>
                <th>Milestone Stage</th>
                <th style="text-align: center;">Share</th>
                <th style="text-align: right;">Amount</th>
            </tr>
        </thead>
        <tbody>
            $milestonesHtml
        </tbody>
    </table>

    ${if (details.additionalTerms.isNotBlank()) """
    <div class="section-title">Terms &amp; Conditions</div>
    <ul style="margin: 0 0 20px 20px; padding: 0;">
        $termsHtml
    </ul>
    """ else ""}

    <div class="footer">
        Generated with FairQuote Lite • Professional cost estimation & quotation engine for freelancers.
    </div>
</body>
</html>
        """.trimIndent()
    }
}
