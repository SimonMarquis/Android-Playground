package fr.smarquis.playground.data.dice

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import fr.smarquis.playground.core.datastore.get
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Default
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.DiceSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@VisibleForTesting
internal val RollsPreferenceKey = byteArrayPreferencesKey("rolls")

internal class DiceSourceImpl @Inject constructor(
    private val datastore: DataStore<Preferences>,
    @param:Dispatcher(Default) private val dispatcher: CoroutineContext,
) : DiceSource {


    override val rolls: Flow<ImmutableList<Dice>> = datastore[RollsPreferenceKey]
        .map { it?.map { Dice.entries.getOrNull(it.toInt()) }?.filterNotNull().orEmpty() }
        .catch { emit(persistentListOf()) }
        .map { it.toImmutableList() }
        .flowOn(dispatcher)

    override suspend fun roll(dice: Dice) {
        datastore.edit {
            val current = datastore[RollsPreferenceKey].first() ?: ByteArray(0)
            it[RollsPreferenceKey] = current.copyOf(current.size + 1).apply { this[lastIndex] = dice.ordinal.toByte() }
        }
    }

    override suspend fun reset() {
        datastore.edit { it.remove(RollsPreferenceKey) }
    }

}

