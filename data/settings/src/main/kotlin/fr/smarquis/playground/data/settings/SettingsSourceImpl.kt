package fr.smarquis.playground.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import fr.smarquis.playground.core.datastore.get
import fr.smarquis.playground.domain.settings.Settings
import fr.smarquis.playground.domain.settings.SettingsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

private val StrictModeKey = booleanPreferencesKey("strict-mode")
private val UncaughtExceptionHandlerKey = booleanPreferencesKey("uncaught-exception-handler")

internal class SettingsSourceImpl @Inject constructor(
    private val datastore: DataStore<Preferences>,
) : SettingsSource {

    override val settings: Flow<Settings> = combine(
        datastore[StrictModeKey],
        datastore[UncaughtExceptionHandlerKey],
    ) { strictMode, uncaughtExceptionHandler ->
        Settings(
            strictMode = strictMode ?: false,
            uncaughtExceptionHandler = uncaughtExceptionHandler ?: false,
        )
    }
        .catch { emit(Settings()) }
        .distinctUntilChanged()

    override suspend fun update(settings: Settings) {
        datastore.edit {
            it[StrictModeKey] = settings.strictMode
            it[UncaughtExceptionHandlerKey] = settings.uncaughtExceptionHandler
        }
    }

}
