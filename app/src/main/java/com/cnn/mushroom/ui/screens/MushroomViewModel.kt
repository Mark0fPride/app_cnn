package com.cnn.mushroom.ui.screens
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.MushroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class MushroomViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository,
) : ViewModel() {

    private val _classificationState = MutableStateFlow<ClassificationState>(ClassificationState.Idle)
    val classificationState: StateFlow<ClassificationState> = _classificationState.asStateFlow()

//    private val _userSettings = MutableStateFlow(UserSettings.DEFAULT)
//    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()
//
//    fun updateSettings(newSettings: UserSettings) {
//        // Użycie metody 'value' w MutableStateFlow do natychmiastowej aktualizacji
//        _userSettings.value = newSettings
//
//        // Alternatywnie, jeśli aktualizacja zależy od bieżącej wartości, użyjemy 'update':
//        // _userSettings.update { currentSettings ->
//        //     newSettings // W tym przypadku po prostu nadpisujemy
//        // }
//    }

    fun getAllMushrooms(): Flow<List<MushroomEntity>> = mushroomRepository.getAllMushrooms()
    fun deleteAllMushrooms() {
        viewModelScope.launch {
            mushroomRepository.deleteAllMushrooms()
        }
    }
    fun deleteMushroom(mushroomEntity: MushroomEntity) = mushroomRepository.deleteMushroom(mushroomEntity)

    fun getMushroomById(id: Int): Flow<MushroomEntity?> = mushroomRepository.getMushroomById(id)

    fun classifyMushroom(imagePath: Uri) {
        _classificationState.value = ClassificationState.Loading

        viewModelScope.launch {
            mushroomRepository.classifyMushroom(imagePath)
                .catch { error ->
                    _classificationState.value = ClassificationState.Error(error.message ?: "Unknown error")
                }
                .collect { mushroom ->
                    mushroomRepository.addMushroom(mushroom)
                    _classificationState.value = ClassificationState.Success(mushroom)
                }
        }
    }
}

// Stan klasyfikacji
sealed class ClassificationState {
    object Idle : ClassificationState()
    object Loading : ClassificationState()
    data class Success(val mushroom: MushroomEntity) : ClassificationState()
    data class Error(val message: String) : ClassificationState()
}