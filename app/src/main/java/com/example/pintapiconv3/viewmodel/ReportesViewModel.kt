package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pintapiconv3.adapter.DynamicReportItem
import com.example.pintapiconv3.repository.ReportesRepository
import com.example.pintapiconv3.utils.Const.Entities.MATCHES
import com.example.pintapiconv3.utils.Const.Entities.RESERVATIONS
import com.example.pintapiconv3.utils.Const.Entities.USERS
import kotlinx.coroutines.launch

class ReportesViewModel(private val reportesRepository: ReportesRepository) : ViewModel() {

    private val _reportData = MutableLiveData<List<DynamicReportItem>>()
    val reportData: LiveData<List<DynamicReportItem>> get() = _reportData

    private val _chartData = MutableLiveData<Map<String, Int>>()
    val chartData: LiveData<Map<String, Int>> get() = _chartData

    fun fetchReportData(entity: String, fechaDesde: String?, fechaHasta: String?) {
        viewModelScope.launch {
            val result = when (entity) {
                RESERVATIONS -> reportesRepository.getReservas(fechaDesde, fechaHasta)
                MATCHES -> reportesRepository.getPartidos(fechaDesde, fechaHasta)
                USERS -> reportesRepository.getUsuarios(fechaDesde, fechaHasta)
                else -> emptyList()
            }
            _reportData.value = result
            calculateChartData(result, entity)
        }
    }

    fun calculateChartData(data: List<DynamicReportItem>, entity: String) {
        val counts = mutableMapOf<String, Int>()
        data.forEach { item ->
            val estado = when (entity) {
                RESERVATIONS -> item.fields[4]
                MATCHES -> item.fields[2]
                else -> null
            }
            estado?.let {
                counts[it] = counts.getOrDefault(it, 0) + 1
            }
        }
        _chartData.value = counts
    }
}

class ReportesViewModelFactory(private val reportesRepository: ReportesRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ReportesViewModel::class.java)) {
            return ReportesViewModel(reportesRepository) as T
        }
        throw IllegalArgumentException("Uknown ViewModel class")
    }
}