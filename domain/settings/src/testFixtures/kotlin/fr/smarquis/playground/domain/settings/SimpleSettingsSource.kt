package fr.smarquis.playground.domain.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SimpleSettingsSource(initialSettings: Settings) : SettingsSource {
    private val mutableSettings = MutableStateFlow(initialSettings)
    override val settings = mutableSettings
    override suspend fun update(settings: Settings) = mutableSettings.update { settings }
}
