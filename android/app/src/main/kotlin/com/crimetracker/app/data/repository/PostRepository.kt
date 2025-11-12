package com.crimetracker.app.data.repository

import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.data.local.dao.PostDao
import com.crimetracker.app.data.mapper.toEntity
import com.crimetracker.app.data.mapper.toPost
import com.crimetracker.app.data.model.CreatePostRequest
import com.crimetracker.app.data.model.Post
import com.crimetracker.app.data.remote.ApiService
import com.crimetracker.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userPreferences: UserPreferences
) {

    suspend fun createPost(groupId: String, conteudo: String): Resource<Post> {
        return try {
            val response = apiService.createPost(groupId, CreatePostRequest(conteudo))
            
            if (response.isSuccessful && response.body() != null) {
                val post = response.body()!!.data
                postDao.insertPost(post.toEntity())
                Resource.Success(post)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    403 -> "Você não é membro deste grupo"
                    400 -> "Conteúdo inválido (máx. 1000 caracteres)"
                    404 -> "Grupo não encontrado"
                    else -> "Erro ao criar post: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun getGroupPosts(groupId: String, page: Int = 1, limit: Int = 20): Resource<List<Post>> {
        return try {
            val response = apiService.getGroupPosts(groupId, page, limit)
            
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.data
                postDao.insertPosts(posts.map { it.toEntity() })
                Resource.Success(posts)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    403 -> "Você não é membro deste grupo"
                    404 -> "Grupo não encontrado"
                    else -> "Erro ao buscar posts: ${response.code()}"
                }
                // Retornar cache
                val cachedPosts = postDao.getPostsByGroup(groupId).map { entities ->
                    entities.map { it.toPost() }
                }
                Resource.Error(errorMsg, cachedPosts)
            }
        } catch (e: Exception) {
            // Retornar cache
            val cachedPosts = postDao.getPostsByGroup(groupId).map { entities ->
                entities.map { it.toPost() }
            }
            Resource.Error("Erro de conexão: ${e.localizedMessage}", cachedPosts)
        }
    }

    suspend fun getUserFeed(page: Int = 1, limit: Int = 20): Resource<List<Post>> {
        return try {
            val response = apiService.getUserFeed(page, limit)
            
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.data
                postDao.insertPosts(posts.map { it.toEntity() })
                Resource.Success(posts)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    else -> "Erro ao buscar feed: ${response.code()}"
                }
                // Retornar cache
                val cachedPosts = postDao.getAllPosts().map { entities ->
                    entities.map { it.toPost() }
                }
                Resource.Error(errorMsg, cachedPosts)
            }
        } catch (e: Exception) {
            // Retornar cache
            val cachedPosts = postDao.getAllPosts().map { entities ->
                entities.map { it.toPost() }
            }
            Resource.Error("Erro de conexão: ${e.localizedMessage}", cachedPosts)
        }
    }

    suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            val response = apiService.deletePost(postId)
            
            if (response.isSuccessful) {
                postDao.deletePostById(postId)
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    403 -> "Apenas o autor pode deletar este post"
                    404 -> "Post não encontrado"
                    else -> "Erro ao deletar post: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    fun getGroupPostsFlow(groupId: String): Flow<List<Post>> {
        return postDao.getPostsByGroup(groupId).map { entities ->
            entities.map { it.toPost() }
        }
    }

    fun getUserFeedFlow(): Flow<List<Post>> {
        return postDao.getAllPosts().map { entities ->
            entities.map { it.toPost() }
        }
    }

    suspend fun canDeletePost(post: Post): Boolean {
        val userId = userPreferences.userId.first()
        return post.authorId == userId
    }
}

