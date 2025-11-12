package com.crimetracker.app.data.repository

import com.crimetracker.app.data.local.dao.CrimeReportDao
import com.crimetracker.app.data.mapper.toEntity
import com.crimetracker.app.data.mapper.toReport
import com.crimetracker.app.data.model.CreateReportRequest
import com.crimetracker.app.data.model.Report
import com.crimetracker.app.data.remote.ApiService
import com.crimetracker.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService,
    private val crimeReportDao: CrimeReportDao
) {

    suspend fun createReport(
        tipo: String,
        descricao: String,
        latitude: Double,
        longitude: Double
    ): Resource<Report> {
        return try {
            val response = apiService.createReport(
                CreateReportRequest(tipo, descricao, latitude, longitude)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val report = response.body()!!.data
                crimeReportDao.insertReport(report.toEntity())
                Resource.Success(report)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    400 -> "Dados inválidos. Verifique tipo e descrição (máx. 500 chars)"
                    else -> "Erro ao criar denúncia: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    suspend fun getNearbyReports(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Resource<List<Report>> {
        return try {
            val response = apiService.getNearbyReports(latitude, longitude, radiusKm)
            
            if (response.isSuccessful && response.body() != null) {
                val reports = response.body()!!.data
                // Cache reports
                crimeReportDao.insertReports(reports.map { it.toEntity() })
                Resource.Success(reports)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Não autenticado. Faça login novamente."
                    else -> "Erro ao buscar denúncias: ${response.code()}"
                }
                // Tentar retornar cache
                val cachedReports = crimeReportDao.getAllReports().map { entities ->
                    entities.map { it.toReport() }
                }
                Resource.Error(errorMsg, cachedReports)
            }
        } catch (e: Exception) {
            // Retornar cache em caso de erro de conexão
            val cachedReports = crimeReportDao.getAllReports().map { entities ->
                entities.map { it.toReport() }
            }
            Resource.Error("Erro de conexão: ${e.localizedMessage}", cachedReports)
        }
    }

    suspend fun getReportById(reportId: String): Resource<Report> {
        return try {
            val response = apiService.getReportById(reportId)
            
            if (response.isSuccessful && response.body() != null) {
                val report = response.body()!!.data
                crimeReportDao.insertReport(report.toEntity())
                Resource.Success(report)
            } else {
                Resource.Error("Erro ao buscar denúncia: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.localizedMessage}")
        }
    }

    fun getAllReportsFlow(): Flow<List<Report>> {
        return crimeReportDao.getAllReports().map { entities ->
            entities.map { it.toReport() }
        }
    }
}

