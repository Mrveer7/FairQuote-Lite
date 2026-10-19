package com.example.model

data class Currency(
    val code: String,
    val symbol: String,
    val name: String
)

val SupportedCurrencies: List<Currency> = listOf(
    Currency("INR", "₹", "Indian Rupee"),
    Currency("USD", "$", "US Dollar"),
    Currency("EUR", "€", "Euro"),
    Currency("GBP", "£", "British Pound"),
    Currency("AUD", "A$", "Australian Dollar"),
    Currency("CAD", "C$", "Canadian Dollar"),
    Currency("SGD", "S$", "Singapore Dollar"),
    Currency("AED", "AED", "UAE Dirham")
)

data class QuoteInputState(
    val projectName: String = "",
    val estimatedHours: String = "20",
    val hourlyLabourCost: String = "500",
    val directExpenses: String = "2000",
    val allocatedOverhead: String = "1000",
    val targetMarginPercent: String = "25",
    val contingencyPercent: String = "0",
    val currency: Currency = SupportedCurrencies[0]
)

data class QuoteCalculationResult(
    val estimatedHours: Double,
    val hourlyLabourCost: Double,
    val labourCost: Double,
    val directExpenses: Double,
    val allocatedOverhead: Double,
    val baseCost: Double,
    val contingencyPercent: Double,
    val contingencyAmount: Double,
    val totalEstimatedCost: Double,
    val targetMarginPercent: Double,
    val targetQuote: Double,
    val estimatedProfit: Double,
    val actualEstimatedMargin: Double,
    val markupOnCost: Double,
    val breakEvenPrice: Double,
    val effectiveHourlyEarnings: Double,
    val effectiveHourlyRate: Double = 0.0,
    val isValid: Boolean,
    val errorMessages: List<String> = emptyList()
)

data class PaymentMilestone(
    val id: String,
    val title: String,
    val percentage: Double,
    val amount: Double = 0.0
)

data class ClientQuoteDetails(
    val clientName: String = "",
    val projectTitle: String = "Freelance Project Deliverables",
    val deliverables: String = "• Initial research, wireframes & prototype\n• Production-ready design system & assets\n• Responsive development & implementation\n• Quality assurance testing and deployment",
    val deliveryTimeline: String = "3-4 weeks from project kickoff",
    val validityPeriod: String = "14 days from date of issue",
    val milestones: List<PaymentMilestone> = listOf(
        PaymentMilestone("1", "Deposit upon project kickoff", 50.0, 0.0),
        PaymentMilestone("2", "Interim milestone / Beta approval", 25.0, 0.0),
        PaymentMilestone("3", "Final delivery and handover", 25.0, 0.0)
    ),
    val additionalTerms: String = "• Includes up to 2 rounds of design revisions.\n• Additional out-of-scope requests will be quoted separately.\n• Full intellectual property rights transfer upon final payment."
)

enum class Screen {
    HOME,
    CALCULATOR,
    QUOTE_BUILDER
}

enum class AppThemeMode(val id: String, val title: String, val subtitle: String) {
    CLASSIC("classic", "Classic Blue", "Tasteful SaaS blue & slate"),
    MONOCHROME_LIGHT("mono_light", "Black & White (Light)", "Minimalist high-contrast paper & ink"),
    MONOCHROME_DARK("mono_dark", "Black & White (Dark)", "OLED pitch black & crisp white")
}
