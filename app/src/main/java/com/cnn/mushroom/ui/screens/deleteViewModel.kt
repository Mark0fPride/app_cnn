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
class DeleteViewModel @Inject constructor(
    private val mushroomRepository: MushroomRepository
) : ViewModel() {
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    fun toggleSelection(id: Int) {
        _selectedIds.value =
            if (_selectedIds.value.contains(id))
                _selectedIds.value - id
            else
                _selectedIds.value + id
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            mushroomRepository.deleteMushroomsByIds(_selectedIds.value.toList())
            clearSelection()
        }
    }

}
