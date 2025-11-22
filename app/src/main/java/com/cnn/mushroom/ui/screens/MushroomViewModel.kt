package com.cnn.mushroom.ui.screens
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.MushroomRepository
import com.cnn.mushroom.data.UserSettings
import com.cnn.mushroom.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MushroomViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _classificationState = MutableStateFlow<ClassificationState>(ClassificationState.Idle)
    val classificationState: StateFlow<ClassificationState> = _classificationState.asStateFlow()

    val userSettings: StateFlow<UserSettings> = userSettingsRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings.DEFAULT
        )

    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun getCommonName(scientificName: String): String {
       return mushroomRepository.getCommonName(scientificName)
    }

    fun getAllMushrooms(): Flow<List<MushroomEntity>> = mushroomRepository.getAllMushrooms()
    fun deleteAllMushrooms() {
        viewModelScope.launch {
            mushroomRepository.deleteAllMushrooms()
        }
    }
    fun deleteMushroom(mushroom: MushroomEntity) {
        viewModelScope.launch {
            mushroomRepository.deleteMushroom(mushroom)
        }
    }

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