package fr.smarquis.playground.feature.licenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.licensee.ArtifactDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.smarquis.playground.core.di.qualifier.Dispatcher
import fr.smarquis.playground.core.di.qualifier.Dispatcher.Type.Default
import fr.smarquis.playground.domain.licenses.LicensesRepository
import fr.smarquis.playground.feature.licenses.UiState.Failure
import fr.smarquis.playground.feature.licenses.UiState.Loading
import fr.smarquis.playground.feature.licenses.UiState.Success
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
internal class LicensesViewModel @Inject constructor(
    repo: LicensesRepository,
    @Dispatcher(Default) dispatcher: CoroutineContext,
) : ViewModel() {

    val uiState: StateFlow<UiState> = repo.licenses
        .map { Success(it.groupBy(ArtifactDetail::groupId).toSortedMap().toPersistentMap()) }
        .catch<UiState> { emit(Failure(it)) }
        .flowOn(dispatcher)
        .stateIn(viewModelScope, Lazily, Loading)

}

