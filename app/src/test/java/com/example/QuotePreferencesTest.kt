package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.QuotePreferences
import com.example.model.SupportedCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuotePreferencesTest {

    private lateinit var preferences: QuotePreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        preferences = QuotePreferences(context)
        preferences.resetDefaults()
    }

    @Test
    fun testDefaultContingencyIsZero() {
        val defaults = preferences.loadDefaultInputs()
        assertEquals("0", defaults.contingencyPercent)
        assertFalse(preferences.hasCustomDefaults())
    }

    @Test
    fun testSaveAndRestoreContingency() {
        preferences.saveDefaults(
            hourlyRate = "750",
            overhead = "1500",
            margin = "30",
            contingency = "15",
            currency = SupportedCurrencies[1] // USD
        )

        assertTrue(preferences.hasCustomDefaults())
        val loaded = preferences.loadDefaultInputs()
        assertEquals("750", loaded.hourlyLabourCost)
        assertEquals("1500", loaded.allocatedOverhead)
        assertEquals("30", loaded.targetMarginPercent)
        assertEquals("15", loaded.contingencyPercent)
        assertEquals("USD", loaded.currency.code)

        preferences.resetDefaults()
        assertFalse(preferences.hasCustomDefaults())
        val resetLoaded = preferences.loadDefaultInputs()
        assertEquals("0", resetLoaded.contingencyPercent)
    }
}
