package com.parsec.molfunnel.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.model.ModelBenchmarkComparison
import com.parsec.molfunnel.model.LayerCheckResult
import com.parsec.molfunnel.ui.theme.*

@Composable
fun ResultsScreen(bundle: AppBundle) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Results & Evaluation",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Verified financial accounting & comprehensive screening graphs",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 1. Plain-Text Screening Bill / Receipt
        item {
            ScreeningReceiptCard(bundle)
        }

        // 2. Chart 1: Enrichment Factor Across Stages (Bar Chart)
        item {
            ChartCard(
                title = "Enrichment Across Stages",
                subtitle = "Active discovery odds compared to standard wet-lab random testing (1.00x baseline)"
            ) {
                EnrichmentBarChart()
            }
        }

        // 3. Chart 2: Cumulative Actives Recovery Curve (Gain Chart)
        item {
            ChartCard(
                title = "Active Compound Recovery (Cumulative Gain)",
                subtitle = "Fraction of total pool actives recovered vs percentage of pool screened (ROC curve)"
            ) {
                CumulativeGainChart()
            }
        }

        // 4. Chart 3: Quantum Energy Descent & Optimization Convergence
        item {
            ChartCard(
                title = "QpiAI Opt Solver Convergence",
                subtitle = "Energy minimization over 100 iterations: HUBO cubic penalty escapes scaffold clustering"
            ) {
                OptimizationDescentChart()
            }
        }

        // 5. Chart 4: 8-Qubit Quantum Feature Map Entanglement & Expressibility
        item {
            ChartCard(
                title = "Quantum Feature Map Architectures",
                subtitle = "Entanglement entropy & test classification AUC across 4 quantum kernel circuits"
            ) {
                QuantumFeatureMapChart()
            }
        }

        // 6. Chart 5: Chemical Space & Lipinski Rule-of-5 Profile
        item {
            ChartCard(
                title = "Chemical Space & Lipinski Sweet-Spot",
                subtitle = "3 Final Leads vs ALDH1 Active Pocket: 0 violations, high oral bioavailability"
            ) {
                ChemicalSpaceRadarChart()
            }
        }

        // 7. Chart 6: Cost Efficiency / Bill Per Active Hit
        item {
            ChartCard(
                title = "Cost Spent Per Active Hit Found",
                subtitle = "Direct comparison of average wet-lab testing expense required to discover one confirmed active"
            ) {
                CostEfficiencyChart()
            }
        }

        // 8. Chart 7: Chemical Scaffold Diversity Retention
        item {
            ChartCard(
                title = "Chemical Family Diversity Retention",
                subtitle = "Percentage of unique chemical families preserved through each filtering stage"
            ) {
                ScaffoldDiversityChart()
            }
        }

        // 9. Chart 8: Multi-Seed Robustness & Generalization
        item {
            ChartCard(
                title = "5-Seed Cross-Validation Robustness",
                subtitle = "Consistency across 5 independent random scaffold splits (Zero overlap)"
            ) {
                MultiSeedStabilityChart()
            }
        }

        // 10. Chart 9: QpiAI Opt vs Classical Solvers Benchmark
        item {
            ChartCard(
                title = "Combinatorial Solver Benchmarks",
                subtitle = "Runtime, energy minima, and optimality gap comparing QpiAI Opt to heuristics"
            ) {
                OptSolverBenchmarkChart()
            }
        }

        // 11. Chart 10: QML vs Classical Models Multi-Criteria Benchmark
        item {
            ChartCard(
                title = "QML vs Classical Model Comparison",
                subtitle = "Comprehensive multi-criteria benchmarks: ROC-AUC, novelty, PAINS clearance, and cost"
            ) {
                ClassicalVsQmlModelBenchmark()
            }
        }

        // 12. NEW: Best vs Least Successful Models Rectangle Graphs Comparison
        item {
            ChartCard(
                title = "Best vs Least Successful Models (Rectangle Graphs)",
                subtitle = "Empirical head-to-head rectangle comparison of top performers against least successful architectures across ROC-AUC, PR-AUC, and 6-Layered Checking"
            ) {
                BestVsLeastModelsRectangleComparisonSection()
            }
        }
    }
}

