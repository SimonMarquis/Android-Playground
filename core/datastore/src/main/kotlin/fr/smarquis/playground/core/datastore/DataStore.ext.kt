package fr.smarquis.playground.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


/**
 * @return [Preferences]'s [Flow] safely, catching any [IOException] and recovering with [emptyPreferences].
 */
public fun DataStore<Preferences>.safe(): Flow<Preferences> = data.catch {
    if (it !is IOException) throw it else emit(emptyPreferences())
}

/**
 * @return this [DataStore]'s [key] [Flow] value or `null` if it's not found.
 */
public operator fun <T> DataStore<Preferences>.get(key: Preferences.Key<T>): Flow<T?> = safe().map { it[key] }
