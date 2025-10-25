package com.cnn.mushroom.ui.screens
import android.app.Application
import androidx.lifecycle.ViewModel
import com.cnn.mushroom.data.Mushroom
import com.cnn.mushroom.data.MushroomRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce


class MushroomViewModel(
    private val mushroomRepository: MushroomRepository,
    private val application: Application
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()



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