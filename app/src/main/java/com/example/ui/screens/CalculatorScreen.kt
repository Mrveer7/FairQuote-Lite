package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.QuoteCalculator
import com.example.model.AppThemeMode
import com.example.model.Currency
import com.example.model.QuoteCalculationResult
import com.example.model.QuoteInputState
import com.example.ui.components.CurrencySelector
import com.example.ui.components.MetricCard
import com.example.ui.components.NumberInputField
import com.example.ui.components.ThemeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    inputs: QuoteInputState,
    calculationResult: QuoteCalculationResult,
    onInputsChanged: (QuoteInputState) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToQuoteBuilder: () -> Unit,
    onSaveDefaults: () -> Unit,
    onResetToDefaults: () -> Unit,
    onResetSavedDefaults: () -> Unit = {},
    currentTheme: AppThemeMode = AppThemeMode.CLASSIC,
    onThemeSelected: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }
    var showBreakdownAccordion by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    val currency = inputs.currency

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Calculator") },
            text = { Text("Would you like to reset the inputs back to your saved preferences or standard factory defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Saved Defaults")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onResetSavedDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Factory Defaults")
                }
            }
        )
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = { Text("Quote Calculation Method") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "FairQuote Lite uses standard professional cost-accounting formulas to protect freelance margins:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Labour Cost = Hours × Hourly Rate\n" +
                               "2. Base Cost = Labour Cost + Direct Expenses + Overhead\n" +
                               "3. Contingency = Base Cost × Contingency %\n" +
                               "4. Total Estimated Cost = Base Cost + Contingency\n" +
                               "5. Target Quote = Total Cost / (1 - Target Margin %)\n" +
                               "6. Estimated Profit = Target Quote - Total Cost\n" +
                               "7. Net Profit per Hour = Estimated Profit / Hours\n" +
                               "8. Effective Hourly Rate = Recommended Quote / Hours",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Note: These figures are planning estimates based on the parameters provided, not guaranteed profits. Actual outcomes depend on execution time and client scope changes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Quote Calculator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Real-time cost & margin breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("calculator_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                ThemeSelector(
                    currentTheme = currentTheme,
                    onThemeSelected = onThemeSelected
                )
                Spacer(modifier = Modifier.width(4.dp))
                CurrencySelector(
                    selectedCurrency = currency,
                    onCurrencySelected = { newCurrency ->
                        onInputsChanged(inputs.copy(currency = newCurrency))
                    }
                )
                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.testTag("reset_calculator_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset values", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Validation Error Banner if any
            if (!calculationResult.isValid && calculationResult.errorMessages.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Please correct the following inputs:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        calculationResult.errorMessages.forEach { err ->
                            Text(
                                text = "• $err",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Results Summary Hero Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "RECOMMENDED CLIENT QUOTE",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { showExplanationDialog = true },
                            modifier = Modifier.size(28.dp).testTag("calculation_help_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "How this is calculated",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = QuoteCalculator.formatCurrency(calculationResult.targetQuote, currency),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Protects ${QuoteCalculator.formatPercent(calculationResult.targetMarginPercent)} target margin on ${QuoteCalculator.formatCurrency(calculationResult.totalEstimatedCost, currency)} total cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Estimated Profit",
                            value = QuoteCalculator.formatCurrency(calculationResult.estimatedProfit, currency),
                            subtitle = "${QuoteCalculator.formatPercent(calculationResult.markupOnCost)} markup",
                            highlight = true,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Net Profit / Hr",
                            value = QuoteCalculator.formatCurrency(calculationResult.effectiveHourlyEarnings, currency),
                            subtitle = if (calculationResult.estimatedHours > 0) {
                                "Quote: ${QuoteCalculator.formatCurrency(calculationResult.effectiveHourlyRate, currency)}/h"
                            } else {
                                "0h work"
                            },
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Total Cost",
                            value = QuoteCalculator.formatCurrency(calculationResult.totalEstimatedCost, currency),
                            subtitle = "Break-even target",
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Contingency Buffer",
                            value = QuoteCalculator.formatCurrency(calculationResult.contingencyAmount, currency),
                            subtitle = "${QuoteCalculator.formatPercent(calculationResult.contingencyPercent)} risk buffer",
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveDefaults,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_defaults_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Defaults", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onNavigateToQuoteBuilder,
                    modifier = Modifier.weight(1.3f).height(48.dp).testTag("build_quote_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Create Quotation", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs Form Section
            Text(
                text = "Project Cost Inputs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Adjust your estimated parameters to fine-tune your quotation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Optional Project Name
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Project Name (Optional)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputs.projectName,
                    onValueChange = { onInputsChanged(inputs.copy(projectName = it)) },
                    placeholder = { Text("e.g. Acme Website Redesign") },
                    modifier = Modifier.fillMaxWidth().testTag("project_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // 2. Estimated Work Hours
            NumberInputField(
                label = "Estimated Work Hours",
                value = inputs.estimatedHours,
                onValueChange = { onInputsChanged(inputs.copy(estimatedHours = it)) },
                suffix = "hours",
                helperText = "Default 20 hours. Billable production, research & revisions.",
                onStep = { delta ->
                    val current = inputs.estimatedHours.toDoubleOrNull() ?: 20.0
                    val next = (current + delta).coerceAtLeast(1.0)
                    onInputsChanged(inputs.copy(estimatedHours = next.toInt().toString()))
                }
            )

            // 3. Hourly Labour Cost
            NumberInputField(
                label = "Hourly Labour Cost",
                value = inputs.hourlyLabourCost,
                onValueChange = { onInputsChanged(inputs.copy(hourlyLabourCost = it)) },
                prefix = currency.symbol,
                suffix = "/ hour",
                helperText = "Your internal compensation or labor wage rate.",
                onStep = { delta ->
                    val stepAmount = if (currency.code == "INR") 50.0 else 5.0
                    val current = inputs.hourlyLabourCost.toDoubleOrNull() ?: 500.0
                    val next = (current + delta * stepAmount).coerceAtLeast(0.0)
                    onInputsChanged(inputs.copy(hourlyLabourCost = next.toInt().toString()))
                }
            )

            // 4. Direct Project Expenses
            NumberInputField(
                label = "Direct Project Expenses",
                value = inputs.directExpenses,
                onValueChange = { onInputsChanged(inputs.copy(directExpenses = it)) },
                prefix = currency.symbol,
                helperText = "Direct licenses, domain, third-party APIs, sub-contractors."
            )

            // 5. Allocated Overhead
            NumberInputField(
                label = "Allocated Overhead",
                value = inputs.allocatedOverhead,
                onValueChange = { onInputsChanged(inputs.copy(allocatedOverhead = it)) },
                prefix = currency.symbol,
                helperText = "Portion of software subscriptions, utilities, internet, hardware depreciation."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Target Profit Margin Slider & Chips
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Profit Margin",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${inputs.targetMarginPercent}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val currentMarginVal = inputs.targetMarginPercent.toFloatOrNull() ?: 25f
                Slider(
                    value = currentMarginVal.coerceIn(0f, 95f),
                    onValueChange = { onInputsChanged(inputs.copy(targetMarginPercent = it.toInt().toString())) },
                    valueRange = 0f..90f,
                    steps = 17,
                    modifier = Modifier.fillMaxWidth().testTag("margin_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Quick Margin Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("15", "20", "25", "30", "40").forEach { presetMargin ->
                        val isSelected = inputs.targetMarginPercent == presetMargin
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInputsChanged(inputs.copy(targetMarginPercent = presetMargin)) },
                            label = { Text("$presetMargin%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Optional Contingency Buffer
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Optional Contingency Buffer",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${inputs.contingencyPercent}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val currentContingencyVal = inputs.contingencyPercent.toFloatOrNull() ?: 0f
                Slider(
                    value = currentContingencyVal.coerceIn(0f, 30f),
                    onValueChange = { onInputsChanged(inputs.copy(contingencyPercent = it.toInt().toString())) },
                    valueRange = 0f..30f,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth().testTag("contingency_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("0", "5", "10", "15", "20").forEach { presetCont ->
                        val isSelected = inputs.contingencyPercent == presetCont
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInputsChanged(inputs.copy(contingencyPercent = presetCont)) },
                            label = { Text("$presetCont%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed Breakdown Accordion
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBreakdownAccordion = !showBreakdownAccordion },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Itemized Cost Breakdown",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (showBreakdownAccordion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showBreakdownAccordion) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showBreakdownAccordion) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            BreakdownRow("Labour (${calculationResult.estimatedHours}h × ${currency.symbol}${calculationResult.hourlyLabourCost.toInt()})", calculationResult.labourCost, currency)
                            BreakdownRow("Direct Project Expenses", calculationResult.directExpenses, currency)
                            BreakdownRow("Allocated Overhead", calculationResult.allocatedOverhead, currency)
                            BreakdownRow("Subtotal (Base Cost)", calculationResult.baseCost, currency, isBold = true)
                            if (calculationResult.contingencyPercent > 0) {
                                BreakdownRow("Contingency Buffer (${calculationResult.contingencyPercent.toInt()}%)", calculationResult.contingencyAmount, currency)
                            }
                            BreakdownRow("Total Estimated Project Cost", calculationResult.totalEstimatedCost, currency, isBold = true, isHighlighted = true)
                            BreakdownRow("Target Profit (${calculationResult.targetMarginPercent.toInt()}%)", calculationResult.estimatedProfit, currency, isBold = true)
                            BreakdownRow("Recommended Quote", calculationResult.targetQuote, currency, isBold = true, isPrimary = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            BreakdownRow("Net Profit per Hour", calculationResult.effectiveHourlyEarnings, currency)
                            BreakdownRow("Effective Hourly Rate (Quote/h)", calculationResult.effectiveHourlyRate, currency)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: Double,
    currency: Currency,
    isBold: Boolean = false,
    isHighlighted: Boolean = false,
    isPrimary: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else if (isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = QuoteCalculator.formatCurrency(amount, currency),
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
