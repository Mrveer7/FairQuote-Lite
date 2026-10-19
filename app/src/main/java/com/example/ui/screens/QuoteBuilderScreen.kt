package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.QuoteCalculator
import com.example.model.AppThemeMode
import com.example.model.ClientQuoteDetails
import com.example.model.PaymentMilestone
import com.example.model.QuoteCalculationResult
import com.example.model.QuoteInputState
import com.example.ui.components.ThemeSelector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBuilderScreen(
    quoteInput: QuoteInputState,
    calculationResult: QuoteCalculationResult,
    quoteDetails: ClientQuoteDetails,
    onQuoteDetailsChanged: (ClientQuoteDetails) -> Unit,
    onNavigateBackToCalculator: () -> Unit,
    onCopyQuote: () -> Unit,
    onShareQuote: () -> Unit,
    onPrintQuote: () -> Unit,
    onStartOver: () -> Unit,
    currentTheme: AppThemeMode = AppThemeMode.CLASSIC,
    onThemeSelected: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Edit Fields, 1 = Preview Document
    var showStartOverDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val currency = quoteInput.currency

    if (showStartOverDialog) {
        AlertDialog(
            onDismissRequest = { showStartOverDialog = false },
            title = { Text("Start Over?") },
            text = { Text("This will reset all project inputs and quote details back to standard defaults.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStartOver()
                        showStartOverDialog = false
                    }
                ) {
                    Text("Start Over", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartOverDialog = false }) {
                    Text("Cancel")
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
                        text = "Quotation Builder",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = QuoteCalculator.formatCurrency(calculationResult.targetQuote, currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBackToCalculator,
                    modifier = Modifier.testTag("quote_builder_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to calculator")
                }
            },
            actions = {
                ThemeSelector(
                    currentTheme = currentTheme,
                    onThemeSelected = onThemeSelected
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onPrintQuote,
                    modifier = Modifier.testTag("action_print_pdf_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print or Save PDF")
                }
                IconButton(
                    onClick = onShareQuote,
                    modifier = Modifier.testTag("action_share_quote_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share quotation")
                }
                IconButton(
                    onClick = { showStartOverDialog = true },
                    modifier = Modifier.testTag("action_start_over_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Start over")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Mode Tabs: Edit vs Preview
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Scope & Terms")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Client Preview")
                    }
                }
            )
        }

        // Action Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCopyQuote,
                    modifier = Modifier.weight(1f).height(42.dp).testTag("copy_quote_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Quote", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onPrintQuote,
                    modifier = Modifier.weight(1f).height(42.dp).testTag("print_pdf_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / PDF", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        if (selectedTab == 0) {
            // Edit Fields Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Client & Project Info
                Text(
                    text = "Client & Project Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                BuilderInputField(
                    label = "Client Name (Optional)",
                    value = quoteDetails.clientName,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(clientName = it)) },
                    placeholder = "e.g. Acme Corp / Alex Smith"
                )

                BuilderInputField(
                    label = "Project Title",
                    value = quoteDetails.projectTitle,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(projectTitle = it)) },
                    placeholder = "e.g. Full-Stack Web App Development"
                )

                BuilderInputField(
                    label = "Delivery Timeline",
                    value = quoteDetails.deliveryTimeline,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(deliveryTimeline = it)) },
                    placeholder = "e.g. 3-4 weeks from initial deposit"
                )

                BuilderInputField(
                    label = "Quote Validity Period",
                    value = quoteDetails.validityPeriod,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(validityPeriod = it)) },
                    placeholder = "e.g. 14 days from date of issue"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scope & Deliverables
                Text(
                    text = "Scope of Work & Deliverables",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "List key deliverables and work stages for client sign-off",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quoteDetails.deliverables,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(deliverables = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .testTag("deliverables_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Milestones Management
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Payment Milestones",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Percentages automatically balance total quote amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = {
                            val newMilestone = PaymentMilestone(
                                id = UUID.randomUUID().toString(),
                                title = "Milestone Stage ${quoteDetails.milestones.size + 1}",
                                percentage = 10.0
                            )
                            onQuoteDetailsChanged(
                                quoteDetails.copy(milestones = quoteDetails.milestones + newMilestone)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stage")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val totalMilestonePercentage = quoteDetails.milestones.sumOf { it.percentage }
                val isMilestoneBalanced = kotlin.math.abs(totalMilestonePercentage - 100.0) < 0.01
                val isMilestoneOver = totalMilestonePercentage > 100.01
                val formattedTotalPct = QuoteCalculator.formatPercent(totalMilestonePercentage)

                // Live Milestone Percentage Summary Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isMilestoneBalanced -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        isMilestoneOver -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isMilestoneBalanced -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            isMilestoneOver -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isMilestoneBalanced -> Icons.Default.CheckCircle
                                isMilestoneOver -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when {
                                isMilestoneBalanced -> MaterialTheme.colorScheme.primary
                                isMilestoneOver -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    isMilestoneBalanced -> "Milestone Total: 100% (Balanced)"
                                    isMilestoneOver -> "Milestone Total: $formattedTotalPct (Over by ${QuoteCalculator.formatPercent(totalMilestonePercentage - 100.0)})"
                                    else -> "Milestone Total: $formattedTotalPct (${QuoteCalculator.formatPercent(100.0 - totalMilestonePercentage)} unallocated)"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = when {
                                    isMilestoneBalanced -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isMilestoneOver -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                            Text(
                                text = when {
                                    isMilestoneBalanced -> "All milestone amounts reconcile with recommended quote (${QuoteCalculator.formatCurrency(calculationResult.targetQuote, currency)})."
                                    isMilestoneOver -> "Combined percentages exceed 100%. Please decrease milestone shares to equal 100%."
                                    else -> "Combined percentages are below 100%. Adjust shares so the total equals 100%."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    isMilestoneBalanced -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    isMilestoneOver -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                    else -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Milestone Cards
                var runningSum = 0.0
                val totalAmount = calculationResult.targetQuote
                quoteDetails.milestones.forEachIndexed { index, milestone ->
                    val isLast = index == quoteDetails.milestones.lastIndex
                    val calculatedAmount = if (isMilestoneBalanced && isLast) {
                        (totalAmount - runningSum).coerceAtLeast(0.0)
                    } else {
                        val amt = totalAmount * (milestone.percentage / 100.0)
                        runningSum += amt
                        amt
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = milestone.title,
                                    onValueChange = { newTitle ->
                                        val updated = quoteDetails.milestones.toMutableList()
                                        updated[index] = milestone.copy(title = newTitle)
                                        onQuoteDetailsChanged(quoteDetails.copy(milestones = updated))
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Amount: ${QuoteCalculator.formatCurrency(calculatedAmount, currency)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedTextField(
                                value = if (milestone.percentage % 1.0 == 0.0) milestone.percentage.toInt().toString() else milestone.percentage.toString(),
                                onValueChange = { newPctStr ->
                                    val newPct = if (newPctStr.isBlank()) 0.0 else (newPctStr.toDoubleOrNull() ?: milestone.percentage)
                                    val updated = quoteDetails.milestones.toMutableList()
                                    updated[index] = milestone.copy(percentage = newPct.coerceIn(0.0, 100.0))
                                    onQuoteDetailsChanged(quoteDetails.copy(milestones = updated))
                                },
                                suffix = { Text("%") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(84.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (quoteDetails.milestones.size > 1) {
                                IconButton(
                                    onClick = {
                                        val updated = quoteDetails.milestones.toMutableList()
                                        updated.removeAt(index)
                                        onQuoteDetailsChanged(quoteDetails.copy(milestones = updated))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove milestone",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Additional Terms
                Text(
                    text = "Additional Terms & Conditions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quoteDetails.additionalTerms,
                    onValueChange = { onQuoteDetailsChanged(quoteDetails.copy(additionalTerms = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .testTag("additional_terms_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Live Preview Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Document Canvas Styled Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        // Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "FAIRQUOTE",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Official Client Quotation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
                                Text(
                                    text = "DATE: $dateStr",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "VALID: ${quoteDetails.validityPeriod}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Project Meta Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PROJECT TITLE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = quoteDetails.projectTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PREPARED FOR",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = quoteDetails.clientName.ifBlank { "Private Client" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ESTIMATED TIMELINE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = quoteDetails.deliveryTimeline,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PRICING BASIS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Fixed Investment Quote",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Investment Total Callout
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Investment Quote",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Includes all production, deliverables & revisions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = QuoteCalculator.formatCurrency(calculationResult.targetQuote, currency),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Deliverables
                        Text(
                            text = "DELIVERABLES & SCOPE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quoteDetails.deliverables,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Milestones Table
                        Text(
                            text = "PAYMENT SCHEDULE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        var runningTotal = 0.0
                        val total = calculationResult.targetQuote
                        val totalPreviewPct = quoteDetails.milestones.sumOf { it.percentage }
                        val isPreviewBalanced = kotlin.math.abs(totalPreviewPct - 100.0) < 0.01
                        quoteDetails.milestones.forEachIndexed { idx, m ->
                            val amount = if (isPreviewBalanced && idx == quoteDetails.milestones.lastIndex) {
                                (total - runningTotal).coerceAtLeast(0.0)
                            } else {
                                val calculated = (total * (m.percentage / 100.0))
                                runningTotal += calculated
                                calculated
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${m.title} (${QuoteCalculator.formatPercent(m.percentage)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = QuoteCalculator.formatCurrency(amount, currency),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!isPreviewBalanced) {
                            Text(
                                text = "⚠️ Milestone percentages total ${QuoteCalculator.formatPercent(totalPreviewPct)} (does not equal 100%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (quoteDetails.additionalTerms.isNotBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "TERMS & CONDITIONS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = quoteDetails.additionalTerms,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Footer Note
                        Text(
                            text = "Generated by FairQuote Lite • Professional Quote Estimator",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun BuilderInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
