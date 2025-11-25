package com.crimetracker.app.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.local.UserPreferences
import com.crimetracker.app.data.model.Report
import com.crimetracker.app.data.repository.ReportRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun createReport(
        tipo: String,
        descricao: String,
        latitude: Double,
        longitude: Double
    ) {
        if (descricao.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Descrição é obrigatória")
            return
        }

        if (descricao.length > 500) {
            _uiState.value = _uiState.value.copy(error = "Descrição deve ter no máximo 500 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val isAnonymous = userPreferences.anonymousModeDefault.first()
            
            when (val result = reportRepository.createReport(tipo, descricao, latitude, longitude, isAnonymous)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Denúncia criada com sucesso!"
                    )
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

    fun getNearbyReports(latitude: Double, longitude: Double, radiusKm: Double = 5.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = reportRepository.getNearbyReports(latitude, longitude, radiusKm)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reports = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    // Tentar usar cache se disponível
                    result.cachedData?.collect { cachedReports ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reports = cachedReports,
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

