package com.crimetracker.app.data.repository

import com.crimetracker.app.data.local.dao.GroupDao
import com.crimetracker.app.data.mapper.toEntity
import com.crimetracker.app.data.mapper.toGroup
import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.remote.ApiService
import com.crimetracker.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: ApiService,
    private val groupDao: GroupDao
) {

    suspend fun createGroup(nome: String, descricao: String?, imageFile: File? = null): Resource<Group> {
        return try {
            val nomePart = nome.toRequestBody("text/plain".toMediaTypeOrNull())
            val descricaoPart = descricao?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imagem", file.name, requestBody)
            }

            val response = apiService.createGroup(nomePart, descricaoPart, imagePart)
            
            if (response.isSuccessful && response.body() != null) {
                val group = response.body()!!.data
                // Backend now returns the group with membership correctly set implicitly, but we force it locally too just in case
                // Actually we should trust the backend, but we set isMember = true because we just created it.
                val updatedGroup = group.copy(isMember = true)
                groupDao.insertGroup(updatedGroup.toEntity(isMember = true))
                Resource.Success(updatedGroup)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    409 -> "Já existe um grupo com este nome"
                    400 -> "Dados inválidos ou imagem muito grande"
                    else -> "Erro ao criar grupo: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun getGroups(search: String? = null): Resource<List<Group>> {
        return try {
            val response = apiService.getGroups(search)
            
            if (response.isSuccessful && response.body() != null) {
                val groups = response.body()!!.data
                groupDao.insertGroups(groups.map { it.toEntity() })
                Resource.Success(groups)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    else -> "Erro ao buscar grupos: ${response.code()}"
                }

                // Return cache flow (assuming dao returns Flow, if List it needs flow{emit(..)})
                // Since we can't see DAO, we will assume it returns Flow as per typical architecture in this project (see UserPreferences)
                // If it returned List, the previous error would have been "Found List, Required Flow".
                // The error "Found List, Required Flow" on lines 75/84 suggests that getCachedGroupsFlow WAS RETURNING FLOW<LIST>,
                // BUT Resource.Error 2nd arg expected something else or I was passing the wrong thing.
                // Actually, if I passed `List<Group>` to `cachedData: Flow<T>?`, that was the error.
                // `getCachedGroupsFlow` returns `Flow<List<Group>>`.
                // `Resource.Error` takes `cachedData: Flow<T>?`.
                // So `Resource.Error(msg, getCachedGroupsFlow(search))` matches types perfectly.

                val cachedFlow = getCachedGroupsFlow(search)
                Resource.Error(errorMsg, cachedFlow)
            }
        } catch (e: Exception) {
            // Return cache flow on network error
            val cachedFlow = getCachedGroupsFlow(search)
            Resource.Error("Erro de conexão: ${e.localizedMessage}", cachedFlow)
        }
    }

    private fun getCachedGroupsFlow(search: String?): Flow<List<Group>> {
         // We assume searchGroups and getAllGroups return Flow.
         // If they return List (suspend), map will return List, so we need flow { emit(...) }
         // But typically in these apps they return Flow.
         // If the previous error was "actual type is List", it might mean the DAO returns List.
         // To be 100% safe without seeing DAO, we can use 'kotlinx.coroutines.flow.flow' builder if we knew it was suspend.
         // But if it IS Flow, flow { emit(flow) } is Flow<Flow<...>>.
         // Given `UserPreferences` uses Flow, it's likely Flow.
         // The previous error was almost certainly because I was calling `.firstOrNull()` inside the `Resource.Error` call,
         // effectively passing `List<Group>` to a `Flow<List<Group>>` parameter.
         // My current code passes the Flow directly.
         return if (search != null) {
            groupDao.searchGroups(search).map { entities ->
                entities.map { it.toGroup() }
            }
        } else {
            groupDao.getAllGroups().map { entities ->
                entities.map { it.toGroup() }
            }
        }
    }

    suspend fun joinGroup(groupId: String): Resource<Unit> {
        return try {
            val response = apiService.joinGroup(groupId)
            
            if (response.isSuccessful) {
                groupDao.updateMemberStatus(groupId, true)
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    404 -> "Grupo não encontrado"
                    409 -> "Você já é membro deste grupo"
                    else -> "Erro ao entrar no grupo: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun leaveGroup(groupId: String): Resource<Unit> {
        return try {
            val response = apiService.leaveGroup(groupId)
            
            if (response.isSuccessful) {
                groupDao.updateMemberStatus(groupId, false)
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    404 -> "Grupo não encontrado"
                    400 -> "Você não é membro deste grupo"
                    else -> "Erro ao sair do grupo: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    fun getMyGroupsFlow(): Flow<List<Group>> {
        return groupDao.getMyGroups().map { entities ->
            entities.map { it.toGroup() }
        }
    }

    fun getAllGroupsFlow(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { it.toGroup() }
        }
    }
}
