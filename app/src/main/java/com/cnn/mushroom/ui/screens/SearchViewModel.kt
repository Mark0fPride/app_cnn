package com.cnn.mushroom.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnn.mushroom.data.MushroomRepository
import com.cnn.mushroom.data.MushroomEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val mushrooms: List<MushroomEntity>) : SearchState()
    data class Error(val message: String) : SearchState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MushroomRepository
) : ViewModel() {
    fun getCommonName(mushroomMame: String) : String{
        return repository.getCommonName(mushroomMame)
    }

    private var End: Long? = null
    private var Start: Long? = null

    private val _dateRange = MutableStateFlow<ClosedRange<Long>?>(null)
    val dateRange = _dateRange.asStateFlow()

    private val _showAllMushrooms = MutableStateFlow(false)
    val showAllMushrooms = _showAllMushrooms.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    init {
        End = System.currentTimeMillis()
        Start = End?.minus(30L * 24 * 60 * 60 * 1000)
        _dateRange.value = Start!!..End!!
        fetchMushrooms()
    }

    private fun fetchMushrooms() {
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val mushroomsFlow = if (_showAllMushrooms.value) {
                    repository.getAllMushrooms()
                } else {
                    val range = _dateRange.value
                    if (range != null) repository.getMushroomsByDateRange(range.start, range.endInclusive)
                    else repository.getAllMushrooms()
                }

                mushroomsFlow.map { mushrooms ->
                    val query = _searchQuery.value.lowercase()
                    val filtered = if (query.isBlank()) mushrooms
                    else mushrooms.filter {
                        it.name.lowercase().contains(query) ||
                                getCommonName(it.name).lowercase().contains(query)
                    }
                    SearchState.Success(filtered)
                }.collect { _searchState.value = it }

            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setDateRange(from: Long, to: Long) {
        _dateRange.value = from..to
        Start = _dateRange.value?.start
        End = _dateRange.value?.endInclusive
        _showAllMushrooms.value = false
        fetchMushrooms()
    }

    fun toggleShowAllMushrooms() {
        _showAllMushrooms.value = !_showAllMushrooms.value
        if (!_showAllMushrooms.value) {
            _dateRange.value = Start!!..End!!
        } else {
            _dateRange.value = null
        }
        fetchMushrooms()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        fetchMushrooms()
    }

    fun resetSearch() {
        _searchQuery.value = ""
        _showAllMushrooms.value = false
        _dateRange.value = Start!!..End!!
        fetchMushrooms()
    }
}
