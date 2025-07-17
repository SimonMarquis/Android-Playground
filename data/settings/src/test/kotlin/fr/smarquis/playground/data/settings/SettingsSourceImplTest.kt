package fr.smarquis.playground.data.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import fr.smarquis.playground.core.datastore.InMemoryDataStore
import fr.smarquis.playground.core.utils.StandardCoroutineScopeRule
import fr.smarquis.playground.domain.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test

class SettingsSourceImplTest {

    @get:Rule
    val coroutines = StandardCoroutineScopeRule()

    @Test
    fun `source initial value is default Settings instance`() = runTest {
        /* Given */
        val source = SettingsSourceImpl(InMemoryDataStore())
        /* Then */
        assertThat(source.settings.first()).isEqualTo(Settings())
    }

    @Test
    fun `roll updates source`() = runTest {
        /* Given */
        val source = SettingsSourceImpl(InMemoryDataStore())
        val newSettings = Settings(
            strictMode = true,
            uncaughtExceptionHandler = true,
        )
        /* When */
        source.update(newSettings)
        /* Then */
        assertThat(source.settings.first()).isEqualTo(newSettings)
    }

}
