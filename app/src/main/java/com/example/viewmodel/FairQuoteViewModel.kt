package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.data.QuotePreferences
import com.example.logic.QuoteCalculator
import com.example.model.AppThemeMode
import com.example.model.ClientQuoteDetails
import com.example.model.Currency
import com.example.model.QuoteCalculationResult
import com.example.model.QuoteInputState
import com.example.model.Screen

class FairQuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = QuotePreferences(application)

    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    var quoteInputs by mutableStateOf(preferences.loadDefaultInputs())
        private set

    var quoteDetails by mutableStateOf(ClientQuoteDetails())
        private set

    var currentThemeMode by mutableStateOf(preferences.loadThemeMode())
        private set

    val calculationResult: QuoteCalculationResult
        get() = QuoteCalculator.calculate(quoteInputs)

    fun updateThemeMode(mode: AppThemeMode) {
        currentThemeMode = mode
        preferences.saveThemeMode(mode)
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun handleBackNavigation(): Boolean {
        return when (currentScreen) {
            Screen.CALCULATOR -> {
                currentScreen = Screen.HOME
                true
            }
            Screen.QUOTE_BUILDER -> {
                currentScreen = Screen.CALCULATOR
                true
            }
            Screen.HOME -> false
        }
    }

    fun updateInputs(newInputs: QuoteInputState) {
        quoteInputs = newInputs
        if (newInputs.projectName.isNotBlank() && quoteDetails.projectTitle != newInputs.projectName) {
            quoteDetails = quoteDetails.copy(projectTitle = newInputs.projectName)
        }
    }

    fun updateCurrency(currency: Currency) {
        quoteInputs = quoteInputs.copy(currency = currency)
    }

    fun applyPreset(preset: QuoteInputState) {
        quoteInputs = preset
        quoteDetails = quoteDetails.copy(
            projectTitle = preset.projectName.ifBlank { "Client Project" }
        )
        currentScreen = Screen.CALCULATOR
    }

    fun updateQuoteDetails(newDetails: ClientQuoteDetails) {
        quoteDetails = newDetails
    }

    fun saveDefaults(): Boolean {
        preferences.saveDefaults(
            hourlyRate = quoteInputs.hourlyLabourCost,
            overhead = quoteInputs.allocatedOverhead,
            margin = quoteInputs.targetMarginPercent,
            contingency = quoteInputs.contingencyPercent,
            currency = quoteInputs.currency
        )
        return true
    }

    fun resetToDefaults() {
        quoteInputs = preferences.loadDefaultInputs()
    }

    fun resetSavedPreferences() {
        preferences.resetDefaults()
        quoteInputs = preferences.getFactoryDefaultInputs()
    }

    fun startOver() {
        quoteInputs = preferences.loadDefaultInputs()
        quoteDetails = ClientQuoteDetails()
        currentScreen = Screen.HOME
    }
}
