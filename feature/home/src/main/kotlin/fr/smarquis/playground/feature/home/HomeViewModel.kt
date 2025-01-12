package fr.smarquis.playground.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.smarquis.playground.domain.dice.Dice
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
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val diceSource: DiceSource,
    private val settingsSource: SettingsSource,
    val data: HomeData,
) : ViewModel() {

    val rolls: StateFlow<ImmutableList<Dice>> = diceSource.rolls.stateIn(viewModelScope, Eagerly, persistentListOf())
    fun roll() = viewModelScope.launch { diceSource.roll(Dice.entries.random()) }
    fun reset(): Job = viewModelScope.launch { diceSource.reset() }

    val settings: StateFlow<Settings> = settingsSource.settings.stateIn(viewModelScope, Eagerly, Settings())
    fun update(settings: Settings): Job = viewModelScope.launch { settingsSource.update(settings) }

}
