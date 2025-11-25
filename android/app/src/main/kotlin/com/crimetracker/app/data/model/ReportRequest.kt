package com.crimetracker.app.data.model

data class ReportRequest(
    val tipo: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double,
    val isAnonymous: Boolean = false
)
