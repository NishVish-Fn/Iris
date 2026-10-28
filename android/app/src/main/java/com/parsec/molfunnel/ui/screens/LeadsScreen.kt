package com.parsec.molfunnel.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    var isRevealed by remember { mutableStateOf(true) }

    val leads = if (selectedTab == 0) bundle.finalLeads else bundle.top20Batch

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // Reveal Toggle Chip
                FilterChip(
                    selected = isRevealed,
                    onClick = { isRevealed = !isRevealed },
                    label = { Text(if (isRevealed) "REVEALED" else "HIDDEN") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InkPrimary,
                        selectedLabelColor = CardBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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

        items(leads) { lead ->
            LeadCard(
                lead = lead,
                isRevealed = isRevealed,
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
    isRevealed: Boolean,
    validationPlan: com.parsec.molfunnel.model.ValidationPlanItem?
) {
    var expandedPlan by remember { mutableStateOf(false) }
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
                    text = "Lead #${lead.leadNum}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif
                )

                // Activity Status Badge (Image 2 style)
                if (isRevealed) {
                    val isHit = lead.trueLabel == 1
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHit) HitBadgeBg else NotHitBadgeBg
                    ) {
                        Text(
                            text = if (isHit) "HIT" else "NOT A HIT",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isHit) HitBadgeText else NotHitBadgeText
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ChipBg
                    ) {
                        Text(
                            text = "HIDDEN",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkSecondary
                        )
                    }
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

            // Monospace Metadata Table
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("PubChem SID", lead.pubchemSid)
                InfoRow("Predicted P(active)", "${String.format("%.4f", lead.pActive)} ± ${String.format("%.4f", lead.sigma)}")
                InfoRow("Acquisition Score", "${String.format("%.4f", lead.pActive + 0.5 * lead.sigma)} (v_i = ${lead.netValue.toInt()})")
                InfoRow("Compound Cost", "₹${lead.costInr.toInt()} (${lead.costSource})")
                InfoRow("Complexity", "${String.format("%.2f", lead.saScore)} / 10 (SAScore)")
                InfoRow("Building-Block Cost", "₹${lead.buildingBlockCost.toInt()} (Tier B, modelled)")
                InfoRow("Availability", lead.availability)
                InfoRow("Funnel Ranks", "Stage 1: #${lead.stage1Rank} | Stage 2: #${lead.stage2Rank}")
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = InkPrimary
                ) {
                    Text(
                        text = "NOVEL SCAFFOLD",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CardBg
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ChipBg
                ) {
                    Text(
                        text = "REQUEST VENDOR QUOTE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                }
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

                    if (expandedPlan && validationPlan != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PlanItem("Primary Assay", validationPlan.primaryAssay)
                        PlanItem("Orthogonal Assay", validationPlan.orthogonalAssay)
                        PlanItem("Counter-Screen", validationPlan.counterScreening)
                        PlanItem("IC50 Dilution", validationPlan.ic50Determination)
                        PlanItem("Solubility", validationPlan.solubilityCheck)
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
