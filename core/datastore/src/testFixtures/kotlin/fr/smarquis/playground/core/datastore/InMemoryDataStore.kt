package fr.smarquis.playground.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet

open class InMemoryDataStore(
    initialData: MutableStateFlow<Preferences> = MutableStateFlow(emptyPreferences()),
) : DataStore<Preferences> {

    final override val data: Flow<Preferences>
        field = initialData

    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences,
    ): Preferences = data.updateAndGet { transform(it) }
}
