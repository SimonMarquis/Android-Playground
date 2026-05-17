package fr.smarquis.playground.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import fr.smarquis.playground.core.di.FileManager
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

@ContributesTo(AppScope::class)
@BindingContainer
public object DataStoreBindings {
    @Provides
    @SingleIn(AppScope::class)
    internal fun providesDataStore(
        @Dispatcher(IO) context: CoroutineContext,
        fileManager: FileManager,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = CoroutineScope(context + SupervisorJob()),
        produceFile = { fileManager("datastore/datastore.preferences_pb") },
    )
}
