package com.parsec.molfunnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.data.MolFunnelRepository
import com.parsec.molfunnel.model.AppBundle
import com.parsec.molfunnel.ui.screens.*
import com.parsec.molfunnel.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = MolFunnelRepository(applicationContext)

        setContent {
            MolFunnelTheme {
                MainApp(repository)
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    FUNNEL("Funnel", Icons.Default.FilterList),
    LEADS("Leads", Icons.Default.Science),
    LAB("Lab", Icons.Default.Tune),
    RESULTS("Results", Icons.Default.BarChart)
}

@Composable
fun MainApp(repository: MolFunnelRepository) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var bundle by remember { mutableStateOf(AppBundle()) }
    var isBackendOnline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        bundle = repository.getBundle()
        isBackendOnline = repository.isBackendReachable()
    }

    Scaffold(
        bottomBar = {
            AppleLiquidGlassBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.HOME -> HomeScreen(bundle) { targetTabIdx ->
                    selectedTab = when (targetTabIdx) {
                        0 -> NavigationTab.FUNNEL
                        1 -> NavigationTab.LEADS
                        2 -> NavigationTab.LAB
                        3 -> NavigationTab.RESULTS
                        else -> NavigationTab.FUNNEL
                    }
                }
                NavigationTab.FUNNEL -> FunnelScreen(bundle)
                NavigationTab.LEADS -> LeadsScreen(bundle)
                NavigationTab.LAB -> LabScreen(bundle, repository)
                NavigationTab.RESULTS -> ResultsScreen(bundle)
            }
        }
    }
}

/**
 * Apple-style liquid glass bottom navigation bar with 100% opaque solid background.
 * Features an animated liquid slider capsule with specular glass gradients and spring physics.
 * Zero opacity or see-through blur so underlying scroll content never bleeds through.
 */
@Composable
fun AppleLiquidGlassBottomBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // 100% Opaque canvas backing - prevents any scroll content from showing through
    Surface(
        color = CanvasBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Apple Floating Capsule Dock (100% solid, opaque)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF222226), // Solid deep obsidian (100% opaque)
                                Color(0xFF141417),
                                Color(0xFF0F0F12)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3F3F48), // Top subtle rim highlight
                                Color(0xFF232328)  // Bottom edge rim
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                // Outer Top Specular Sheen line (Apple glass aesthetic, 100% solid)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF232328),
                                    Color(0xFF5A5A68),
                                    Color(0xFF232328)
                                )
                            )
                        )
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val tabCount = NavigationTab.values().size
                    val tabWidth = maxWidth / tabCount
                    val selectedIndex = selectedTab.ordinal
                    val targetOffset = tabWidth * selectedIndex

                    // Apple bouncy liquid spring animation
                    val animatedOffset by animateDpAsState(
                        targetValue = targetOffset,
                        animationSpec = spring(
                            dampingRatio = 0.74f, // Organic liquid bounce
                            stiffness = 380f
                        ),
                        label = "liquidSliderOffset"
                    )

                    // Liquid Glass Slider Capsule (Solid, 100% Opaque)
                    Box(
                        modifier = Modifier
                            .offset(x = animatedOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(26.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF454552), // Top specular sheen (100% solid)
                                        Color(0xFF2C2C34), // Mid glass reflection
                                        Color(0xFF1E1E24)  // Base tone
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF6B6B7E), // Crisp glossy highlight rim
                                        Color(0xFF34343E)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        // Glossy specular highlight arc at the top of the pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .padding(horizontal = 8.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF454552),
                                            Color(0xFF88889C),
                                            Color(0xFF454552)
                                        )
                                    )
                                )
                        )
                    }

                    // Foreground Tab Items Row
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab

                            // Dynamic scale animation for active tab icon
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = 0.65f,
                                    stiffness = 400f
                                ),
                                label = "tabIconScale"
                            )

                            val iconColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF8E8E93)
                            val textColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF8E8E93)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onTabSelected(tab)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = iconColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .scale(iconScale)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.title,
                                        color = textColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Apple active micro-pill dot
                                    Box(
                                        modifier = Modifier
                                            .width(if (isSelected) 10.dp else 0.dp)
                                            .height(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF0A84FF) else Color(0xFF222226)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
