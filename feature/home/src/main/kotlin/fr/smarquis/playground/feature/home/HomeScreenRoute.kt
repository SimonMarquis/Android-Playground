package fr.smarquis.playground.feature.home

import androidx.annotation.Keep
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Keep
@Serializable
public object HomeRoute

public fun NavGraphBuilder.addHomeRoute(
    navGraphBuilder: NavGraphBuilder,
    navController: NavController,
    modifier: Modifier = Modifier,
    navigate: () -> Unit,
): Unit = composable<HomeRoute> {
    // NOTE: Customize behavior with navGraphBuilder and/or navController
    HomeScreen(
        modifier = modifier,
        navigate = navigate,
    )
}
