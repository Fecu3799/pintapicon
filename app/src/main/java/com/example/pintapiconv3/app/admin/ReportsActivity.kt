package com.example.pintapiconv3.app.admin

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.DynamicReportAdapter
import com.example.pintapiconv3.repository.ReportesRepository
import com.example.pintapiconv3.utils.Const.Entities.MATCHES
import com.example.pintapiconv3.utils.Const.Entities.RESERVATIONS
import com.example.pintapiconv3.utils.Const.Entities.USERS
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.ReportesViewModel
import com.example.pintapiconv3.viewmodel.ReportesViewModelFactory
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Dynamic
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var fechaDesde: TextView
    private lateinit var fechaHasta: TextView
    private lateinit var spnerEntidades: Spinner
    private lateinit var spnerFiltro: Spinner
    private lateinit var rvListado: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var reportsAdapter: DynamicReportAdapter

    private val reportesRepository = ReportesRepository()
    private val reportesViewModel: ReportesViewModel by viewModels {
        ReportesViewModelFactory(reportesRepository)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reports)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupDatePickers()
        setupObservers()
        setupPieChart()
    }

    private fun initViews() {
        fechaDesde = findViewById(R.id.fecha_desde)
        fechaHasta = findViewById(R.id.fecha_hasta)
        spnerEntidades = findViewById(R.id.spner_entidades)
        spnerFiltro = findViewById(R.id.spner_filtro)
        rvListado = findViewById(R.id.rv_listado)
        pieChart = findViewById(R.id.pie_chart)

        setupSpinners()

    }

    private fun setupObservers() {

        reportesViewModel.reportData.observe(this) { data ->
            val entity = spnerEntidades.selectedItem.toString()
            val columnTitles = when(entity) {
                RESERVATIONS -> listOf("Fecha", "Hora inicio", "Hora fin", "Monto", "Estado", "Predio")
                MATCHES -> listOf("Fecha", "Hora", "Estado", "Participantes", "Tipo de cancha")
                USERS -> listOf("Nombre", "Apellido", "Reservas", "Partidos jugados", "Abandonos")
                else -> emptyList()
            }
            reportsAdapter.setData(columnTitles, data, entity)
        }

        reportesViewModel.chartData.observe(this) { chartData ->
            if(chartData.isNotEmpty()) {
                updatePieChart(chartData)
            } else {
                pieChart.clear()
                pieChart.invalidate()
            }
        }

    }

    private fun setupSpinners() {
        val entidades = listOf("ENTIDADES", USERS, RESERVATIONS, MATCHES)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entidades)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnerEntidades.adapter = spinnerAdapter

        spnerEntidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val entity = entidades[position]
                if(entity != "ENTIDADES") {
                    fetchAndDisplayData()
                    when (entity) {
                        RESERVATIONS -> {
                            spnerFiltro.visibility = android.view.View.VISIBLE
                            setupSpinnerFilters(RESERVATIONS)
                        }
                        MATCHES -> {
                            spnerFiltro.visibility = android.view.View.VISIBLE
                            setupSpinnerFilters(MATCHES)
                        }
                        USERS -> {
                            spnerFiltro.visibility = android.view.View.GONE
                            reportsAdapter.setData(emptyList(), emptyList(), entity)
                            pieChart.clear()
                            pieChart.invalidate()
                        }
                    }
                } else {
                    spnerFiltro.visibility = android.view.View.GONE
                    reportsAdapter.setData(emptyList(), emptyList(), entity)
                    pieChart.clear()
                    pieChart.invalidate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // DO NOTHING
            }
        }
    }

    private fun setupSpinnerFilters(entity: String) {

    }

    private fun setupRecyclerView() {
        rvListado.layoutManager = LinearLayoutManager(this)
        reportsAdapter = DynamicReportAdapter()
        rvListado.adapter = reportsAdapter
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        fechaDesde.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                fechaDesde.text = dateFormat.format(calendar.time)
                fetchAndDisplayData()
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        fechaHasta.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                fechaHasta.text = dateFormat.format(calendar.time)
                fetchAndDisplayData()
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelTextSize(0f)
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(ContextCompat.getColor(this, android.R.color.black))

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val selectedState = e?.data as? String
                filterGridByState(selectedState)
            }

            override fun onNothingSelected() {
                filterGridByState(null)
            }
        })
    }

    private fun fetchAndDisplayData() {
        val entidadSeleccionada = spnerEntidades.selectedItem.toString()
        val fechaInicio = if(fechaDesde.text.toString() != "Desde") fechaDesde.text.toString() else null
        val fechaFin = if(fechaHasta.text.toString() != "Hasta") fechaHasta.text.toString() else null

        when(entidadSeleccionada) {
            RESERVATIONS -> reportesViewModel.fetchReportData(RESERVATIONS, fechaInicio, fechaFin)
            MATCHES -> reportesViewModel.fetchReportData(MATCHES, fechaInicio, fechaFin)
            USERS -> reportesViewModel.fetchReportData(USERS, null, null)
        }
    }

    private fun updatePieChart(chartData: Map<String, Int>) {
        val pieEntries = chartData.map { PieEntry(it.value.toFloat(), it.key, it.key) }

        val dataSet = PieDataSet(pieEntries, "Estados")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(this, android.R.color.black)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = pieData
        pieChart.setCenterTextSize(16f)
        pieChart.invalidate()
    }

    private fun filterGridByState(state: String?) {
        val entity = spnerEntidades.selectedItem.toString()



        val filteredData = if(state != null) {
            reportesViewModel.reportData.value?.filter {
                when (entity) {
                    RESERVATIONS -> it.fields[4] == state
                    MATCHES -> it.fields[2] == state
                    else -> false
                }
            } ?: emptyList()
        } else {
            reportesViewModel.reportData.value ?: emptyList()
        }

        val columnTitles = when (entity) {
            RESERVATIONS -> listOf("Fecha", "Hora inicio", "Hora fin", "Monto", "Estado", "Predio")
            MATCHES -> listOf("Fecha", "Hora", "Estado", "Participantes", "Tipo de cancha")
            USERS -> listOf("Nombre", "Apellido", "Reservas", "Partidos jugados", "Abandonos")
            else -> emptyList()
        }

        reportsAdapter.setData(columnTitles, filteredData, spnerEntidades.selectedItem.toString())
    }
}