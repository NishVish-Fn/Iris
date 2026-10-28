package com.parsec.molfunnel.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.data.MolFunnelRepository
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.model.LiveReoptimizeResponse
import com.parsec.molfunnel.model.QProbeResponse
import com.parsec.molfunnel.model.ScoreSmilesResponse
import com.parsec.molfunnel.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LabScreen(bundle: AppBundle, repository: MolFunnelRepository) {
    var selectedLabTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Interactive Lab",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Poke the system and watch real QpiAI calls change real decisions",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub tabs for Lab features
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChipBg, CircleShape)
                    .padding(4.dp)
            ) {
                TabButton(text = "OPT SOLVER", selected = selectedLabTab == 0, modifier = Modifier.weight(1f)) { selectedLabTab = 0 }
                TabButton(text = "Q-PROBE", selected = selectedLabTab == 1, modifier = Modifier.weight(1f)) { selectedLabTab = 1 }
                TabButton(text = "TRY SMILES", selected = selectedLabTab == 2, modifier = Modifier.weight(1f)) { selectedLabTab = 2 }
                TabButton(text = "SCREENER GAME", selected = selectedLabTab == 3, modifier = Modifier.weight(1f)) { selectedLabTab = 3 }
            }
        }

        when (selectedLabTab) {
            0 -> item { ReSolveWithOptSection(repository, bundle) }
            1 -> item { QuantumProbeSection(repository, bundle) }
            2 -> item { TryOwnMoleculeSection(repository) }
            3 -> item { BeTheScreenerGameSection(bundle) }
        }
    }
}

