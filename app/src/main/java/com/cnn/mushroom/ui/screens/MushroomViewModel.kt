package com.cnn.mushroom.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.classifyMushroom
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.MushroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MushroomViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository,
) : ViewModel() {


    private val _recentMushroom = MutableStateFlow<MushroomEntity?>(null)
    val recentMushroom: StateFlow<MushroomEntity?> = _recentMushroom

    private val _isComputing = MutableStateFlow(false)
    val isComputing: StateFlow<Boolean> = _isComputing

    fun classifyPhoto(path: String) {
        viewModelScope.launch {
            _isComputing.value = true

            val mushroom = classifyMushroom(path) // suspend function
            _recentMushroom.value = mushroom

            _isComputing.value = false

            withContext(Dispatchers.IO) {
                mushroomRepository.addMushroom(mushroom)
            }
        }
    }

    private fun fetchRecentMushroom() {
        viewModelScope.launch {
            mushroomRepository.getRecentMushroom().collectLatest { mushroom ->
                _recentMushroom.value = mushroom
            }
        }
    }

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