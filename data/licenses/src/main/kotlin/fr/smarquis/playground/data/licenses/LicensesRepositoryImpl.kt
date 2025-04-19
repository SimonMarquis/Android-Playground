package fr.smarquis.playground.data.licenses

import app.cash.licensee.ArtifactDetail
import fr.smarquis.playground.core.di.AssetManager
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.IO
import fr.smarquis.playground.domain.licenses.LicensesRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

internal class LicensesRepositoryImpl @Inject constructor(
    assets: AssetManager,
    @Dispatcher(IO) dispatcher: CoroutineContext,
) : LicensesRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override val licenses = flow {
        assets.open("app/cash/licensee/artifacts.json")
            .bufferedReader().use(BufferedReader::readText)
            .let<String, List<ArtifactDetail>>(json::decodeFromString)
            .sortedWith(compareBy(ArtifactDetail::groupId, ArtifactDetail::artifactId, ArtifactDetail::version))
            .toImmutableList()
            .let { emit(it) }
    }.flowOn(dispatcher)

}
