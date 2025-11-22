package com.crimetracker.app.data.repository

import com.crimetracker.app.data.local.dao.GroupDao
import com.crimetracker.app.data.mapper.toEntity
import com.crimetracker.app.data.mapper.toGroup
import com.crimetracker.app.data.model.CreateGroupRequest
import com.crimetracker.app.data.model.Group
import com.crimetracker.app.data.remote.ApiService
import com.crimetracker.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: ApiService,
    private val groupDao: GroupDao
) {

    suspend fun createGroup(nome: String, descricao: String?, coverUrl: String? = null): Resource<Group> {
        return try {
            val response = apiService.createGroup(CreateGroupRequest(nome, descricao, coverUrl))
            
            if (response.isSuccessful && response.body() != null) {
                val group = response.body()!!.data
                groupDao.insertGroup(group.toEntity(isMember = true))
                Resource.Success(group)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    409 -> "Já existe um grupo com este nome"
                    400 -> "Nome inválido"
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
                // Tentar retornar cache
                val cachedGroups = if (search != null) {
                    groupDao.searchGroups(search).map { entities ->
                        entities.map { it.toGroup() }
                    }
                } else {
                    groupDao.getAllGroups().map { entities ->
                        entities.map { it.toGroup() }
                    }
                }
                Resource.Error(errorMsg, cachedGroups)
            }
        } catch (e: Exception) {
            // Retornar cache
            val cachedGroups = if (search != null) {
                groupDao.searchGroups(search).map { entities ->
                    entities.map { it.toGroup() }
                }
            } else {
                groupDao.getAllGroups().map { entities ->
                    entities.map { it.toGroup() }
                }
            }
            Resource.Error("Erro de conexão: ${e.localizedMessage}", cachedGroups)
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

