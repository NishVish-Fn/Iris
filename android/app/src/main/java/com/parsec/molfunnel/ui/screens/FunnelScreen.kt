package com.parsec.molfunnel.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.ui.theme.*

@Composable
fun FunnelScreen(bundle: AppBundle) {
    var scrubberStage by remember { mutableFloatStateOf(3f) }
    val currentStageInt = scrubberStage.toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Screening Funnel",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "10,000 Pool → 1,000 Filter → 50 QUBO → 3 HUBO",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Funnel Dot Grid Interactive Canvas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FUNNEL DOT-GRID VISUALIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw representative dots
                        val rows = 12
                        val cols = 26
                        for (r in 0 until rows) {
                            val rowY = (height / (rows + 1)) * (r + 1)
                            val rowWidthFactor = 1.0f - (r.toFloat() / rows) * 0.75f
                            val effectiveCols = (cols * rowWidthFactor).toInt().coerceAtLeast(3)
                            val startX = (width - effectiveCols * 14f) / 2f

                            for (c in 0 until effectiveCols) {
                                val dotX = startX + c * 14f
                                val isSurvivor = when (currentStageInt) {
                                    0 -> true
                                    1 -> r < 8
                                    2 -> r >= 4 && r < 9 && c % 2 == 0
                                    else -> r >= 9 && c in (effectiveCols / 2 - 1)..(effectiveCols / 2 + 1)
                                }
                                val color = if (isSurvivor) InkPrimary else Color(0xFFD6D6D6)
                                drawCircle(color = color, radius = 3.5f, center = Offset(dotX, rowY))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pool (10k)", style = MaterialTheme.typography.labelSmall)
                        Text("Stage 1 (1k)", style = MaterialTheme.typography.labelSmall)
                        Text("Stage 2 (50)", style = MaterialTheme.typography.labelSmall)
                        Text("Stage 3 (3)", style = MaterialTheme.typography.labelSmall)
                    }

                    Slider(
                        value = scrubberStage,
                        onValueChange = { scrubberStage = it },
                        valueRange = 0f..3f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = InkPrimary,
                            activeTrackColor = InkPrimary,
                            inactiveTrackColor = HairlineBorder
                        )
                    )
                }
            }
        }

        // Stage 1 Card
        item {
            StageCard(
                stageNum = "STAGE 1: CLASSICAL FILTER",
                reduction = "10,000 → 1,000",
                tool = "CLASSICAL RANKING",
                toolTagColor = InkSecondary,
                description = "Molecules ranked by acquisition net value v_i = R*(P_i + beta*sigma) + eta*nu - c_i with max 5 compounds per Bemis-Murcko scaffold cluster.",
                metrics = bundle.officialMetrics["stage1_1000"]
            )
        }

        // Stage 2 Card
        item {
            StageCard(
                stageNum = "STAGE 2: COMBINATORIAL QUBO",
                reduction = "1,000 → 50",
                tool = "QpiAI OPT (QUBO)",
                toolTagColor = InkPrimary,
                description = "QUBO formulated with exact cardinality penalty A*(sum x - 50)^2 and Tanimoto diversity penalty. Solved with authenticated QpiAI Opt LocalProvider.",
                metrics = bundle.officialMetrics["stage2_50"]
            )
        }

        // Stage 3 Card
        item {
            StageCard(
                stageNum = "STAGE 3: HIGHER-ORDER HUBO",
                reduction = "50 → FINAL 3",
                tool = "QpiAI OPT (HUBO)",
                toolTagColor = InkPrimary,
                description = "Native cubic HUBO term omega*x_i*x_j*x_l strictly penalizes >2 compounds from identical scaffold. Verified against ground-truth brute force over all 19,600 combinations (gap=0.0000).",
                metrics = bundle.officialMetrics["stage3_final3"]
            )
        }
    }
}

@Composable
fun StageCard(
    stageNum: String,
    reduction: String,
    tool: String,
    toolTagColor: Color,
    description: String,
    metrics: com.parsec.molfunnel.model.StageMetric?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stageNum, style = MaterialTheme.typography.labelSmall)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChipBg
                ) {
                    Text(
                        text = tool,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = toolTagColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reduction,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(description, style = MaterialTheme.typography.bodyMedium)

            if (metrics != null) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = HairlineBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniItem("True Hits", "${metrics.hits}")
                    MetricMiniItem("Hit Rate", "${(metrics.hitRate * 100).toInt()}%")
                    MetricMiniItem("EF", "${metrics.enrichmentFactor}x")
                    MetricMiniItem("Cost", "₹${metrics.totalCost.toInt()}")
                }
            }
        }
    }
}

@Composable
fun MetricMiniItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
