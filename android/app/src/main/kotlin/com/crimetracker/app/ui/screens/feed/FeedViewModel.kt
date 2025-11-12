package com.crimetracker.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadUserFeed()
    }

    fun createPost(groupId: String, conteudo: String) {
        if (conteudo.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Conteúdo é obrigatório")
            return
        }

        if (conteudo.length > 1000) {
            _uiState.value = _uiState.value.copy(error = "Conteúdo deve ter no máximo 1000 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = postRepository.createPost(groupId, conteudo)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Post publicado!"
                    )
                    loadUserFeed()
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

    fun loadUserFeed(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = postRepository.getUserFeed(page)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    result.cachedData?.collect { cachedPosts ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            posts = cachedPosts,
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

    fun loadGroupPosts(groupId: String, page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = postRepository.getGroupPosts(groupId, page)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    result.cachedData?.collect { cachedPosts ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            posts = cachedPosts,
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

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = postRepository.deletePost(postId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Post deletado"
                    )
                    loadUserFeed()
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

    suspend fun canDeletePost(post: Post): Boolean {
        return postRepository.canDeletePost(post)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

