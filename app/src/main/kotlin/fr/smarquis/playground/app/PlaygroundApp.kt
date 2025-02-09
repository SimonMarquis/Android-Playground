package fr.smarquis.playground.app


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.smarquis.playground.core.ui.AndroidPlaygroundTheme
import fr.smarquis.playground.feature.home.HomeRoute
import fr.smarquis.playground.feature.home.addHomeRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun PlaygroundApp() = AndroidPlaygroundTheme {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = Modifier.semantics { testTagsAsResourceId = true }
    ) {
        addHomeRoute(
            navGraphBuilder = this,
            navController = navController,
            navigate = { /* do nothing */ },
        )
        // NOTE: add new routes here
    }
}

