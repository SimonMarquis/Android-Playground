package fr.smarquis.playground.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import fr.smarquis.playground.domain.dice.Dice
import fr.smarquis.playground.domain.dice.DiceRoller
import fr.smarquis.playground.domain.dice.DiceSource
import fr.smarquis.playground.domain.settings.Settings
import fr.smarquis.playground.domain.settings.SettingsSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ViewModelKey
@ContributesIntoMap(AppScope::class)
public class HomeViewModel internal constructor(
    private val diceRoller: DiceRoller,
    private val diceSource: DiceSource,
    private val settingsSource: SettingsSource,
    internal val data: HomeData,
) : ViewModel() {

    public val rolls: StateFlow<ImmutableList<Dice>> = diceSource.rolls.stateIn(viewModelScope, Eagerly, persistentListOf())
    public fun roll(): Job = viewModelScope.launch { diceSource.roll(diceRoller()) }
    public fun reset(): Job = viewModelScope.launch { diceSource.reset() }

    public val settings: StateFlow<Settings> = settingsSource.settings.stateIn(viewModelScope, Eagerly, Settings())
    public fun update(settings: Settings): Job = viewModelScope.launch { settingsSource.update(settings) }

}
