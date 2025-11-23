package fr.smarquis.playground.app


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import fr.smarquis.playground.core.ui.AndroidPlaygroundTheme
import fr.smarquis.playground.core.utils.navigation.EntryInstaller
import fr.smarquis.playground.feature.home.Home
import kotlinx.collections.immutable.ImmutableSet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun PlaygroundApp(
    entryProviders: ImmutableSet<EntryInstaller>,
): Unit = AndroidPlaygroundTheme {
    val backstack = remember { mutableStateListOf<NavKey>(Home) }
    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        backStack = backstack,
        entryProvider = entryProvider { entryProviders.forEach { it(backstack) } },
        modifier = Modifier.semantics { testTagsAsResourceId = true },
    )
}
