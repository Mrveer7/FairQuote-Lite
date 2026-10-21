package com.example.ads

/**
 * AdMob configuration for FairQuote Lite.
 *
 * Provides centralized management for test and production banner ad unit IDs.
 * By default, [USE_TEST_ADS] is set to `true` to guarantee Google AdMob policy compliance
 * during development, automated testing, and device verification.
 */
object AdConfig {
    /**
     * Google's official sample/test banner ad unit ID.
     * Guaranteed to serve test creatives without generating invalid impressions.
     * See: https://developers.google.com/admob/android/test-ads
     */
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    /**
     * FairQuote Lite production banner ad unit ID configured in Google AdMob.
     */
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-2728945467017612/1823437220"

    /**
     * Development flag controlling ad serving behavior.
     *
     * - `true`: Serves Google's official test banner (safe for dev/testing/GitHub Actions).
     * - `false`: Serves real production ads using [PROD_BANNER_AD_UNIT_ID].
     */
    const val USE_TEST_ADS = true

    /**
     * Active banner ad unit ID determined by [USE_TEST_ADS].
     */
    val bannerAdUnitId: String
        get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
}
