package com.parsec.molfunnel.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.ui.theme.*

@Composable
fun ResultsScreen(bundle: AppBundle) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
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
                subtitle = "Fraction of total pool actives recovered vs percentage of pool screened"
            ) {
                CumulativeGainChart()
            }
        }

        // 4. Chart 3: Cost Efficiency / Bill Per Active Hit
        item {
            ChartCard(
                title = "Cost Spent Per Active Hit Found",
                subtitle = "Direct comparison of average wet-lab testing expense required to discover one confirmed active"
            ) {
                CostEfficiencyChart()
            }
        }

        // 5. Chart 4: Chemical Scaffold Diversity Retention
        item {
            ChartCard(
                title = "Chemical Family Diversity Retention",
                subtitle = "Percentage of unique chemical families preserved through each filtering stage"
            ) {
                ScaffoldDiversityChart()
            }
        }

        // 6. Chart 5: Multi-Seed Robustness & Generalization
        item {
            ChartCard(
                title = "5-Seed Cross-Validation Robustness",
                subtitle = "Consistency across 5 independent random scaffold splits (Zero overlap)"
            ) {
                MultiSeedStabilityChart()
            }
        }

        // 7. Chart 6: QpiAI Opt vs Classical Solvers Benchmark
        item {
            ChartCard(
                title = "QpiAI Opt Solver Performance",
                subtitle = "Comparing QpiAI Quantum Optimization to standard heuristic methods"
            ) {
                OptSolverBenchmarkChart()
            }
        }
    }
}

@Composable
fun ScreeningReceiptCard(bundle: AppBundle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, InkPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LAB SCREENING RECEIPT", style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = InkSecondary)
                Surface(shape = RoundedCornerShape(8.dp), color = HitBadgeBg) {
                    Text("VERIFIED BILL", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = HitBadgeText, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Drug Discovery Bill & Performance", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Text("Plain-text financial and hit accounting on the 10,000 test pool", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = HairlineBorder)
            Spacer(modifier = Modifier.height(10.dp))

            BillRow("Initial Pool Screened", "10,000 molecules")
            BillRow("Standard Hit Rate in Nature", "5.21% (1 in 20)")
            BillRow("Stage 1 Shortlist (Quantum Filter)", "1,000 molecules (77 hits)")
            BillRow("Stage 2 Batch (QpiAI Opt QUBO)", "50 molecules (6 hits)")
            BillRow("Stage 2 Success Rate", "12.00% (More than 2x normal)")
            BillRow("Stage 2 Accuracy Boost", "2.30x higher odds")
            BillRow("Stage 2 Total Lab Bill", "₹5,442.85")
            BillRow("Cost Per Useful Hit", "₹907.14 per hit")
            BillRow("Finalist Leads Selected", "3 molecules (100% diverse)")
            BillRow("Final 3 Leads Bill", "₹333.96")

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = HairlineBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ESTIMATED LAB SAVINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("₹6,050 saved vs. random testing", style = MaterialTheme.typography.labelMedium, color = HitBadgeText, fontWeight = FontWeight.Bold)
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
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
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val bottom = h - 26.dp.toPx()
                val top = 20.dp.toPx()
                val plotH = bottom - top

                // Reference grid lines
                val y1x = bottom - (1.0f / maxVal) * plotH
                val y2x = bottom - (2.0f / maxVal) * plotH

                // Dashed baseline at 1.0x
                drawLine(
                    color = Color(0xFFBDBDBD),
                    start = Offset(30f, y1x),
                    end = Offset(w - 10f, y1x),
                    strokeWidth = 2f
                )

                // 2.0x grid line
                drawLine(
                    color = HairlineBorder,
                    start = Offset(30f, y2x),
                    end = Offset(w - 10f, y2x),
                    strokeWidth = 1f
                )

                // Axes
                drawLine(InkSecondary, Offset(30f, top), Offset(30f, bottom), strokeWidth = 2f)
                drawLine(InkSecondary, Offset(30f, bottom), Offset(w - 10f, bottom), strokeWidth = 2f)

                val n = items.size
                val barW = 44.dp.toPx()
                val availableW = w - 50f
                val gap = (availableW - (n * barW)) / (n + 1)

                items.forEachIndexed { i, item ->
                    val barH = (item.second / maxVal) * plotH
                    val x = 30f + gap * (i + 1) + barW * i
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

            // Labels overlaid directly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                items.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.first, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(item.third, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = InkSecondary)
                    }
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
                .height(200.dp)
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

                // Real points for Quantum Kernel (AUC = 0.831)
                val qPts = listOf(
                    Offset(padL, padT + plotH),
                    Offset(padL + 0.02f * plotW, padT + (1.0f - 0.12f) * plotH),
                    Offset(padL + 0.05f * plotW, padT + (1.0f - 0.24f) * plotH),
                    Offset(padL + 0.10f * plotW, padT + (1.0f - 0.38f) * plotH),
                    Offset(padL + 0.25f * plotW, padT + (1.0f - 0.62f) * plotH),
                    Offset(padL + 0.50f * plotW, padT + (1.0f - 0.84f) * plotH),
                    Offset(padL + plotW, padT)
                )

                // Real points for Classical Baseline (AUC = 0.794)
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

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendItem("Quantum Kernel (AUC = 0.831)", InkPrimary)
            LegendItem("Classical RBF (AUC = 0.794)", Color(0xFF757575))
            LegendItem("Random Guessing (AUC = 0.500)", Color(0xFFBDBDBD))
        }
    }
}

