package fr.smarquis.playground.domain

import app.cash.licensee.ArtifactDetail
import fr.smarquis.playground.domain.licenses.LicensesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.experimental.ExperimentalTypeInference

public class SimpleLicensesRepository(
    override val licenses: Flow<ImmutableList<ArtifactDetail>>
) : LicensesRepository {
    constructor(licenses: ImmutableList<ArtifactDetail>):this(flowOf(licenses))
    @OptIn(ExperimentalTypeInference::class)
    constructor(@BuilderInference block: suspend FlowCollector<ImmutableList<ArtifactDetail>>.() -> Unit):this(flow(block))
}
