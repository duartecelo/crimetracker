package com.crimetracker.app.ui.screens.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.data.repository.GroupRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val userPreferences: com.crimetracker.app.data.local.UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    var currentUsername: String = ""
        private set

    init {
        viewModelScope.launch {
            userPreferences.username.collect { 
                currentUsername = it ?: ""
            }
        }
        loadGroupDetails()
        checkMembership()
        loadPosts()
    }

    // Load group details
    fun loadGroupDetails() {
        viewModelScope.launch {
            groupRepository.getAllGroupsFlow().collect { groups ->
                val group = groups.find { it.id == groupId }
                _uiState.update { state ->
                    state.copy(
                        group = group
                    )
                }
            }
        }
    }

    // Check if user is a member
    private fun checkMembership() {
        viewModelScope.launch {
            groupRepository.getMyGroupsFlow().collect { myGroups ->
                val isMember = myGroups.any { it.id == groupId }
                _uiState.update { it.copy(isMember = isMember) }
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
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
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
                    loadPosts()
                    checkMembership()
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
                    checkMembership()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun createPost(content: String) {
         // Deprecated, use the one with media
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = postRepository.deletePost(postId)) {
                is Resource.Success -> {
                    _uiState.update { 
                        val updatedPosts = it.posts.filter { post -> post.id != postId }
                        it.copy(
                            posts = updatedPosts,
                            filteredPosts = filterPosts(updatedPosts, it.isImportantFilterActive),
                            isLoading = false,
                            successMessage = "Post removido"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
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
        // ... (unchanged) ...
    }

    fun dislikePost(postId: String) {
        // ... (unchanged) ...
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
