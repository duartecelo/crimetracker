package com.crimetracker.app.ui.screens.group

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

data class GroupUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val myGroups: List<Group> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        loadMyGroups()
    }

    fun createGroup(nome: String, descricao: String?, coverUrl: String? = null) {
        if (nome.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Nome do grupo é obrigatório")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = groupRepository.createGroup(nome, descricao, coverUrl)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Grupo criado com sucesso!"
                    )
                    loadMyGroups()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun searchGroups(query: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = groupRepository.getGroups(query)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        groups = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    result.cachedData?.collect { cachedGroups ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            groups = cachedGroups,
                            error = result.message
                        )
                    } ?: run {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = groupRepository.joinGroup(groupId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Você entrou no grupo!"
                    )
                    loadMyGroups()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = groupRepository.leaveGroup(groupId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Você saiu do grupo"
                    )
                    loadMyGroups()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    private fun loadMyGroups() {
        viewModelScope.launch {
            groupRepository.getMyGroupsFlow().collect { groups ->
                _uiState.value = _uiState.value.copy(myGroups = groups)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

