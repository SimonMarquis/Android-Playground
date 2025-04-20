package fr.smarquis.playground.feature.licenses
import androidx.annotation.Keep
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Keep
@Serializable
public object LicensesRoute

public fun NavGraphBuilder.addLicensesRoute(
    navGraphBuilder: NavGraphBuilder,
    navController: NavController,
    modifier: Modifier = Modifier,
): Unit = composable<LicensesRoute> {
    LicensesScreen(
        modifier = modifier,
        navigateBack = navController::popBackStack
    )
}
