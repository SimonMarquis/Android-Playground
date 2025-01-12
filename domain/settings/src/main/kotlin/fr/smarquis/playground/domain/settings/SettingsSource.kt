package fr.smarquis.playground.domain.settings

import kotlinx.coroutines.flow.Flow

public interface SettingsSource {
    public val settings: Flow<Settings>
    public suspend fun update(settings: Settings)
}