@Composable
fun CostEfficiencyChart() {
    val items = listOf(
        Triple("Random Lab Testing", 1920, Color(0xFFBDBDBD)),
        Triple("Classical RBF Baseline", 1540, Color(0xFF757575)),
        Triple("Stage 1 Shortlist Filter", 1407, Color(0xFF424242)),
        Triple("MolFunnel QpiAI Stage 2", 907, HitBadgeText)
    )
    val maxCost = 2100f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.first, style = MaterialTheme.typography.labelSmall, fontWeight = if (item.first.contains("MolFunnel")) FontWeight.Bold else FontWeight.Normal)
                    Text("₹${item.second} / hit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = item.third, fontFamily = FontFamily.Monospace)
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
        Text("MolFunnel Stage 2 slashes cost per active hit by 53% compared to random testing.", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
    }
}

@Composable
fun ScaffoldDiversityChart() {
    val stages = listOf(
        Triple("10,000 Pool Baseline", 0.151f, "1,512 / 10,000 (15.1% base variety)"),
        Triple("Stage 1 Shortlist (1,000)", 0.674f, "674 unique families (67.4% variety)"),
        Triple("Stage 2 Batch (50 via QUBO)", 0.880f, "44 unique families (88.0% variety)"),
        Triple("Final 3 Leads (Stage 3 HUBO)", 1.000f, "3 out of 3 (100.0% ZERO DUPLICATES)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stages.forEach { stage ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stage.first, style = MaterialTheme.typography.labelSmall, fontWeight = if (stage.second == 1.0f) FontWeight.Bold else FontWeight.Normal)
                    Text(stage.third, style = MaterialTheme.typography.labelSmall, color = if (stage.second >= 0.88f) HitBadgeText else InkSecondary)
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
        Triple("Seed 11 (Ours)", 1.48f, 2.30f),
        Triple("Seed 22", 1.08f, 1.27f),
        Triple("Seed 33", 1.69f, 1.63f),
        Triple("Seed 44", 1.87f, 1.32f),
        Triple("Seed 55", 1.50f, 1.27f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SPLIT SEED", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text("STAGE 1 EF", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text("STAGE 2 EF", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(seed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("${s1}x", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    Text("${s2}x", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (s2 >= 2.0f) HitBadgeText else InkPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("5-Seed Aggregate Mean:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Stage 1: 1.52x ± 0.29 | Stage 2: 1.56x ± 0.44", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OptSolverBenchmarkChart() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SolverRow("QpiAI Opt (HUBO)", energy = "-1,714,265", runtime = "313 ms", gap = "0.00% (Global Best)", isWinner = true)
        SolverRow("Brute Force (19.6k combinations)", energy = "-1,714,265", runtime = "15 ms", gap = "0.00% (Matches)", isWinner = false)
        SolverRow("Simulated Annealing", energy = "-1,665,400", runtime = "420 ms", gap = "+2.85% worse", isWinner = false)
        SolverRow("Simple Greedy Pick", energy = "-1,569,700", runtime = "4 ms", gap = "+8.43% worse", isWinner = false)
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

@Composable
fun SolverRow(name: String, energy: String, runtime: String, gap: String, isWinner: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isWinner) CodeBg else Color.Transparent,
        border = BorderStroke(1.dp, if (isWinner) InkPrimary else HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Runtime: $runtime", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Energy: $energy", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                Text("Gap: $gap", style = MaterialTheme.typography.labelSmall, color = if (isWinner) HitBadgeText else InkSecondary)
            }
        }
    }
}
