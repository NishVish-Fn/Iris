package com.parsec.molfunnel.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.ui.theme.*

@Composable
fun HomeScreen(
    bundle: AppBundle,
    onNavigateToTab: (Int) -> Unit
) {
    var inWalkthrough by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) }

    val steps = listOf(
        Pair("1. The Problem", "ALDH1 inhibition (AID 1030). We must select high-value next experimental batch trading predicted activity against chemical novelty, diversity, assay cost and availability.") to 0,
        Pair("2. Raw Activity Data", "145,133 records downloaded from LIT-PCBA (Univ. of Strasbourg). Cleaned with RDKit: salt stripped, canonicalized, deduplicated by InChIKey. 23 conflict molecules removed.") to 0,
        Pair("3. Zero-Overlap Scaffold Split", "Bemis-Murcko scaffolds grouped into 3,000 Train and 10,000 Pool. Strictly disjoint: |Scaffolds(train) ∩ Scaffolds(pool)| = 0. Pool labels hidden until reveal.") to 0,
        Pair("4. The Quantum Kernel", "8 qubits, ZZ-feature map with ring entanglement and gamma bandwidth scaling. Built and executed via official QpiAI Quantum SDK (qpiai_quantum).") to 3,
        Pair("5. Stage 1 Classical Filter", "10,000 pool ranked by net value v_i with a per-scaffold cap of 5. Reduces pool to 1,000 diverse candidates while retaining active coverage.") to 0,
        Pair("6. Stage 2 QpiAI Opt QUBO", "Formulates QUBO with Tanimoto diversity penalty and exact cardinality constraint A*(sum x - 50)^2. Solved natively with QpiAI Opt LocalProvider.") to 0,
        Pair("7. Stage 3 Native HUBO", "50 survivors to final 3 using QpiAI Opt HUBO with cubic penalty term omega * x_i*x_j*x_l preventing more than 2 from the same scaffold. Validated against all 19,600 combinations.") to 1,
        Pair("8. The Reveal Rule", "Pool activity labels were unlocked only after selections were frozen to disk. Retrospective recall and enrichment calculated with zero data leakage.") to 3,
        Pair("9. Retrospective Enrichment", "Enrichment Factor (EF) and recall computed at every stage alongside hypergeometric p-values.") to 3,
        Pair("10. Scaffold Novelty", "100% of hit scaffolds in the selected batch are structurally distinct from the training set by construction.") to 1,
        Pair("11. Procurement Economics", "Cost model calibrated against Mcule catalog (CoPriNet GNN validation). Per-test cost: c_i = price_per_mmol * mmol + assay_fee.") to 1,
        Pair("12. Written Validation Plan", "Concrete experimental plan: primary NADH fluorescence assay, orthogonal LC-MS consumption, ALDH2 selectivity counter-screen, and 10-point IC50 dilution.") to 1
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!inWalkthrough) {
            // Main Hero Editorial Card (Image 1 style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MOLFUNNEL",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 3.sp,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Decorative Dotted Blob Canvas
                    Canvas(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(8.dp)
                    ) {
                        drawCircle(color = BlobDeep, radius = size.minDimension / 2.2f)
                        val step = 14f
                        for (x in 20..size.width.toInt() - 20 step step.toInt()) {
                            for (y in 20..size.height.toInt() - 20 step step.toInt()) {
                                val dx = x - size.width / 2
                                val dy = y - size.height / 2
                                if (dx * dx + dy * dy < (size.minDimension / 2.4f) * (size.minDimension / 2.4f)) {
                                    drawCircle(color = CardBg, radius = 2f, center = Offset(x.toFloat(), y.toFloat()))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "From 10,000 molecules to 3",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quant-A-Maze 3.0, QML-04 | Team Parsec\nALDH1 Inhibition (AID 1030)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { inWalkthrough = true },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = InkPrimary),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "START WALKTHROUGH",
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.5.sp,
                            color = CardBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onNavigateToTab(1) },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                    ) {
                        Text("VIEW FINAL 3 LEADS", style = MaterialTheme.typography.labelMedium, color = InkPrimary)
                    }
                }
            }
        } else {
            // Walkthrough Card
            val currentPair = steps[currentStep - 1]
            val stepTitle = currentPair.first.first
            val stepDesc = currentPair.first.second
            val targetTab = currentPair.second

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STEP $currentStep/12",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSecondary,
                            letterSpacing = 1.sp
                        )

                        TextButton(onClick = { inWalkthrough = false }) {
                            Text("CLOSE", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif,
                        color = InkPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stepDesc,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp,
                        color = InkPrimary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                if (currentStep > 1) currentStep--
                            },
                            enabled = currentStep > 1
                        ) {
                            Text("PREVIOUS", color = if (currentStep > 1) InkPrimary else Color.Transparent)
                        }

                        Button(
                            onClick = {
                                if (currentStep < 12) {
                                    currentStep++
                                } else {
                                    inWalkthrough = false
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = InkPrimary)
                        ) {
                            Text(
                                text = if (currentStep < 12) "NEXT STEP" else "FINISH",
                                color = CardBg,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            inWalkthrough = false
                            onNavigateToTab(targetTab)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Open in app →", style = MaterialTheme.typography.labelMedium, color = InkPrimary)
                    }
                }
            }
        }
    }
}
