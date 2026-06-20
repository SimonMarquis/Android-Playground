package fr.smarquis.playground.domain.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

public class SimpleSettingsSource(initialSettings: Settings) : SettingsSource {
    override val settings: StateFlow<Settings>
        field = MutableStateFlow(initialSettings)

    override suspend fun update(settings: Settings) = this.settings.update { settings }
}
