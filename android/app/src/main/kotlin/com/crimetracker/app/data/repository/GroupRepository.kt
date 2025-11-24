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

                // Try to get from cache
                val cachedGroups = getCachedGroupsFlow(search).firstOrNull() ?: emptyList()
                Resource.Error(errorMsg, data = cachedGroups)
            }
        } catch (e: Exception) {
            // Return cache on network error
            val cachedGroups = try {
                 getCachedGroupsFlow(search).firstOrNull() ?: emptyList()
            } catch (ex: Exception) {
                emptyList()
            }
            Resource.Error("Erro de conexão: ${e.localizedMessage}", data = cachedGroups)
        }
    }

    private fun getCachedGroupsFlow(search: String?): Flow<List<Group>> {
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
