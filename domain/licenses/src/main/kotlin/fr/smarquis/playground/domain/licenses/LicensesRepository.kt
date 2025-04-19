package fr.smarquis.playground.domain.licenses

import app.cash.licensee.ArtifactDetail
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

public interface LicensesRepository {
    public val licenses: Flow<ImmutableList<ArtifactDetail>>
}
