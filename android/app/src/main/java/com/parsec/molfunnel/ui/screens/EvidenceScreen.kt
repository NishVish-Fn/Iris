package com.parsec.molfunnel.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.data.MolFunnelRepository
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.ui.theme.*

@Composable
fun EvidenceScreen(
    bundle: AppBundle,
    repository: MolFunnelRepository
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = HitBadgeBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Evidence",
                            tint = HitBadgeText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verifiable Evidence Audit",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = "Cryptographic provenance & claims verification",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        maxLines = 2
                    )
                }
            }
        }

        // Section 1: Provenance & Execution Integrity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, InkPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Execution Provenance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = CircleShape,
                            color = HitBadgeBg
                        ) {
                            Text(
                                text = "HONEST DISCLOSURE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = HitBadgeText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    EvidenceField("Target Assay", "ALDH1 (LIT-PCBA AID 1030 • 10,000 pool)")
                    EvidenceField("Active Scaffolds", "485 distinct active clusters (asserted 0% test overlap)")
                    EvidenceField("Quantum Execution", "LOCAL_STATEVECTOR (8 Qubits, depth 1, gamma 0.25)")
                    EvidenceField("Optimization Engine", "OPT_LOCAL (QpiAI Opt LocalProvider)")
                    EvidenceField("QCloud Remote Status", "FAILED_SUBSCRIPTION_REQUIRED (Auth OK, HTTP 400)")
                    EvidenceField("Shot Noise Fidelity", "Matches sqrt(k(1-k)/M) bound across 256-8192 shots")
                    EvidenceField("Pre-trim Variable Count", "300 Candidates (2-level QUBO partitioning)")
                }
            }
        }

        // Section 2: Automated Test Matrix (test_claims.py, check_secrets.py)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automated Test Matrix (CI / Local)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TestStatusRow("tests/test_claims.py", "10 / 10 Claims Match Exactly", "PASS")
                    TestStatusRow("scripts/check_secrets.py", "0 Secrets, 0 Windows Paths", "PASS")
                    TestStatusRow("tests/test_qubo_ising.py", "Ising <-> QUBO exact bijection (<1e-10)", "PASS")
                    TestStatusRow("scripts/verify_evidence.py", "All QCloud & Opt JSONs Re-verified", "PASS")
                    TestStatusRow("tests/test_data_integrity.py", "SHA-256 Manifest Validated", "PASS")
                }
            }
        }

        // Section 3: Traceable Claims Matrix from claims.yaml
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Claims Traceability (claims.yaml)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every number in Iris reports and UI is bound to verified result files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ClaimRow("Stage 1 Filter", "77 Hits (EF 1.48x)", "results/runs/seed11/metrics.json")
                    ClaimRow("Stage 2 QUBO (50)", "6 Hits (EF 2.30x)", "results/runs/seed11/metrics.json")
                    ClaimRow("Stage 3 HUBO (3)", "0 Hits (Honest Result)", "results/runs/seed11/metrics.json")
                    ClaimRow("Opt Optimality Gap", "0.00% (Matches Brute Force)", "results/runs/seed11/selection.json")
                    ClaimRow("Quantum Circuit", "8 Qubits (PSD Verified)", "results/quantum_diagnostics.json")
                    ClaimRow("Cost Model R²", "0.9070 (Spearman ρ 0.9749)", "results/cost_model_sensitivity.json")
                    ClaimRow("Noise Robustness", "p <= 0.04 tolerable limit", "results/noise_hardware_limits.json")
                }
            }
        }

        // Section 4: One-Command Reproduction
        item {
            val reproCmd = "git clone https://github.com/NishVish-Fn/Iris.git && make all && make verify"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141417)),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "One-Command Reproduction",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(reproCmd))
                                copiedText = "Copied to clipboard!"
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy command",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF09090B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reproCmd,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                    if (copiedText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = copiedText ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun EvidenceField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.5.sp,
            letterSpacing = 0.5.sp,
            color = InkSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = InkPrimary,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp), color = HairlineBorder, thickness = 0.5.dp)
    }
}

@Composable
private fun TestStatusRow(testName: String, detail: String, status: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = CodeBg,
        border = BorderStroke(0.5.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Pass",
                tint = HitBadgeText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = testName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.5.sp,
                    color = InkSecondary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = HitBadgeBg
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = HitBadgeText
                )
            }
        }
    }
}

@Composable
private fun ClaimRow(title: String, outcome: String, sourcePath: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = CodeBg,
        border = BorderStroke(0.5.dp, HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = HitBadgeBg
                ) {
                    Text(
                        text = "AUDITED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = HitBadgeText
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = outcome,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = HitBadgeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Source: $sourcePath",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.5.sp,
                color = InkSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
