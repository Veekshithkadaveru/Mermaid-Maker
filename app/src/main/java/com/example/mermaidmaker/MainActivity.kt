package com.example.mermaidmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mermaidmaker.ui.diagrams.CreateDiagramScreen
import com.example.mermaidmaker.ui.editor.MainEditorScreen
import com.example.mermaidmaker.ui.home.HomeScreen
import com.example.mermaidmaker.ui.preview.MermaidPreviewTest
import com.example.mermaidmaker.ui.settings.ApiKeyScreen
import com.example.mermaidmaker.ui.theme.MermaidMakerTheme
import com.example.mermaidmaker.ui.components.BottomNavItem
import com.example.mermaidmaker.ui.components.ProfessionalBottomNavigation
import com.example.mermaidmaker.ui.components.ProfessionalScaffold
import com.example.mermaidmaker.ui.components.ProfessionalTopAppBar
import com.example.mermaidmaker.ui.components.ProfessionalHomeTopBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MermaidMakerTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("editor", "Editor", Icons.Filled.Add),
        BottomNavItem("home", "Home", Icons.Filled.Home),
        BottomNavItem("settings", "Settings", Icons.Filled.Settings)
    )

    // Normalize selection for nested editor routes
    val selectedTopRoute = when {
        currentRoute?.startsWith("editor") == true -> "editor"
        else -> currentRoute
    }

    ProfessionalScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (selectedTopRoute) {
                "home" -> ProfessionalHomeTopBar()
                "settings" -> ProfessionalTopAppBar(title = "Settings")
                "editor" -> ProfessionalTopAppBar(title = "Mermaid Maker")
                else -> {}
            }
        },
        bottomBar = {
            ProfessionalBottomNavigation(
                items = items,
                selectedRoute = selectedTopRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTopRoute == "home") {
                FloatingActionButton(onClick = { navController.navigate("create") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "editor",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("editor") {
                MainEditorScreen()
            }
            composable("editor/{diagramId}") { backStackEntry ->
                val diagramId = backStackEntry.arguments?.getString("diagramId")
                MainEditorScreen(diagramId = diagramId)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("create") {
                CreateDiagramScreen(navController = navController)
            }
            composable("settings") {
                SettingsScreen(navController)
            }
            composable("api_keys") {
                ApiKeyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("preview_test") {
                MermaidPreviewTest()
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // API Keys Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI Features",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configure AI providers to enable diagram generation from text",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController?.navigate("api_keys") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage API Keys")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Developer Tools Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Developer Tools",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Testing and debugging tools",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { navController?.navigate("preview_test") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Preview System")
                }
            }
        }
    }
}