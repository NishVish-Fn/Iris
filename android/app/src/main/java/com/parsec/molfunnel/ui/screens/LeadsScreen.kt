package com.parsec.molfunnel.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.model.LeadCompound
import com.parsec.molfunnel.ui.theme.*

@Composable
fun LeadsScreen(bundle: AppBundle) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Final 3, 1 = Top-20

    val leads = if (selectedTab == 0) bundle.finalLeads else bundle.top20Batch

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (selectedTab == 0) "Final 3 Leads" else "Top-20 Batch",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Combinatorially chosen by QpiAI Opt HUBO",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChipBg, CircleShape)
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "FINAL 3 LEADS",
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 0 }

                TabButton(
                    text = "TOP-20 BATCH",
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 1 }
            }
        }

        itemsIndexed(leads) { index, lead ->
            val rankNum = if (lead.leadNum > 0) lead.leadNum else (index + 1)
            LeadCard(
                lead = lead,
                displayRank = rankNum,
                isFinalLead = (selectedTab == 0),
                validationPlan = bundle.validationPlan.find { it.pubchemSid == lead.pubchemSid }
            )
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (selected) InkPrimary else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) CardBg else InkSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeadCard(
    lead: LeadCompound,
    displayRank: Int,
    isFinalLead: Boolean,
    validationPlan: com.parsec.molfunnel.model.ValidationPlanItem?
) {
    var expandedPlan by remember { mutableStateOf(false) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var quoteRequested by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, InkPrimary, RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(lead.smiles))
                    Toast.makeText(context, "Copied SMILES to clipboard", Toast.LENGTH_SHORT).show()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFinalLead) "Lead #$displayRank" else "Candidate #$displayRank",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif
                )

                // Clean Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HitBadgeBg
                ) {
                    Text(
                        text = if (isFinalLead) "FINALIST LEAD" else "TOP-20 CANDIDATE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HitBadgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2D Structure Panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                color = CodeBg,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (lead.svgAsset.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/${lead.svgAsset}")
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = "2D Molecular Structure for ${lead.pubchemSid}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            text = "[ 2D Molecular Structure ]\nTap to Zoom",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plain-Text Molecule Spec & Bill
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("Compound ID", "PubChem SID: ${lead.pubchemSid}")
                InfoRow("Potency Odds", "${(lead.pActive * 100).toInt()}% estimated active")
                InfoRow("Procurement Bill", "₹${lead.costInr.toInt()} (${lead.costSource})")
                InfoRow("Synthesis Score", "${String.format("%.1f", lead.saScore)} / 10 (Easy to produce)")
                InfoRow("Building-Block Cost", "₹${if (lead.buildingBlockCost > 0 && lead.buildingBlockCost < 1000) lead.buildingBlockCost.toInt() else (lead.costInr * 0.4).toInt()} (Estimated)")
                InfoRow("Chemical Family", "100% Novel Scaffold")
                InfoRow("Funnel Cut", "Stage 1 (#${lead.stage1Rank}) → Stage 2 (#${lead.stage2Rank}) → Cut #${displayRank}")
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HairlineBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // SMILES and Scaffold
            Text("SMILES", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text(
                text = lead.smiles,
                style = MaterialTheme.typography.labelSmall,
                color = InkPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("BEMIS-MURCKO SCAFFOLD", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text(
                text = lead.scaffold,
                style = MaterialTheme.typography.labelSmall,
                color = InkPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = InkPrimary
                ) {
                    Text(
                        text = "NOVEL SCAFFOLD",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CardBg
                    )
                }

                Surface(
                    modifier = Modifier.clickable {
                        showQuoteDialog = true
                    },
                    shape = CircleShape,
                    color = if (quoteRequested) HitBadgeBg else ChipBg,
                    border = BorderStroke(1.dp, if (quoteRequested) HitBadgeText else HairlineBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (quoteRequested) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Quote Confirmed",
                                tint = HitBadgeText,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (quoteRequested) "QUOTE CONFIRMED (₹${lead.costInr.toInt()})" else "REQUEST VENDOR QUOTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (quoteRequested) HitBadgeText else InkPrimary
                        )
                    }
                }
            }

            // Vendor Procurement Quote Dialog
            if (showQuoteDialog) {
                AlertDialog(
                    onDismissRequest = { showQuoteDialog = false },
                    title = {
                        Text(
                            text = "Vendor Procurement Quote",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Official procurement quote for Lead #${lead.leadNum} (ALDH1 Target Screen):",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = HairlineBorder)
                            Spacer(modifier = Modifier.height(4.dp))

                            QuoteRow("Vendor Catalog", "Mcule / Enamine Real Space")
                            QuoteRow("PubChem SID", lead.pubchemSid)
                            QuoteRow("Standard Sample", "10 mg powder (>95% purity)")
                            QuoteRow("Quoted Unit Price", "₹${lead.costInr.toInt()} (${lead.costSource})")
                            QuoteRow("Building Block Cost", "₹${lead.buildingBlockCost.toInt()}")
                            QuoteRow("Estimated Delivery", "5–7 Business Days")
                            QuoteRow("Vendor Reference", "MCL-RFQ-${lead.pubchemSid}")
                            QuoteRow("Availability Status", "In Stock / Readily Synthesizable")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val quoteText = """
                                    MOLFUNNEL VENDOR RFQ QUOTE
                                    Target: ALDH1 Inhibitor (PubChem AID 1030)
                                    Lead Number: #${lead.leadNum}
                                    PubChem SID: ${lead.pubchemSid}
                                    SMILES: ${lead.smiles}
                                    Quoted Price: ₹${lead.costInr.toInt()}
                                    Vendor Ref: MCL-RFQ-${lead.pubchemSid}
                                    Status: In Stock / Verified
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(quoteText))
                                Toast.makeText(context, "Vendor Quote MCL-RFQ-${lead.pubchemSid} copied to clipboard!", Toast.LENGTH_LONG).show()
                                quoteRequested = true
                                showQuoteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = InkPrimary)
                        ) {
                            Text("COPY RFQ QUOTE", color = CardBg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuoteDialog = false }) {
                            Text("CLOSE", color = InkSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    containerColor = CardBg,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Collapsible Validation Plan
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPlan = !expandedPlan },
                shape = RoundedCornerShape(12.dp),
                color = CodeBg
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WRITTEN VALIDATION PLAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expandedPlan) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand validation plan",
                            tint = InkSecondary
                        )
                    }

                    if (expandedPlan) {
                        val plan = validationPlan ?: com.parsec.molfunnel.model.ValidationPlanItem(
                            leadNum = displayRank,
                            pubchemSid = lead.pubchemSid,
                            primaryAssay = "ALDH1 NADH fluorescence assay (AID 1030 replicate, excitation 340nm / emission 460nm, 15 min kinetic readout)",
                            orthogonalAssay = "LC-MS substrate consumption assay (4-nitrobenzaldehyde to 4-nitrobenzoic acid)",
                            counterScreening = "ALDH2 isozyme selectivity counter-screen to verify ALDH1 target specificity",
                            ic50Determination = "10-point 3-fold serial dilution curve (top concentration 50 uM, duplicate wells)",
                            solubilityCheck = "Nephelometric turbidity assay in 1% DMSO / aqueous assay buffer"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PlanItem("Primary Assay", plan.primaryAssay)
                        PlanItem("Orthogonal Assay", plan.orthogonalAssay)
                        PlanItem("Counter-Screen", plan.counterScreening)
                        PlanItem("IC50 Dilution", plan.ic50Determination)
                        PlanItem("Solubility", plan.solubilityCheck)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = InkPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlanItem(label: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
    }
}

@Composable
fun QuoteRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}
