package fr.smarquis.playground.feature.licenses

import app.cash.licensee.ArtifactDetail
import kotlinx.collections.immutable.PersistentMap

public sealed interface UiState {
    public data object Loading : UiState
    public data class Failure(val cause: Throwable): UiState
    public data class Success(val licenses: PersistentMap<String, List<ArtifactDetail>>) : UiState
}
