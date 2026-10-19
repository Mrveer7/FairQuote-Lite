package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppThemeMode
import com.example.model.Currency
import com.example.model.QuoteInputState
import com.example.model.SupportedCurrencies

class QuotePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fairquote_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOURLY_RATE = "pref_hourly_rate"
        private const val KEY_OVERHEAD = "pref_overhead"
        private const val KEY_MARGIN = "pref_margin"
        private const val KEY_CONTINGENCY = "pref_contingency"
        private const val KEY_CURRENCY = "pref_currency"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val DEFAULT_CONTINGENCY = "0"
    }

    fun getFactoryDefaultInputs(): QuoteInputState {
        return QuoteInputState(
            projectName = "",
            estimatedHours = "20",
            hourlyLabourCost = "500",
            directExpenses = "2000",
            allocatedOverhead = "1000",
            targetMarginPercent = "25",
            contingencyPercent = DEFAULT_CONTINGENCY,
            currency = SupportedCurrencies[0]
        )
    }

    fun hasCustomDefaults(): Boolean {
        return prefs.contains(KEY_HOURLY_RATE) ||
               prefs.contains(KEY_OVERHEAD) ||
               prefs.contains(KEY_MARGIN) ||
               prefs.contains(KEY_CONTINGENCY) ||
               prefs.contains(KEY_CURRENCY)
    }

    fun loadDefaultInputs(): QuoteInputState {
        val hourlyRate = prefs.getString(KEY_HOURLY_RATE, "500") ?: "500"
        val overhead = prefs.getString(KEY_OVERHEAD, "1000") ?: "1000"
        val margin = prefs.getString(KEY_MARGIN, "25") ?: "25"
        val contingency = prefs.getString(KEY_CONTINGENCY, DEFAULT_CONTINGENCY) ?: DEFAULT_CONTINGENCY
        val currencyCode = prefs.getString(KEY_CURRENCY, "INR") ?: "INR"
        val currency = SupportedCurrencies.find { it.code == currencyCode } ?: SupportedCurrencies[0]

        return QuoteInputState(
            projectName = "",
            estimatedHours = "20",
            hourlyLabourCost = hourlyRate,
            directExpenses = "2000",
            allocatedOverhead = overhead,
            targetMarginPercent = margin,
            contingencyPercent = contingency,
            currency = currency
        )
    }

    fun saveDefaults(
        hourlyRate: String,
        overhead: String,
        margin: String,
        contingency: String = DEFAULT_CONTINGENCY,
        currency: Currency
    ) {
        prefs.edit()
            .putString(KEY_HOURLY_RATE, hourlyRate)
            .putString(KEY_OVERHEAD, overhead)
            .putString(KEY_MARGIN, margin)
            .putString(KEY_CONTINGENCY, contingency)
            .putString(KEY_CURRENCY, currency.code)
            .apply()
    }

    fun loadThemeMode(): AppThemeMode {
        val id = prefs.getString(KEY_THEME_MODE, AppThemeMode.CLASSIC.id)
        return AppThemeMode.entries.find { it.id == id } ?: AppThemeMode.CLASSIC
    }

    fun saveThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.id).apply()
    }

    fun resetDefaults() {
        prefs.edit()
            .remove(KEY_HOURLY_RATE)
            .remove(KEY_OVERHEAD)
            .remove(KEY_MARGIN)
            .remove(KEY_CONTINGENCY)
            .remove(KEY_CURRENCY)
            .apply()
    }
}
