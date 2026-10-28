package com.parsec.molfunnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    RESULTS("Results", Icons.Default.BarChart),
    EVIDENCE("Evidence", Icons.Default.Verified)
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
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 4.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = InkPrimary,
                            selectedTextColor = InkPrimary,
                            indicatorColor = ChipBg,
                            unselectedIconColor = InkSecondary,
                            unselectedTextColor = InkSecondary
                        )
                    )
                }
            }
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
                        4 -> NavigationTab.EVIDENCE
                        else -> NavigationTab.FUNNEL
                    }
                }
                NavigationTab.FUNNEL -> FunnelScreen(bundle)
                NavigationTab.LEADS -> LeadsScreen(bundle)
                NavigationTab.LAB -> LabScreen(bundle, repository)
                NavigationTab.RESULTS -> ResultsScreen(bundle)
                NavigationTab.EVIDENCE -> EvidenceScreen(bundle)
            }
        }
    }
}
