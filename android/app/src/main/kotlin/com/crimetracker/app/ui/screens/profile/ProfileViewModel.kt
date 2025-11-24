package com.crimetracker.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.repository.AuthRepository
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val email: String = "",
    val nickname: String = "",
    val description: String = "",
    val userColor: String = "#1E3A8A",
    val profileImageUri: String? = null,
    val posts: List<Post> = emptyList(),
    val isLoadingPosts: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadUserPosts()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userPreferences.userProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(
                    username = profile.username,
                    email = profile.email,
                    nickname = profile.nickname.ifEmpty { profile.username },
                    description = profile.description,
                    userColor = profile.userColor
                )
            }
        }
    }

    fun loadUserPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPosts = true, error = null)
            
            when (val result = postRepository.getUserPosts()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPosts = false,
                        posts = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPosts = false,
                        error = result.message,
                        posts = emptyList()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoadingPosts = false)
                }
            }
        }
    }

    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname)
        viewModelScope.launch {
            userPreferences.setUserNickname(nickname)
        }
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
        viewModelScope.launch {
            userPreferences.setUserDescription(description)
        }
    }

    fun updateUserColor(color: String) {
        _uiState.value = _uiState.value.copy(userColor = color)
        viewModelScope.launch {
            userPreferences.setUserColor(color)
        }
    }

    fun updateProfileImage(uri: String?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

