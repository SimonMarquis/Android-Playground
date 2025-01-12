package fr.smarquis.playground.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet

open class InMemoryDataStore(
    override val data: MutableStateFlow<Preferences> = MutableStateFlow(emptyPreferences()),
) : DataStore<Preferences> {

    constructor(preferences: Preferences) : this(MutableStateFlow(preferences))

    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences,
    ): Preferences = data.updateAndGet { transform(it) }
}
