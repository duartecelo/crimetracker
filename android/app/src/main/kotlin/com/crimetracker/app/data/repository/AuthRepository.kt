package com.crimetracker.app.data.repository

import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.data.local.dao.UserDao
import com.crimetracker.app.data.mapper.toEntity
import com.crimetracker.app.data.model.LoginRequest
import com.crimetracker.app.data.model.RegisterRequest
import com.crimetracker.app.data.remote.ApiService
import com.crimetracker.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao
) {

    suspend fun register(username: String, email: String, password: String): Resource<Unit> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, username))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Salvar dados no DataStore
                userPreferences.saveAuthData(
                    token = authResponse.token,
                    userId = authResponse.userId,
                    username = authResponse.username,
                    email = authResponse.email
                )
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Email já cadastrado"
                    400 -> "Dados inválidos. Verifique email e senha (mín. 8 caracteres)"
                    else -> "Erro ao criar conta: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Salvar dados no DataStore
                userPreferences.saveAuthData(
                    token = authResponse.token,
                    userId = authResponse.userId,
                    username = authResponse.username,
                    email = authResponse.email
                )
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Email ou senha incorretos"
                    400 -> "Dados inválidos"
                    else -> "Erro ao fazer login: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun logout() {
        userPreferences.clearAuthData()
        userDao.deleteAll()
    }

    suspend fun getProfile(): Resource<Unit> {
        return try {
            val response = apiService.getProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                userDao.insertUser(user.toEntity())
                Resource.Success(Unit)
            } else {
                when (response.code()) {
                    401 -> {
                        userPreferences.clearAuthData()
                        Resource.Error("Sessão expirada. Faça login novamente.")
                    }
                    else -> Resource.Error("Erro ao buscar perfil: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    fun isLoggedIn(): Flow<Boolean> = flow {
        val token = userPreferences.authToken.first()
        emit(!token.isNullOrEmpty())
    }
}

