package com.cnn.mushroom.ui.screens
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.MyApplication
import com.cnn.mushroom.data.Mushroom
import com.cnn.mushroom.data.MushroomRepository
import com.cnn.mushroom.ui.screens.MushroomUiState.Loading
import com.cnn.mushroom.ui.screens.MushroomUiState.Success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class MushroomViewModel(
    private val mushroomRepository: MushroomRepository,
    private val application: Application
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MushroomUiState> =
        _searchQuery
            .debounce(300) // poczekaj chwilę, żeby nie spamować zapytań
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    mushroomRepository.mushooms
                } else {
                    mushroomRepository.getMushroomByName(query)
                }
            }
            .map<List<Mushroom>, MushroomUiState>(::Success)
            .catch { emit(MushroomUiState.Error(it))}
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Loading
            )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

// UI States
sealed interface MushroomUiState {
    object Loading : MushroomUiState
    data class Error(val throwable: Throwable) : MushroomUiState
    data class Success(val data: List<Mushroom>) : MushroomUiState
}