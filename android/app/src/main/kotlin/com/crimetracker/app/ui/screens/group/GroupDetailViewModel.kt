package com.crimetracker.app.ui.screens.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.repository.GroupRepository

data class GroupDetailUiState(
    val group: Group? = null,
    val isMember: Boolean = false,
    val posts: List<Post> = emptyList(),
    val filteredPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isImportantFilterActive: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroupDetails()
        loadPosts()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            // Em um app real, teríamos um endpoint getGroupDetails(id)
            // Aqui vamos simular buscando da lista ou cache
            // TODO: Implementar getGroupById no Repository
            
            // Por enquanto, vamos buscar todos e filtrar (ineficiente mas funcional para o protótipo)
            groupRepository.getAllGroupsFlow().collect { groups ->
                val group = groups.find { it.id == groupId }
                _uiState.update { it.copy(group = group) }
                
                // Verificar se é membro
                groupRepository.getMyGroupsFlow().collect { myGroups ->
                    val isMember = myGroups.any { it.id == groupId }
                    _uiState.update { it.copy(isMember = isMember) }
                }
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = postRepository.getGroupPosts(groupId)) {
                is Resource.Success -> {
                    val posts = result.data ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            posts = posts,
                            filteredPosts = filterPosts(posts, it.isImportantFilterActive),
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

    fun joinGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = groupRepository.joinGroup(groupId)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isMember = true,
                            successMessage = "Você entrou no grupo!"
                        ) 
                    }
                    loadPosts() // Recarregar posts (talvez mostre mais coisas)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = groupRepository.leaveGroup(groupId)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isMember = false,
                            successMessage = "Você saiu do grupo"
                        ) 
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun createPost(content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = postRepository.createPost(groupId, content)) {
                is Resource.Success -> {
                    loadPosts()
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

    fun toggleImportantFilter() {
        _uiState.update { 
            val newFilterState = !it.isImportantFilterActive
            it.copy(
                isImportantFilterActive = newFilterState,
                filteredPosts = filterPosts(it.posts, newFilterState)
            )
        }
    }

    private fun filterPosts(posts: List<Post>, isImportantOnly: Boolean): List<Post> {
        return if (isImportantOnly) {
            posts.filter { it.isImportant }
        } else {
            posts
        }
    }

    fun likePost(postId: String) {
        // Optimistic update
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLiked
                    post.copy(
                        isLiked = newIsLiked,
                        likeCount = if (newIsLiked) post.likeCount + 1 else post.likeCount - 1,
                        isDisliked = if (newIsLiked) false else post.isDisliked,
                        dislikeCount = if (newIsLiked && post.isDisliked) post.dislikeCount - 1 else post.dislikeCount
                    )
                } else {
                    post
                }
            }
            state.copy(
                posts = updatedPosts,
                filteredPosts = filterPosts(updatedPosts, state.isImportantFilterActive)
            )
        }
        // TODO: Call API
    }

    fun dislikePost(postId: String) {
        // Optimistic update
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    val newIsDisliked = !post.isDisliked
                    post.copy(
                        isDisliked = newIsDisliked,
                        dislikeCount = if (newIsDisliked) post.dislikeCount + 1 else post.dislikeCount - 1,
                        isLiked = if (newIsDisliked) false else post.isLiked,
                        likeCount = if (newIsDisliked && post.isLiked) post.likeCount - 1 else post.likeCount
                    )
                } else {
                    post
                }
            }
            state.copy(
                posts = updatedPosts,
                filteredPosts = filterPosts(updatedPosts, state.isImportantFilterActive)
            )
        }
        // TODO: Call API
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
