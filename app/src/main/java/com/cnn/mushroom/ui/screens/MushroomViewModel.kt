package com.cnn.mushroom.ui.screens
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.data.MushroomEntity
import com.cnn.mushroom.data.MushroomRepository
import com.cnn.mushroom.data.UserSettings
import com.cnn.mushroom.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.internal.wait

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

    fun getMushroomById(id: Int): Flow<MushroomEntity?> = mushroomRepository.getMushroomByIdSingle(id)

    fun classifyMushroom(imagePath: Uri) {
        _classificationState.value = ClassificationState.Loading

        viewModelScope.launch {
            mushroomRepository.classifyMushroom(imagePath)
                .catch { error ->
                    _classificationState.value = ClassificationState.Error(error.message ?: "Unknown error")
                }
                .collect { mushroom ->
                    if (mushroom != null) {
                        mushroomRepository.addMushroom(mushroom)
                        _classificationState.value = ClassificationState.Success(mushroom)
                    } else {
                        _classificationState.value = ClassificationState.Error("Confidence too low (<50%) Mushroom not recognized")
                        delay(3000)
                        _classificationState.value = ClassificationState.Idle
                    }
                }
        }
    }

    fun swapTopPrediction(mushroomId: Int, newTopName: String, oldTopName: String) {
        viewModelScope.launch {
            // 1. Pobierz obecną encję (może być potrzebne dla pełnego obiektu do aktualizacji)
            val mushroom = mushroomRepository.getMushroomByIdSingle(mushroomId).first()
            Log.d("MushroomViewModel", "Mushroom to update: ${mushroom?.id}")
            if (mushroom != null) {
                // 2. Utwórz nową listę TopKNames:
                // a) Dodaj starą nazwę Top-1 do listy Top-K
                val updatedTopKNames = (mushroom.topKNames + oldTopName)
                    // b) Usuń nową nazwę Top-1 z listy Top-K
                    .filter { it != newTopName }
                    // c) Ogranicz listę do 4 elementów (Top-2 do Top-5)
                    .take(4)

                // 3. Utwórz zaktualizowaną encję
                val updatedMushroom = mushroom.copy(
                    name = newTopName, // Nowa nazwa Top-1
                    topKNames = updatedTopKNames, // Zaktualizowana lista Top-K
                    confidenceScore = null,
                    isEdible = mushroomRepository.getEdibility(newTopName)
                )

                Log.d("MushroomViewModel", "Updating mushroom with ID: ${updatedMushroom.id}")

                // 4. Zapisz zaktualizowaną encję
                mushroomRepository.updateMushroom(updatedMushroom)
            }
        }
    }
    fun resetClassificationState(){
        _classificationState.value = ClassificationState.Idle
    }

}

// Stan klasyfikacji
sealed class ClassificationState {
    object Idle : ClassificationState()
    object Loading : ClassificationState()
    data class Success(val mushroom: MushroomEntity) : ClassificationState()
    data class Error(val message: String) : ClassificationState()
}