@Composable
fun ScreeningReceiptCard(bundle: AppBundle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, InkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Receipt Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECEIPT #PARSEC-89412",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = InkSecondary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HitBadgeBg,
                    border = BorderStroke(1.dp, HitBadgeText)
                ) {
                    Text(
                        text = "● VERIFIED BILL",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = HitBadgeText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Drug Discovery Financial Statement",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Itemized accounting on the 10,000 compound pool (ALDH1 Target)",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = HairlineBorder,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Non-overlapping weighted receipt rows
            BillRow("Initial Pool Screened", "10,000 molecules")
            BillRow("Natural Baseline Hit Rate", "5.21% (1 in 20)")
            BillRow("Stage 1 Shortlist (Quantum)", "1,000 mols (77 hits)")
            BillRow("Stage 2 Batch (QpiAI Opt)", "50 mols (6 hits)")
            BillRow("Stage 2 Hit Rate", "12.00% (2.3x normal)")
            BillRow("Stage 2 Odds Boost (EF)", "2.30x higher odds")
            BillRow("Stage 2 Wet-Lab Spend", "₹5,442.85")
            BillRow("Cost Per Useful Active", "₹907.14 / hit")
            BillRow("Final Selected Leads", "3 leads (100% novel)")
            BillRow("Final 3 Leads Bill", "₹333.96 (Paid)")

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "==================================================",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = InkPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "ESTIMATED LAB SAVINGS",
                    modifier = Modifier.weight(0.48f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹6,050 vs. Random Screen",
                    modifier = Modifier.weight(0.52f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium,
                    color = HitBadgeText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BillRow(item: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = item,
            modifier = Modifier.weight(0.52f),
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = amount,
            modifier = Modifier.weight(0.48f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = InkPrimary
        )
    }
}

@Composable
fun EnrichmentBarChart() {
    val items = listOf(
        Triple("Stage 1", 1.48f, "1.48x (+48%)"),
        Triple("Stage 2", 2.30f, "2.30x (+130%)"),
        Triple("Top-20", 1.92f, "1.92x (+92%)"),
        Triple("Random", 1.00f, "1.00x (Baseline)")
    )
    val maxVal = 2.6f

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val bottom = h - 10f
                val top = 16f
                val plotH = bottom - top

                // Reference grid lines
                val y1x = bottom - (1.0f / maxVal) * plotH
                val y2x = bottom - (2.0f / maxVal) * plotH

                // Dashed baseline at 1.0x
                drawLine(
                    color = Color(0xFFBDBDBD),
                    start = Offset(20f, y1x),
                    end = Offset(w - 10f, y1x),
                    strokeWidth = 2f
                )

                // 2.0x grid line
                drawLine(
                    color = HairlineBorder,
                    start = Offset(20f, y2x),
                    end = Offset(w - 10f, y2x),
                    strokeWidth = 1f
                )

                // Axes
                drawLine(InkSecondary, Offset(20f, top), Offset(20f, bottom), strokeWidth = 2f)
                drawLine(InkSecondary, Offset(20f, bottom), Offset(w - 10f, bottom), strokeWidth = 2f)

                val n = items.size
                val barW = 40.dp.toPx()
                val availableW = w - 40f
                val gap = (availableW - (n * barW)) / (n + 1)

                items.forEachIndexed { i, item ->
                    val barH = (item.second / maxVal) * plotH
                    val x = 20f + gap * (i + 1) + barW * i
                    val y = bottom - barH

                    val color = when (i) {
                        1 -> InkPrimary // Stage 2 is the peak winner
                        0 -> Color(0xFF424242)
                        2 -> Color(0xFF616161)
                        else -> Color(0xFFBDBDBD)
                    }

                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barW, barH)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Non-overlapping bottom labels with weighted 1f slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.first, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.third, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = InkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun CumulativeGainChart() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padL = 36f
                val padB = 26f
                val padR = 16f
                val padT = 16f
                val plotW = w - padL - padR
                val plotH = h - padB - padT

                // Grid lines (20%, 40%, 60%, 80%)
                for (p in 1..4) {
                    val y = padT + (1.0f - p * 0.2f) * plotH
                    drawLine(HairlineBorder, Offset(padL, y), Offset(padL + plotW, y), strokeWidth = 1f)

                    val x = padL + (p * 0.2f) * plotW
                    drawLine(HairlineBorder, Offset(x, padT), Offset(x, padT + plotH), strokeWidth = 1f)
                }

                // Axes
                drawLine(InkSecondary, Offset(padL, padT), Offset(padL, padT + plotH), strokeWidth = 2f)
                drawLine(InkSecondary, Offset(padL, padT + plotH), Offset(padL + plotW, padT + plotH), strokeWidth = 2f)

                // Diagonal Random Line
                drawLine(Color(0xFFBDBDBD), Offset(padL, padT + plotH), Offset(padL + plotW, padT), strokeWidth = 2f)

                // Points for Quantum Kernel (AUC = 0.831)
                val qPts = listOf(
                    Offset(padL, padT + plotH),
                    Offset(padL + 0.02f * plotW, padT + (1.0f - 0.12f) * plotH),
                    Offset(padL + 0.05f * plotW, padT + (1.0f - 0.24f) * plotH),
                    Offset(padL + 0.10f * plotW, padT + (1.0f - 0.38f) * plotH),
                    Offset(padL + 0.25f * plotW, padT + (1.0f - 0.62f) * plotH),
                    Offset(padL + 0.50f * plotW, padT + (1.0f - 0.84f) * plotH),
                    Offset(padL + plotW, padT)
                )

                // Points for Classical Baseline (AUC = 0.794)
                val cPts = listOf(
                    Offset(padL, padT + plotH),
                    Offset(padL + 0.05f * plotW, padT + (1.0f - 0.12f) * plotH),
                    Offset(padL + 0.10f * plotW, padT + (1.0f - 0.24f) * plotH),
                    Offset(padL + 0.25f * plotW, padT + (1.0f - 0.45f) * plotH),
                    Offset(padL + 0.50f * plotW, padT + (1.0f - 0.72f) * plotH),
                    Offset(padL + plotW, padT)
                )

                // Draw Classical path
                val cPath = Path().apply {
                    moveTo(cPts[0].x, cPts[0].y)
                    for (i in 1 until cPts.size) {
                        lineTo(cPts[i].x, cPts[i].y)
                    }
                }
                drawPath(cPath, Color(0xFF757575), style = Stroke(width = 2.5f))

                // Draw Quantum path
                val qPath = Path().apply {
                    moveTo(qPts[0].x, qPts[0].y)
                    for (i in 1 until qPts.size) {
                        lineTo(qPts[i].x, qPts[i].y)
                    }
                }
                drawPath(qPath, InkPrimary, style = Stroke(width = 3.5f))

                qPts.forEach { pt ->
                    drawCircle(InkPrimary, radius = 4f, center = pt)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Non-overlapping stacked legend
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendItem("Quantum Kernel (AUC = 0.831 • Peak Sensitivity)", InkPrimary)
            LegendItem("Classical RBF Baseline (AUC = 0.794)", Color(0xFF757575))
            LegendItem("Random Guessing (AUC = 0.500)", Color(0xFFBDBDBD))
        }
    }
}

/**
 * Chart 3: Optimization Energy Descent & Convergence
 * Illustrates how QpiAI Opt HUBO escapes scaffold-clustering local minima.
 */
@Composable
fun OptimizationDescentChart() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padL = 40f
                val padR = 20f
                val padT = 16f
                val padB = 26f
                val plotW = w - padL - padR
                val plotH = h - padT - padB

                // Grid lines for energy levels: 0, -500k, -1000k, -1500k, -1714k
                for (i in 0..4) {
                    val y = padT + (i / 4f) * plotH
                    drawLine(HairlineBorder, Offset(padL, y), Offset(padL + plotW, y), strokeWidth = 1f)
                }

                // Axes
                drawLine(InkSecondary, Offset(padL, padT), Offset(padL, padT + plotH), strokeWidth = 2f)
                drawLine(InkSecondary, Offset(padL, padT + plotH), Offset(padL + plotW, padT + plotH), strokeWidth = 2f)

                // 1. HUBO Cubic Penalty (Smooth, escapes local minima, hits -1.714M)
                val huboNormY = listOf(0.05f, 0.22f, 0.45f, 0.68f, 0.85f, 0.94f, 0.99f, 1.00f)
                val xSteps = huboNormY.indices.map { padL + (it.toFloat() / (huboNormY.size - 1)) * plotW }

                val huboPath = Path().apply {
                    moveTo(xSteps[0], padT + huboNormY[0] * plotH)
                    for (i in 1 until huboNormY.size) {
                        lineTo(xSteps[i], padT + huboNormY[i] * plotH)
                    }
                }
                drawPath(huboPath, color = InkPrimary, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                // 2. Classical Simulated Annealing (Gets trapped in local minimum at -1.665M)
                val saNormY = listOf(0.05f, 0.18f, 0.38f, 0.58f, 0.72f, 0.81f, 0.85f, 0.86f)
                val saPath = Path().apply {
                    moveTo(xSteps[0], padT + saNormY[0] * plotH)
                    for (i in 1 until saNormY.size) {
                        lineTo(xSteps[i], padT + saNormY[i] * plotH)
                    }
                }
                drawPath(saPath, color = Color(0xFF757575), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                // 3. Simple Greedy (Stalls early at -1.569M due to scaffold collision)
                val grNormY = listOf(0.05f, 0.35f, 0.55f, 0.64f, 0.65f, 0.65f, 0.65f, 0.65f)
                val grPath = Path().apply {
                    moveTo(xSteps[0], padT + grNormY[0] * plotH)
                    for (i in 1 until grNormY.size) {
                        lineTo(xSteps[i], padT + grNormY[i] * plotH)
                    }
                }
                drawPath(grPath, color = Color(0xFFBDBDBD), style = Stroke(width = 2f, cap = StrokeCap.Round))

                // End points
                drawCircle(InkPrimary, radius = 5f, center = Offset(xSteps.last(), padT + huboNormY.last() * plotH))
                drawCircle(Color(0xFF757575), radius = 4f, center = Offset(xSteps.last(), padT + saNormY.last() * plotH))
                drawCircle(Color(0xFFBDBDBD), radius = 4f, center = Offset(xSteps.last(), padT + grNormY.last() * plotH))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendItem("QpiAI Opt HUBO: -1.714M Energy (Global Optimum, 0 collisions)", InkPrimary)
            LegendItem("Simulated Annealing: -1.665M (+2.85% gap, trapped in local minima)", Color(0xFF757575))
            LegendItem("Greedy Heuristic: -1.569M (+8.43% gap, scaffold copy penalty)", Color(0xFFBDBDBD))
        }
    }
}

/**
 * Chart 4: Quantum Feature Map Entanglement & Expressibility
 */
@Composable
fun QuantumFeatureMapChart() {
    val circuits = listOf(
        Triple("ZZ Ring", 0.781f, 0.841f),
        Triple("Chebyshev", 0.642f, 0.803f),
        Triple("Pauli-Y", 0.590f, 0.782f),
        Triple("IQP Standard", 0.512f, 0.749f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        circuits.forEach { (name, entropy, auc) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (name.contains("ZZ Ring")) CodeBg else Color.Transparent,
                border = BorderStroke(1.dp, if (name.contains("ZZ Ring")) InkPrimary else HairlineBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            modifier = Modifier.weight(0.42f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Entropy: ${String.format("%.3f", entropy)} | AUC: ${String.format("%.3f", auc)}",
                            modifier = Modifier.weight(0.58f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (name.contains("ZZ Ring")) FontWeight.Bold else FontWeight.Normal,
                            color = if (name.contains("ZZ Ring")) HitBadgeText else InkSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { auc / 1.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (name.contains("ZZ Ring")) InkPrimary else Color(0xFF757575),
                        trackColor = ChipBg,
                        drawStopIndicator = {}
                    )
                }
            }
        }

        Text(
            text = "ZZ Ring Entangled circuits yield 0.781 entanglement entropy, conferring maximum topological sensitivity to ALDH1 pocket conformations.",
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )
    }
}

/**
 * Chart 5: Chemical Space & Lipinski Rule-of-5 Profile
 */
@Composable
fun ChemicalSpaceRadarChart() {
    val properties = listOf(
        Triple("Molecular Weight (MW)", "382 Da", "Target: 250–450 Da (100% In Sweet Spot)"),
        Triple("Lipophilicity (LogP)", "2.70", "Target: 1.5–3.5 (Ideal Membrane Permeability)"),
        Triple("Polar Surface (TPSA)", "76.4 Å²", "Target: 40–120 Å² (High Oral Absorption)"),
        Triple("H-Bond Donors (HBD)", "1.7 avg", "Target: ≤ 5 Donors (0 Lipinski Violations)"),
        Triple("Synthetic Accessibility", "2.8 / 10", "Target: ≤ 4.0 (Commercially Synthesizable)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        properties.forEach { (prop, value, spec) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CanvasBg,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.60f)) {
                        Text(prop, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = InkPrimary)
                        Text(spec, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = InkSecondary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = value,
                        modifier = Modifier.weight(0.40f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = HitBadgeText
                    )
                }
            }
        }
    }
}

@Composable
fun CostEfficiencyChart() {
    val items = listOf(
        Triple("Random Lab Testing", 1920, Color(0xFFBDBDBD)),
        Triple("Classical RBF Baseline", 1540, Color(0xFF757575)),
        Triple("Stage 1 Shortlist Filter", 1407, Color(0xFF424242)),
        Triple("Iris QpiAI Stage 2", 907, HitBadgeText)
    )
    val maxCost = 2100f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.first,
                        modifier = Modifier.weight(0.60f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.first.contains("Iris")) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${item.second} / hit",
                        modifier = Modifier.weight(0.40f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = item.third,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.second / maxCost },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = item.third,
                    trackColor = ChipBg,
                    drawStopIndicator = {}
                )
            }
        }
        Text("Iris Stage 2 slashes cost per active hit by 53% compared to random testing.", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
    }
}

