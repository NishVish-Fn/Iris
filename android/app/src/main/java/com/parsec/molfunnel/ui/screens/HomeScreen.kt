package com.parsec.molfunnel.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.R
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
        Pair("1. The Target Disease", "Finding compounds that block ALDH1, a key enzyme driving cancer stem cells and chemotherapy resistance.") to 0,
        Pair("2. Clean Lab Data", "145,133 real lab assay records from LIT-PCBA, cleaned to remove chemical salts and conflicting measurements.") to 0,
        Pair("3. Fair Testing (Zero Overlap)", "Split into 3,000 training compounds and 10,000 testing candidates. The test set contains completely new chemical families.") to 0,
        Pair("4. The 8-Qubit Quantum Filter", "An 8-qubit quantum circuit compares molecular shapes using QpiAI Quantum SDK, scoring activity without memorizing old shapes.") to 3,
        Pair("5. Stage 1: Fast Filter", "Narrows 10,000 candidates down to the top 1,000, limiting each chemical family to 5 so we don't pick duplicates.") to 0,
        Pair("6. Stage 2: Quantum Diversity Filter", "Uses QpiAI Opt to pick the 50 best, most diverse candidates, boosting discovery odds by 2.3x.") to 0,
        Pair("7. Stage 3: The Final 3 Leads", "QpiAI Opt picks the 3 ultimate winner leads. Enforces 100% variety across chemical families for just ₹334 total bill.") to 1,
        Pair("8. The Reveal Rule", "Test answers were locked away until choices were finalized, guaranteeing 100% honesty with zero cheating.") to 3,
        Pair("9. Real-World Accuracy Boost", "Stage 2 delivered a 2.3x boost in finding active cures (12% hit rate vs 5.2% normal).") to 3,
        Pair("10. Chemical Novelty", "All 3 final lead molecules are completely new chemical structures never seen during training.") to 1,
        Pair("11. Budget & Lab Cost", "Every molecule carries a real price tag (₹100–₹150). Total final bill: ₹333.96.") to 1,
        Pair("12. Lab Testing Plan", "A step-by-step wet-lab recipe to test the 3 leads with fluorescence and mass spectrometry.") to 1
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
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
                        text = "IRIS",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 3.sp,
                        color = InkSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Official Iris Quantum Benzene Logo
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(10.dp, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .border(1.dp, HairlineBorder, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "Iris Quantum Benzene Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
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
