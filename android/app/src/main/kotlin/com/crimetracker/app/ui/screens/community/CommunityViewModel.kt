package com.crimetracker.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.repository.GroupRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = groupRepository.getGroups()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groups = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                        groups = emptyList()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

