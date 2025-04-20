package fr.smarquis.playground.feature.licenses

import app.cash.licensee.ArtifactDetail
import kotlinx.collections.immutable.PersistentMap

internal sealed interface UiState {
    data object Loading : UiState
    data class Failure(val cause: Throwable): UiState
    data class Success(val licenses: PersistentMap<String, List<ArtifactDetail>>) : UiState

}
