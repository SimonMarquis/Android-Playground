package fr.smarquis.playground.feature.licenses

import app.cash.licensee.ArtifactDetail
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import fr.smarquis.playground.core.utils.StandardCoroutineScopeRule
import fr.smarquis.playground.domain.SimpleLicensesRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.io.FileNotFoundException
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LicensesViewModelTest {

    @get:Rule
    val coroutines = StandardCoroutineScopeRule()

    @Test
    fun `loading state`() = runTest {
        /* Given */
        val vm = LicensesViewModel(
            repo = SimpleLicensesRepository(emptyFlow()),
            dispatcher = coroutines.dispatcher,
        )

        /* When / Then */
        vm.uiState.test {
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `success state`() = runTest {
        /* Given */
        val artifact = ArtifactDetail(
            groupId = "fr.smarquis",
            artifactId = "foo",
            version = "1.0.0",
            name = "Foo",
        )
        val vm = LicensesViewModel(
            repo = SimpleLicensesRepository(persistentListOf(artifact)),
            dispatcher = coroutines.dispatcher,
        )

        /* When / Then */
        vm.uiState.test {
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            assertThat(awaitItem()).isEqualTo(UiState.Success(persistentMapOf(artifact.groupId to listOf(artifact))))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `error state`() = runTest {
        /* Given */
        val exception = FileNotFoundException()
        val vm = LicensesViewModel(
            repo = SimpleLicensesRepository { throw exception },
            dispatcher = coroutines.dispatcher,
        )

        /* When / Then */
        vm.uiState.test {
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            assertThat(awaitItem()).isEqualTo(UiState.Failure(exception))
            ensureAllEventsConsumed()
        }
    }

}
