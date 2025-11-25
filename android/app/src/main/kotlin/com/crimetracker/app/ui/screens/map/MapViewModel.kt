package com.crimetracker.app.ui.screens.map

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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val selectedReport: Report? = null,
    val error: String? = null,
    val showHeatmap: Boolean = false,
    val filterType: String? = null,
    val userLocation: Pair<Double, Double>? = null,
    val isSatelliteMode: Boolean = false,
    val isAutoDayNightEnabled: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Carregar preferências do usuário
        viewModelScope.launch {
            launch {
                userPreferences.mapType.collect { type ->
                    _uiState.value = _uiState.value.copy(
                        isSatelliteMode = type == "satellite"
                    )
                }
            }
            
            launch {
                userPreferences.autoDayNightMode.collect { enabled ->
                    _uiState.value = _uiState.value.copy(
                        isAutoDayNightEnabled = enabled
                    )
                }
            }
        }
    }

    fun loadReports(latitude: Double, longitude: Double, radiusKm: Double = 5.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = reportRepository.getNearbyReports(latitude, longitude, radiusKm)) {
                is Resource.Success -> {
                    val reports = result.data ?: emptyList()
                    
                    // Lógica de Filtro Simplificada e Robusta
                    val filteredReports = if (_uiState.value.filterType != null) {
                        val filter = _uiState.value.filterType!!.lowercase()
                        reports.filter { report ->
                            // Compara o tipo salvo no banco com o filtro selecionado
                            report.tipo.lowercase() == filter
                        }
                    } else {
                        reports
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reports = filteredReports
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                        reports = result.data ?: emptyList()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun selectReport(report: Report?) {
        _uiState.value = _uiState.value.copy(selectedReport = report)
    }

    fun toggleHeatmap() {
        _uiState.value = _uiState.value.copy(showHeatmap = !_uiState.value.showHeatmap)
    }

    fun setFilter(type: String?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        
        // Reaplicar filtro aos reports existentes
        val currentReports = _uiState.value.reports
        val filtered = if (type != null) {
            // Se já temos reports carregados, precisamos recarregar com filtro
            // Por enquanto, vamos apenas filtrar os que já temos
            emptyList() // Será recarregado na próxima chamada de loadReports
        } else {
            currentReports
        }
        
        _uiState.value = _uiState.value.copy(
            reports = filtered,
            filterType = type
        )
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            userLocation = Pair(latitude, longitude)
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun submitFeedback(reportId: String, feedback: String) {
        viewModelScope.launch {
            // Otimistic update
            val updatedReports = _uiState.value.reports.map { report ->
                if (report.id == reportId) {
                    val currentUseful = report.usefulCount
                    val currentNotUseful = report.notUsefulCount
                    
                    when (feedback) {
                        "useful" -> {
                            if (report.userFeedback == "useful") {
                                // Toggle off
                                report.copy(
                                    usefulCount = (currentUseful - 1).coerceAtLeast(0),
                                    userFeedback = null
                                )
                            } else {
                                // Toggle on (or switch)
                                report.copy(
                                    usefulCount = currentUseful + 1,
                                    notUsefulCount = if (report.userFeedback == "not_useful") (currentNotUseful - 1).coerceAtLeast(0) else currentNotUseful,
                                    userFeedback = "useful"
                                )
                            }
                        }
                        "not_useful" -> {
                            if (report.userFeedback == "not_useful") {
                                // Toggle off
                                report.copy(
                                    notUsefulCount = (currentNotUseful - 1).coerceAtLeast(0),
                                    userFeedback = null
                                )
                            } else {
                                // Toggle on (or switch)
                                report.copy(
                                    notUsefulCount = currentNotUseful + 1,
                                    usefulCount = if (report.userFeedback == "useful") (currentUseful - 1).coerceAtLeast(0) else currentUseful,
                                    userFeedback = "not_useful"
                                )
                            }
                        }
                        else -> report
                    }
                } else {
                    report
                }
            }
            
            _uiState.value = _uiState.value.copy(reports = updatedReports)
            
            // Atualizar report selecionado se necessário
            _uiState.value.selectedReport?.let { selected ->
                if (selected.id == reportId) {
                    _uiState.value = _uiState.value.copy(
                        selectedReport = updatedReports.find { it.id == reportId }
                    )
                }
            }

            // Call API
            val result = reportRepository.submitReportFeedback(reportId, feedback)
            if (result is Resource.Error) {
                // Revert on error (optional, but good practice)
                // For now just showing error
                _uiState.value = _uiState.value.copy(error = "Falha ao salvar avaliação: ${result.message}")
            }
        }
    }

    fun reportAbuse(reportId: String, reason: String, description: String?) {
        viewModelScope.launch {
            // TODO: Implementar chamada à API quando backend estiver pronto
            // Por enquanto, apenas mostrar mensagem de sucesso
            _uiState.value = _uiState.value.copy(
                error = null
            )
        }
    }

    fun toggleSatelliteMode() {
        val newMode = !_uiState.value.isSatelliteMode
        _uiState.value = _uiState.value.copy(
            isSatelliteMode = newMode
        )
        viewModelScope.launch {
            userPreferences.setMapType(if (newMode) "satellite" else "standard")
        }
    }
}