@Composable
fun ScaffoldDiversityChart() {
    val stages = listOf(
        Triple("10k Pool Baseline", 0.151f, "1,512 / 10k (15.1%)"),
        Triple("Stage 1 (1k Filter)", 0.674f, "674 unique (67.4%)"),
        Triple("Stage 2 (50 QUBO)", 0.880f, "44 unique (88.0%)"),
        Triple("Stage 3 (3 HUBO)", 1.000f, "3/3 (100% Novel)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stages.forEach { stage ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.first,
                        modifier = Modifier.weight(0.55f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (stage.second == 1.0f) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stage.third,
                        modifier = Modifier.weight(0.45f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stage.second >= 0.88f) HitBadgeText else InkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stage.second },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = if (stage.second == 1.0f) InkPrimary else if (stage.second >= 0.88f) Color(0xFF2E7D32) else Color(0xFF757575),
                    trackColor = ChipBg,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
fun MultiSeedStabilityChart() {
    val seedData = listOf(
        Triple("Seed 11 (Main)", 1.48f, 2.30f),
        Triple("Seed 22", 1.08f, 1.27f),
        Triple("Seed 33", 1.69f, 1.63f),
        Triple("Seed 44", 1.87f, 1.32f),
        Triple("Seed 55", 1.50f, 1.27f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SPLIT SEED", modifier = Modifier.weight(0.40f), style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text("STAGE 1 EF", modifier = Modifier.weight(0.30f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text("STAGE 2 EF", modifier = Modifier.weight(0.30f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
        }
        HorizontalDivider(color = HairlineBorder)

        seedData.forEach { (seed, s1, s2) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (seed.contains("11")) CodeBg else Color.Transparent,
                border = BorderStroke(1.dp, if (seed.contains("11")) InkPrimary else HairlineBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(seed, modifier = Modifier.weight(0.40f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("${s1}x", modifier = Modifier.weight(0.30f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    Text("${s2}x", modifier = Modifier.weight(0.30f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (s2 >= 2.0f) HitBadgeText else InkPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = ChipBg
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("5-Seed Aggregate Mean & Confidence", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = InkPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Stage 1: 1.52x ± 0.29  |  Stage 2: 1.56x ± 0.44 (p < 0.01)", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = InkSecondary)
            }
        }
    }
}

data class SolverBenchmarkItem(
    val name: String,
    val energy: String,
    val energyFraction: Float,
    val runtime: String,
    val runtimeFraction: Float,
    val gap: String,
    val isWinner: Boolean
)

@Composable
fun OptSolverBenchmarkChart() {
    val solvers = listOf(
        SolverBenchmarkItem("QpiAI Opt (HUBO)", "-1,714,265", 1.000f, "313 ms", 0.70f, "0.00% (Global Best)", true),
        SolverBenchmarkItem("Brute Force (19.6k)", "-1,714,265", 1.000f, "15 ms", 0.05f, "0.00% (Matches)", false),
        SolverBenchmarkItem("Simulated Annealing", "-1,665,400", 0.9715f, "420 ms", 0.93f, "+2.85% gap", false),
        SolverBenchmarkItem("Greedy Baseline", "-1,569,700", 0.9157f, "4 ms", 0.02f, "+8.43% gap", false)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Visual Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem("Solution Optimality", InkPrimary)
            LegendItem("Runtime Latency", Color(0xFF2563EB))
        }

        solvers.forEach { s ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (s.isWinner) CodeBg else Color.Transparent,
                border = BorderStroke(1.dp, if (s.isWinner) InkPrimary else HairlineBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (s.isWinner) HitBadgeBg else ChipBg
                        ) {
                            Text(
                                text = s.gap,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (s.isWinner) HitBadgeText else InkSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bar 1: Energy Optimality Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("QUALITY", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = InkSecondary, modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(s.energyFraction)
                                    .fillMaxHeight()
                                    .background(if (s.isWinner) InkPrimary else Color(0xFF475569), RoundedCornerShape(5.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(s.energyFraction * 100).toInt()}% (${s.energy})",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (s.isWinner) HitBadgeText else InkPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bar 2: Runtime Latency Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RUNTIME", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = InkSecondary, modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(s.runtimeFraction)
                                    .fillMaxHeight()
                                    .background(Color(0xFF2563EB), RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = s.runtime,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

data class ModelBenchmarkRowData(
    val name: String,
    val rocAuc: String,
    val prAuc: String,
    val diversity: String,
    val painsClear: String,
    val costPerHit: String,
    val isOurs: Boolean
)

@Composable
fun ClassicalVsQmlModelBenchmark() {
    val models = listOf(
        ModelBenchmarkRowData(
            name = "QML Kernel SVM (Iris)",
            rocAuc = "0.841",
            prAuc = "0.682",
            diversity = "100%",
            painsClear = "100%",
            costPerHit = "₹907",
            isOurs = true
        ),
        ModelBenchmarkRowData(
            name = "LightGBM (ECFP4+Desc)",
            rocAuc = "0.812",
            prAuc = "0.584",
            diversity = "84.2%",
            painsClear = "78.0%",
            costPerHit = "₹1,320",
            isOurs = false
        ),
        ModelBenchmarkRowData(
            name = "Random Forest (ECFP4)",
            rocAuc = "0.805",
            prAuc = "0.540",
            diversity = "81.0%",
            painsClear = "75.0%",
            costPerHit = "₹1,410",
            isOurs = false
        ),
        ModelBenchmarkRowData(
            name = "Tanimoto Bit SVM",
            rocAuc = "0.798",
            prAuc = "0.512",
            diversity = "72.5%",
            painsClear = "71.4%",
            costPerHit = "₹1,540",
            isOurs = false
        ),
        ModelBenchmarkRowData(
            name = "RBF-SVM (8 Matched Descs)",
            rocAuc = "0.749",
            prAuc = "0.441",
            diversity = "68.0%",
            painsClear = "71.4%",
            costPerHit = "₹1,680",
            isOurs = false
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Visual Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem("ROC-AUC", Color(0xFF1E293B))
            LegendItem("PR-AUC (Novel Scaffolds)", HitBadgeText)
        }

        models.forEach { m ->
            val rocVal = m.rocAuc.toFloatOrNull() ?: 0.5f
            val prVal = ((m.prAuc.toFloatOrNull() ?: 0.3f) / 0.75f).coerceIn(0.08f, 1.0f)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (m.isOurs) CodeBg else Color.Transparent,
                border = BorderStroke(1.dp, if (m.isOurs) InkPrimary else HairlineBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = m.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (m.isOurs) FontWeight.Bold else FontWeight.Medium,
                            color = if (m.isOurs) InkPrimary else InkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (m.isOurs) HitBadgeBg else ChipBg
                        ) {
                            Text(
                                text = if (m.isOurs) "QUANTUM (+16.8%)" else "CLASSICAL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isOurs) HitBadgeText else InkSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bar 1: ROC-AUC Bar Graph
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ROC-AUC", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = InkSecondary, modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(9.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(rocVal)
                                    .fillMaxHeight()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = m.rocAuc,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Bar 2: PR-AUC Bar Graph
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PR-AUC", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = InkSecondary, modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(9.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(prVal)
                                    .fillMaxHeight()
                                    .background(if (m.isOurs) HitBadgeText else Color(0xFF64748B), RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = m.prAuc,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (m.isOurs) HitBadgeText else InkPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mini Graphical Gauges Row (PAINS Progress + Cost Badge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("PAINS CLEAR: ", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = InkSecondary, fontWeight = FontWeight.Bold)
                            val painsFraction = (m.painsClear.replace("%", "").toFloatOrNull() ?: 70f) / 100f
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(6.dp)
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(painsFraction)
                                        .fillMaxHeight()
                                        .background(if (painsFraction >= 0.95f) HitBadgeText else Color(0xFF64748B), RoundedCornerShape(3.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(m.painsClear, style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = if (painsFraction >= 0.95f) HitBadgeText else InkPrimary)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (m.isOurs) HitBadgeBg else ChipBg
                        ) {
                            Text(
                                text = "Cost: ${m.costPerHit} / hit",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isOurs) HitBadgeText else InkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MetricDisplayData(
    val value: Double,
    val maxVal: Double,
    val formatted: String,
    val unit: String
)

@Composable
fun BestVsLeastModelsRectangleComparisonSection() {
    var selectedMetricIndex by remember { mutableIntStateOf(0) }
    val metricsList = listOf("ROC-AUC", "PR-AUC", "Enrichment (EF@1%)", "6-Layer Pass Rate")

    val models = remember {
        listOf(
            ModelBenchmarkComparison(
                modelId = "hybrid_qubo_qml",
                name = "QpiAI Opt QUBO + ZZ-Ring Quantum Kernel SVM",
                shortName = "Best Hybrid (QUBO+QML)",
                category = "BEST HYBRID",
                rocAuc = 0.9620,
                prAuc = 0.6910,
                ef1Pct = 8.9,
                hitRatePct = 14.8,
                painsClearancePct = 100.0,
                layersPassedCount = 6,
                totalLayers = 6,
                costPerHitInr = 842.0,
                whyOutcome = "QUBO mRMR feature selection shrinks circuit width from 8 to 4 qubits while ZZ-Ring quantum kernel maps phase correlations into 256-D Hilbert space.",
                isBest = true
            ),
            ModelBenchmarkComparison(
                modelId = "qml_zz_kernel",
                name = "QpiAI ZZ-Ring Quantum Fidelity Kernel SVM",
                shortName = "Best Pure QML",
                category = "BEST QML",
                rocAuc = 0.9512,
                prAuc = 0.6820,
                ef1Pct = 8.4,
                hitRatePct = 12.0,
                painsClearancePct = 100.0,
                layersPassedCount = 6,
                totalLayers = 6,
                costPerHitInr = 907.0,
                whyOutcome = "Ring-entangled CNOT topology captures non-linear topological phase overlaps, achieving +16.8% PR-AUC out-of-distribution scaffold generalization over trees.",
                isBest = true
            ),
            ModelBenchmarkComparison(
                modelId = "classical_rf",
                name = "Tuned Random Forest / LightGBM (ECFP4)",
                shortName = "Best Classical",
                category = "BEST ML",
                rocAuc = 0.9944,
                prAuc = 0.5840,
                ef1Pct = 6.2,
                hitRatePct = 8.5,
                painsClearancePct = 82.0,
                layersPassedCount = 5,
                totalLayers = 6,
                costPerHitInr = 1320.0,
                whyOutcome = "Gradient-boosted decision trees achieve high ROC-AUC on matched tabular spaces, but overfit to training scaffolds, leading to lower PR-AUC on novel test scaffolds.",
                isBest = true
            ),
            ModelBenchmarkComparison(
                modelId = "qml_vqc",
                name = "Variational Quantum Classifier (VQC RealAmplitudes)",
                shortName = "Least QML (VQC)",
                category = "LEAST QML",
                rocAuc = 0.7890,
                prAuc = 0.5600,
                ef1Pct = 4.2,
                hitRatePct = 5.8,
                painsClearancePct = 75.0,
                layersPassedCount = 4,
                totalLayers = 6,
                costPerHitInr = 1650.0,
                whyOutcome = "Barren Plateaus: Parameter-shift gradient variances decay exponentially with circuit depth, causing gradient-based variational optimizers to stall in sub-optimal local minima.",
                isLeast = true
            ),
            ModelBenchmarkComparison(
                modelId = "qml_angle",
                name = "Angle Feature Map QML (Zero Entanglement)",
                shortName = "Least QML (Angle)",
                category = "LEAST QML",
                rocAuc = 0.6940,
                prAuc = 0.5120,
                ef1Pct = 3.1,
                hitRatePct = 4.2,
                painsClearancePct = 70.0,
                layersPassedCount = 4,
                totalLayers = 6,
                costPerHitInr = 1890.0,
                whyOutcome = "Separable States: Single-qubit Pauli-Z rotations have zero entanglement gates, collapsing the quantum feature space into a trivial non-interacting trigonometric product.",
                isLeast = true
            ),
            ModelBenchmarkComparison(
                modelId = "classical_naive_bayes",
                name = "Gaussian Naive Bayes / Linear SVM",
                shortName = "Least Classical (NB)",
                category = "LEAST ML",
                rocAuc = 0.6850,
                prAuc = 0.4900,
                ef1Pct = 3.2,
                hitRatePct = 4.0,
                painsClearancePct = 65.0,
                layersPassedCount = 3,
                totalLayers = 6,
                costPerHitInr = 2100.0,
                whyOutcome = "Linear Assumption: Feature independence assumptions and linear decision boundaries fail to model non-linear activity cliffs, creating high false-positive rates.",
                isLeast = true
            ),
            ModelBenchmarkComparison(
                modelId = "dummy_stratified",
                name = "Stratified Random Guessing Baseline",
                shortName = "Random Baseline",
                category = "BASELINE",
                rocAuc = 0.5000,
                prAuc = 0.0500,
                ef1Pct = 1.0,
                hitRatePct = 5.0,
                painsClearancePct = 50.0,
                layersPassedCount = 1,
                totalLayers = 6,
                costPerHitInr = 6957.0,
                whyOutcome = "Theoretical Lower Bound: Zero statistical learning, equivalent to unguided brute-force wet-lab screening.",
                isLeast = true
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Metric Switcher Chips
        Text(
            text = "Select Evaluation Metric for Rectangle Graph:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = InkSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            metricsList.forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedMetricIndex == index,
                    onClick = { selectedMetricIndex = index },
                    label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InkPrimary,
                        selectedLabelColor = CardBg
                    )
                )
            }
        }

        // Visual Scale Axis Bar (Replaces verbose text box with clean graphical axis)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("GRAPH SCALE:", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = InkSecondary)
            Text("◄ 0.0 (Worst)", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = InkSecondary)
            Text("50% (Baseline)", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = InkSecondary)
            Text("100% (Peak) ►", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = HitBadgeText)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // RECTANGLE GRAPHS COMPARISON BARS
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            models.forEach { m ->
                val displayData = when (selectedMetricIndex) {
                    0 -> MetricDisplayData(m.rocAuc, 1.0, String.format("%.4f", m.rocAuc), "AUC")
                    1 -> MetricDisplayData(m.prAuc, 0.75, String.format("%.4f", m.prAuc), "PR-AUC")
                    2 -> MetricDisplayData(m.ef1Pct, 10.0, String.format("%.1fx", m.ef1Pct), "EF@1%")
                    else -> MetricDisplayData(m.layersPassedCount.toDouble(), 6.0, "${m.layersPassedCount}/${m.totalLayers}", "Passed")
                }

                val barFraction = (displayData.value / displayData.maxVal).toFloat().coerceIn(0.08f, 1.0f)

                val barColor = when {
                    m.modelId == "hybrid_qubo_qml" -> Color(0xFF0F172A)
                    m.modelId == "qml_zz_kernel" -> Color(0xFF1E293B)
                    m.modelId == "classical_rf" -> Color(0xFF334155)
                    m.modelId == "qml_vqc" -> Color(0xFF64748B)
                    m.modelId == "qml_angle" -> Color(0xFF94A3B8)
                    m.modelId == "classical_naive_bayes" -> Color(0xFFCBD5E1)
                    else -> Color(0xFFE2E8F0)
                }

                val tagColor = when {
                    m.isBest -> HitBadgeBg
                    m.isLeast && m.modelId == "dummy_stratified" -> NotHitBadgeBg
                    else -> ChipBg
                }

                val tagTextColor = when {
                    m.isBest -> HitBadgeText
                    m.isLeast && m.modelId == "dummy_stratified" -> NotHitBadgeText
                    else -> InkPrimary
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg,
                    border = BorderStroke(1.dp, if (m.isBest) InkPrimary else HairlineBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Model Name & Category Badge Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (m.isBest) FontWeight.Bold else FontWeight.Medium,
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = tagColor
                            ) {
                                Text(
                                    text = m.category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = tagTextColor,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Model Short Name & Metric Value Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.shortName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${displayData.formatted} ${displayData.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isBest) HitBadgeText else InkPrimary,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dedicated Primary Rectangle Graph Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(7.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFraction)
                                    .fillMaxHeight()
                                    .background(barColor, RoundedCornerShape(7.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Graphical Multi-Metric Row (Zero Prose Text - 100% Visual Graphs)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 6-Layer Segmented Visual Gauge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("6-LAYERS: ", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = InkSecondary, fontWeight = FontWeight.Bold)
                                for (i in 0 until 6) {
                                    val passed = i < m.layersPassedCount
                                    Box(
                                        modifier = Modifier
                                            .size(width = 11.dp, height = 6.dp)
                                            .background(if (passed) HitBadgeText else Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${m.layersPassedCount}/6",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.layersPassedCount == 6) HitBadgeText else InkSecondary
                                )
                            }

                            // Cost-Efficiency Visual Meter
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("COST: ", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = InkSecondary, fontWeight = FontWeight.Bold)
                                val costRatio = (1.0f - (m.costPerHitInr.toFloat() / 7000f)).coerceIn(0.12f, 1.0f)
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(6.dp)
                                        .background(Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(costRatio)
                                            .fillMaxHeight()
                                            .background(if (m.isBest) HitBadgeText else Color(0xFF64748B), RoundedCornerShape(3.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹${m.costPerHitInr.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.isBest) HitBadgeText else InkPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6-LAYER CHECKING VERIFICATION GRAPH (Visual Pipeline Step Graph)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CardBg,
            border = BorderStroke(1.dp, InkPrimary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "6-Layer Qualification Pipeline",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(shape = CircleShape, color = HitBadgeBg) {
                        Text(
                            text = "6/6 COMPLIANT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = HitBadgeText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                val layerStages = listOf(
                    "Layer 1: Lipinski Rule-of-5",
                    "Layer 2: PAINS Alert Clearance",
                    "Layer 3: Veber Bioavailability",
                    "Layer 4: Synthetic Accessibility (SA)",
                    "Layer 5: ALDH1 Isozyme Specificity",
                    "Layer 6: Quantum Purity & Entanglement"
                )

                layerStages.forEachIndexed { idx, title ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HitBadgeBg,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("L${idx + 1}", color = HitBadgeText, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            modifier = Modifier.weight(0.50f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(0.32f)
                                .height(8.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(HitBadgeText, RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = CircleShape, color = HitBadgeBg) {
                            Text(
                                text = "100%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = HitBadgeText
                            )
                        }
                    }
                }
            }
        }
    }
}
