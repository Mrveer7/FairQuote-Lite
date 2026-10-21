package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ads.AdConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Reusable Jetpack Compose component to display a Google AdMob banner advertisement.
 *
 * Implements AndroidView with lifecycle awareness (pause, resume, destroy),
 * safe error handling without crashing, and prevents reloading on recomposition.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.bannerAdUnitId,
    adSize: AdSize = AdSize.BANNER
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob Banner Preview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adFailedToLoad by remember { mutableStateOf(false) }

    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdLoaded = true
                    adFailedToLoad = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    isAdLoaded = false
                    adFailedToLoad = true
                }
            }
            try {
                loadAd(AdRequest.Builder().build())
            } catch (_: Exception) {
                adFailedToLoad = true
            }
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        adView.resume()
                    } catch (_: Exception) {
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        adView.pause()
                    } catch (_: Exception) {
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        adView.destroy()
                    } catch (_: Exception) {
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                adView.destroy()
            } catch (_: Exception) {
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_container")
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