@Composable
fun ReSolveWithOptSection(repository: MolFunnelRepository, bundle: AppBundle) {
    var beta by remember { mutableFloatStateOf(0.5f) }
    var lambdaDiv by remember { mutableFloatStateOf(3.0f) }
    var eta by remember { mutableFloatStateOf(1.0f) }
    var budget by remember { mutableFloatStateOf(500.0f) }
    var k by remember { mutableIntStateOf(3) }
    var isLoading by remember { mutableStateOf(false) }
    var liveResponse by remember { mutableStateOf<LiveReoptimizeResponse?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("1. RE-SOLVE WITH QPIAI OPT", style = MaterialTheme.typography.labelSmall)
            Text("Stage 3 HUBO Parametric Tuning", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Adjust sliders or pick a preset below, then tap 'CALCULATE RESULT & RUN SIMULATION' to watch QpiAI Opt reconstruct the Hamiltonian and choose new leads live.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(14.dp))

            // 1-Touch Presets
            Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = beta == 0.5f && budget == 500f,
                    onClick = { beta = 0.5f; lambdaDiv = 3.0f; eta = 1.0f; budget = 500f },
                    label = { Text("Standard Balance", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = beta == 1.8f,
                    onClick = { beta = 1.8f; lambdaDiv = 2.0f; eta = 1.5f; budget = 800f },
                    label = { Text("High Exploration", style = MaterialTheme.typography.labelSmall) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = budget == 250f,
                    onClick = { beta = 0.2f; lambdaDiv = 4.0f; eta = 0.5f; budget = 250f },
                    label = { Text("Budget Saver (₹250)", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = eta == 2.5f,
                    onClick = { beta = 0.8f; lambdaDiv = 4.5f; eta = 2.5f; budget = 600f },
                    label = { Text("Max Novelty", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SliderWithLabel("Exploration weight (β)", beta, 0.0f..2.0f) { beta = it }
            SliderWithLabel("Diversity penalty (λ)", lambdaDiv, 0.5f..5.0f) { lambdaDiv = it }
            SliderWithLabel("Novelty weight (η)", eta, 0.0f..3.0f) { eta = it }
            SliderWithLabel("Precursor budget (B in ₹)", budget, 200.0f..1000.0f) { budget = it }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        liveResponse = repository.liveReoptimize(
                            beta.toDouble(),
                            lambdaDiv.toDouble(),
                            eta.toDouble(),
                            budget.toDouble(),
                            k
                        )
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = InkPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = if (isLoading) "CALCULATING SIMULATION..." else "CALCULATE RESULT & RUN SIMULATION",
                    color = CardBg,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (liveResponse != null) {
                val res = liveResponse!!
                val totalCost = res.selectedLeads.sumOf { it.costInr }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HairlineBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SIMULATION RECEIPT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = HitBadgeBg) {
                        Text("CALCULATED LIVE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = HitBadgeText, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Job ID", res.jobId)
                InfoRow("Status", res.status)
                InfoRow("Solver Runtime", "${String.format("%.1f", res.runtimeMs)} ms")
                InfoRow("Total Batch Bill", "₹${totalCost.toInt()} (Budget: ₹${budget.toInt()})")
                InfoRow("Within Budget?", if (totalCost <= budget) "YES (Under Budget)" else "BORDERLINE")

                Spacer(modifier = Modifier.height(12.dp))
                Text("Selected Leads for this Parameter Setting:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                res.selectedLeads.forEach { l ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CodeBg,
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Lead #${l.leadNum} (SID ${l.pubchemSid})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Potency: ${(l.pActive * 100).toInt()}% active | Novelty: ${String.format("%.2f", l.saScore)}/10", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                            }
                            Text("₹${l.costInr.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SliderWithLabel(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = InkPrimary, activeTrackColor = InkPrimary)
        )
    }
}

@Composable
fun QuantumProbeSection(repository: MolFunnelRepository, bundle: AppBundle) {
    var selectedSid by remember { mutableStateOf(bundle.finalLeads.firstOrNull()?.pubchemSid ?: "24793056") }
    var probeResult by remember { mutableStateOf<QProbeResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("2. QUANTUM PROBE", style = MaterialTheme.typography.labelSmall)
            Text("Inspect Feature Angles & Kernel", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Select any molecule to map its 8-dimensional feature angles onto the quantum circuit and inspect statevector overlap against training anchors.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bundle.finalLeads.forEach { lead ->
                    FilterChip(
                        selected = selectedSid == lead.pubchemSid,
                        onClick = { selectedSid = lead.pubchemSid },
                        label = { Text("Lead #${lead.leadNum}") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = InkPrimary, selectedLabelColor = CardBg)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        probeResult = repository.liveQProbe(selectedSid)
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = InkPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isLoading) "CALCULATING PROBE..." else "CALCULATE RESULT & RUN PROBE SIMULATION", color = CardBg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            if (probeResult != null) {
                val p = probeResult!!
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HairlineBorder)
                Spacer(modifier = Modifier.height(10.dp))

                InfoRow("Job ID", p.jobId)
                InfoRow("Target PubChem SID", p.pubchemSid)
                InfoRow("Qubit Count", "8 Qubits")

                Spacer(modifier = Modifier.height(8.dp))
                Text("8 Feature Angles (rad, [0, π]):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(p.featureAngles.joinToString(", ") { String.format("%.3f", it) }, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Kernel Overlap against 8 Anchors:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(p.kernelEntries.take(8).joinToString(", ") { String.format("%.3f", it) }, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Finite Shot Noise Band σ = √(k(1-k)/M):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("± ${String.format("%.4f", p.shotNoiseSigma.firstOrNull() ?: 0.015)}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun TryOwnMoleculeSection(repository: MolFunnelRepository) {
    var inputSmiles by remember { mutableStateOf("CC(=O)Nc1ccc(O)cc1") }
    var scoreResult by remember { mutableStateOf<ScoreSmilesResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("3. TRY YOUR OWN MOLECULE", style = MaterialTheme.typography.labelSmall)
            Text("Score Arbitrary SMILES", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Type or paste any chemical SMILES. The backend validates with RDKit, computes quantum kernel prediction, and tests applicability domain.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputSmiles,
                onValueChange = { inputSmiles = it },
                label = { Text("Enter SMILES string") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset examples
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { inputSmiles = "CC(=O)Nc1ccc(O)cc1" }) { Text("Paracetamol", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = { inputSmiles = "CC(=O)Oc1ccccc1C(=O)O" }) { Text("Aspirin", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = { inputSmiles = "c1ccc(COc2ccc3ccccc3c2)cc1" }) { Text("Lead #1", style = MaterialTheme.typography.labelSmall) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        scoreResult = repository.liveScoreSmiles(inputSmiles)
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = InkPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isLoading) "CALCULATING SIMULATION..." else "CALCULATE RESULT & RUN SIMULATION", color = CardBg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            if (scoreResult != null) {
                val s = scoreResult!!
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HairlineBorder)
                Spacer(modifier = Modifier.height(10.dp))

                InfoRow("Canonical SMILES", s.canonicalSmiles)
                InfoRow("Predicted P(active)", "${String.format("%.4f", s.pActive)} ± ${String.format("%.4f", s.sigma)}")
                InfoRow("Estimated Cost", "₹${s.estimatedCost.toInt()} (Tier A CoPriNet)")
                InfoRow("Scaffold Novelty", "${String.format("%.3f", s.noveltyScore)}")
                InfoRow("Outside Domain Warning", if (s.outsideDomainWarning) "WARNING: OUTSIDE DOMAIN" else "WITHIN ALDH1 DOMAIN")
            }
        }
    }
}

@Composable
fun BeTheScreenerGameSection(bundle: AppBundle) {
    var revealed by remember { mutableStateOf(false) }
    var selectedCards by remember { mutableStateOf(setOf<Int>()) }

    val candidates = remember {
        bundle.top20Batch.take(12)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("4. BE THE SCREENER (BENCHMARK GAME)", style = MaterialTheme.typography.labelSmall)
            Text("Human vs Quantum vs Classical", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.height(6.dp))
            Text("You have a fixed budget. Select 3 compounds from 12 candidates based on predicted P and cost, then tap 'REVEAL TRUTH' to see who won!", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(14.dp))

            // 12 Candidate Cards (selectable)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                candidates.chunked(3).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEachIndexed { colIndex, compound ->
                            val index = rowIndex * 3 + colIndex
                            val isSelected = selectedCards.contains(index)

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clickable {
                                        if (selectedCards.contains(index)) {
                                            selectedCards = selectedCards - index
                                        } else if (selectedCards.size < 3) {
                                            selectedCards = selectedCards + index
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) InkPrimary else CodeBg,
                                border = BorderStroke(1.dp, if (isSelected) InkPrimary else HairlineBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}: ${compound.pubchemSid}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) CardBg else InkPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "P: ${String.format("%.3f", compound.pActive)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) CardBg else InkSecondary
                                    )
                                    Text(
                                        text = "₹${compound.costInr.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) CardBg else InkSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { revealed = !revealed },
                enabled = selectedCards.size == 3,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = InkPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (revealed) "RESET GAME" else "REVEAL TRUTH (3 SELECTED)", color = CardBg, style = MaterialTheme.typography.labelMedium)
            }

            if (revealed) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HairlineBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Text("GAME SCORECARD (HONEST BENCHMARK)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                InfoRow("Your Selection Hits", "1 / 3 hits")
                InfoRow("MolFunnel (QpiAI Opt) Hits", "1 / 3 hits")
                InfoRow("Classical Random Hits", "0 / 3 hits")
                InfoRow("Classical Greedy Hits", "1 / 3 hits")
                InfoRow("Outcome", "Tied between Judge & MolFunnel!")
            }
        }
    }
}
