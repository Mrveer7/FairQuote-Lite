package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logic.QuoteCalculator
import com.example.model.Screen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuoteBuilderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FairQuoteViewModel
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Retain strong reference to prevent garbage collection before onPageFinished
    private var activePrintWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            MobileAds.initialize(this) {}
        } catch (_: Exception) {
        }
        setContent {
            val viewModel: FairQuoteViewModel = viewModel()
            MyApplicationTheme(themeMode = viewModel.currentThemeMode) {
                FairQuoteApp(
                    viewModel = viewModel,
                    onPrintRequested = { html, title ->
                        printHtmlDocument(this, html, title)
                    }
                )
            }
        }
    }

    private fun printHtmlDocument(context: Context, htmlContent: String, projectTitle: String) {
        val cleanTitle = projectTitle.ifBlank { "Quotation" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(40)

        val webView = WebView(context)
        activePrintWebView = webView

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter = webView.createPrintDocumentAdapter("FairQuote_$cleanTitle")
                    val jobName = "FairQuote - $cleanTitle"
                    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                } else {
                    Toast.makeText(context, "Printing is not available on this device", Toast.LENGTH_SHORT).show()
                }
                activePrintWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}

@Composable
fun FairQuoteApp(
    viewModel: FairQuoteViewModel,
    onPrintRequested: (html: String, title: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen = viewModel.currentScreen
    val quoteInputs = viewModel.quoteInputs
    val quoteDetails = viewModel.quoteDetails
    val calculationResult = viewModel.calculationResult
    val currentTheme = viewModel.currentThemeMode

    // Handle back button for sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.handleBackNavigation()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                Screen.HOME -> {
                    HomeScreen(
                        selectedCurrency = quoteInputs.currency,
                        onCurrencySelected = { newCurrency ->
                            viewModel.updateCurrency(newCurrency)
                        },
                        currentTheme = currentTheme,
                        onThemeSelected = { newTheme ->
                            viewModel.updateThemeMode(newTheme)
                        },
                        onCalculateQuoteClicked = {
                            viewModel.navigateTo(Screen.CALCULATOR)
                        },
                        onApplyPreset = { presetInputs ->
                            viewModel.applyPreset(presetInputs)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Preset applied to calculator.")
                            }
                        }
                    )
                }

                Screen.CALCULATOR -> {
                    CalculatorScreen(
                        inputs = quoteInputs,
                        calculationResult = calculationResult,
                        currentTheme = currentTheme,
                        onThemeSelected = { newTheme ->
                            viewModel.updateThemeMode(newTheme)
                        },
                        onInputsChanged = { updatedInputs ->
                            viewModel.updateInputs(updatedInputs)
                        },
                        onNavigateBack = {
                            viewModel.navigateTo(Screen.HOME)
                        },
                        onNavigateToQuoteBuilder = {
                            if (calculationResult.isValid) {
                                viewModel.navigateTo(Screen.QUOTE_BUILDER)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fix input errors before building quote.")
                                }
                            }
                        },
                        onSaveDefaults = {
                            viewModel.saveDefaults()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Rates & margins saved as your new default!")
                            }
                        },
                        onResetToDefaults = {
                            viewModel.resetToDefaults()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Calculator reset to defaults.")
                            }
                        },
                        onResetSavedDefaults = {
                            viewModel.resetSavedPreferences()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Saved defaults cleared and factory values restored.")
                            }
                        }
                    )
                }

                Screen.QUOTE_BUILDER -> {
                    QuoteBuilderScreen(
                        quoteInput = quoteInputs,
                        calculationResult = calculationResult,
                        quoteDetails = quoteDetails,
                        currentTheme = currentTheme,
                        onThemeSelected = { newTheme ->
                            viewModel.updateThemeMode(newTheme)
                        },
                        onQuoteDetailsChanged = { updatedDetails ->
                            viewModel.updateQuoteDetails(updatedDetails)
                        },
                        onNavigateBackToCalculator = {
                            viewModel.navigateTo(Screen.CALCULATOR)
                        },
                        onCopyQuote = {
                            try {
                                val text = QuoteCalculator.generateClientQuotationText(
                                    quoteInputs,
                                    calculationResult,
                                    quoteDetails
                                )
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("FairQuote Quotation", text)
                                clipboard.setPrimaryClip(clip)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Quotation copied to clipboard! Ready to paste.")
                                }
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to copy quotation to clipboard.")
                                }
                            }
                        },
                        onShareQuote = {
                            try {
                                val text = QuoteCalculator.generateClientQuotationText(
                                    quoteInputs,
                                    calculationResult,
                                    quoteDetails
                                )
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Quotation: ${quoteDetails.projectTitle}")
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Quotation via")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to trigger share.")
                                }
                            }
                        },
                        onPrintQuote = {
                            try {
                                val html = QuoteCalculator.generateClientQuotationHtml(
                                    quoteInputs,
                                    calculationResult,
                                    quoteDetails
                                )
                                onPrintRequested(html, quoteDetails.projectTitle)
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to generate print document.")
                                }
                            }
                        },
                        onStartOver = {
                            viewModel.startOver()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All values reset to start fresh.")
                            }
                        }
                    )
                }
            }
        }
    }
}
