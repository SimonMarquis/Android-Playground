package fr.smarquis.playground.app


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.smarquis.playground.core.ui.AndroidPlaygroundTheme
import fr.smarquis.playground.feature.home.HomeRoute
import fr.smarquis.playground.feature.home.addHomeRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaygroundApp() = AndroidPlaygroundTheme {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
    ) {
        addHomeRoute(
            navGraphBuilder = this,
            navController = navController,
            navigate = { /* do nothing */ },
        )
        // NOTE: add new routes here
    }
}

