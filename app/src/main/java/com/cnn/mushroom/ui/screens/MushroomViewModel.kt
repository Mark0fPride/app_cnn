package com.cnn.mushroom.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.MushroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MushroomViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository,
) : ViewModel() {

//    private val _searchQuery = MutableStateFlow("")
//    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
//
//    fun updateSearchQuery(query: String) {
//        _searchQuery.value = query
//    }

    val mushrooms = mushroomRepository.getAllMushrooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMushroom(mushroom: MushroomEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            mushroomRepository.addMushroom(mushroom)
        }
    }

    fun deleteAllMushrooms(){
        viewModelScope.launch(Dispatchers.IO) {
            mushroomRepository.deleteAllMushrooms()
        }
    }

}

// UI States
sealed interface MushroomUiState {
    object Loading : MushroomUiState
    data class Error(val throwable: Throwable) : MushroomUiState
    data class Success(val data: List<MushroomEntity>) : MushroomUiState
}