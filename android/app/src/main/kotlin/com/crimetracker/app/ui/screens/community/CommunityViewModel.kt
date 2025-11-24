package com.crimetracker.app.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.repository.GroupRepository
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val groups: List<Group> = emptyList(),
    val feed: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = groupRepository.getGroups()) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            groups = result.data ?: emptyList(),
                            isLoading = false
                        ) 
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.message,
                            isLoading = false
                        ) 
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            // Não mostrar loading se já tiver dados (refresh silencioso ou paginação futura)
            if (_uiState.value.feed.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            when (val result = postRepository.getUserFeed()) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            feed = result.data ?: emptyList(),
                            isLoading = false
                        ) 
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.message,
                            isLoading = false
                        ) 
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createPost(groupId: String, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = postRepository.createPost(groupId, content)) {
                is Resource.Success -> {
                    // Recarregar feed e grupos
                    loadFeed()
                    loadGroups()
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.message,
                            isLoading = false
                        ) 
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun likePost(postId: String) {
        // Optimistic update
        _uiState.update { state ->
            val updatedFeed = state.feed.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLiked
                    post.copy(
                        isLiked = newIsLiked,
                        likeCount = if (newIsLiked) post.likeCount + 1 else post.likeCount - 1
                    )
                } else {
                    post
                }
            }
            state.copy(feed = updatedFeed)
        }
        // TODO: Call API to persist like
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
