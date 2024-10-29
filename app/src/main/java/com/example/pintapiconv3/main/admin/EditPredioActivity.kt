package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.main.admin.fragments.EditCanchasFragment
import com.example.pintapiconv3.main.admin.fragments.EditHorariosFragment
import com.example.pintapiconv3.main.admin.fragments.EditPredioFragment
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.DireccionRepository
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.PredioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException

class EditPredioActivity : AppCompatActivity() {

    private lateinit var predio: Predio
    private lateinit var direccion: Direccion

    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnCancelar: Button

    private lateinit var fragmentList: List<Fragment>

    private var currentFragmentIndex = 0

    private val direccionRepository = DireccionRepository()
    private val predioRepository = PredioRepository()

    private lateinit var predioViewModel: PredioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_predio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnAnterior = findViewById(R.id.btn_anterior)
        btnSiguiente = findViewById(R.id.btn_siguiente)
        btnGuardarCambios = findViewById(R.id.btn_guardar_cambios)
        btnCancelar = findViewById(R.id.btn_cancel)

        predioViewModel = ViewModelProvider(this)[PredioViewModel::class.java]

        predio = intent.getSerializableExtra("EXTRA_PREDIO") as Predio
        direccion = intent.getSerializableExtra("EXTRA_DIRECCION") as Direccion

        predioViewModel.updatePredio(predio)
        predioViewModel.updateDireccion(direccion)

        CoroutineScope(Dispatchers.Main).launch {
            val canchas = withContext(Dispatchers.IO) {
                predioRepository.getCanchasByPredio(predio.id)
            }
            predioViewModel.updateCanchas(canchas.toMutableList())
        }

        fragmentList = listOf (
            EditPredioFragment.newInstance(),
            EditCanchasFragment.newInstance(),
            EditHorariosFragment.newInstance()
        )

        if(savedInstanceState == null) {
            replaceFragment(fragmentList[currentFragmentIndex])
        }

        setupNavigationButtons()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setupNavigationButtons() {
        btnAnterior.setOnClickListener {
            if(currentFragmentIndex > 0) {
                currentFragmentIndex--
                replaceFragment(fragmentList[currentFragmentIndex])
                updateNavigationButtons()
            }
        }

        btnSiguiente.setOnClickListener {
            if(currentFragmentIndex < fragmentList.size -1) {
                currentFragmentIndex++
                replaceFragment(fragmentList[currentFragmentIndex])
                updateNavigationButtons()
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        when(currentFragmentIndex) {
            0 -> {
                btnCancelar.visibility = View.VISIBLE
                btnAnterior.visibility = View.GONE
                btnSiguiente.visibility = View.VISIBLE
                btnGuardarCambios.visibility = View.GONE
            }
            fragmentList.size - 1 -> {
                btnCancelar.visibility = View.GONE
                btnAnterior.visibility = View.VISIBLE
                btnSiguiente.visibility = View.GONE
                btnGuardarCambios.visibility = View.VISIBLE
            }
            else -> {
                btnCancelar.visibility = View.GONE
                btnAnterior.visibility = View.VISIBLE
                btnSiguiente.visibility = View.VISIBLE
                btnGuardarCambios.visibility = View.GONE
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun guardarCambios() {
        val updatedPredio = predioViewModel.predio.value
        val updatedDireccion = predioViewModel.direccion.value
        val updatedCanchas = predioViewModel.canchas.value
        val deletedCanchas = predioViewModel.deletedCanchas.value
        val updatedHorarios = predioViewModel.horarios.value

        if(updatedPredio != null && updatedDireccion != null && updatedCanchas != null && updatedHorarios != null) {
            CoroutineScope(Dispatchers.IO).launch {
                var conn: Connection? = null

                try {
                    conn = DBConnection.getConnection()
                    conn?.autoCommit = false

                    if(conn != null) {
                        val direccionActualizada = direccionRepository.updateDireccionWithConnection(conn, updatedDireccion)
                        if(!direccionActualizada) throw SQLException("Error al actualizar la direccion")

                        val predioActualizado = predioRepository.updatePredioWithConnection(conn, updatedPredio)
                        if(!predioActualizado) throw SQLException("Error al actualizar el predio")

                        deletedCanchas?.forEach { cancha ->
                            val canchasEliminadas = predioRepository.deleteCanchaWithConnection(conn, cancha)
                            if(!canchasEliminadas) throw SQLException("Error al eliminar cancha ${cancha.tipoCancha}")
                        }

                        updatedCanchas.filter { it.isNew }.forEach { cancha ->
                            val canchaInsertada = predioRepository.insertCanchaWithConnection(conn, cancha)
                            if(!canchaInsertada) throw SQLException("Error al guardar cancha ${cancha.tipoCancha}")
                        }

                        updatedCanchas.filter {!it.isNew}.forEach { cancha ->
                            val canchaActualizada = predioRepository.updateCanchaWithConnection(conn, cancha)
                            if(!canchaActualizada) throw SQLException("Error al actualizar cancha ${cancha.tipoCancha}")
                        }

                        updatedHorarios.forEach { horario ->
                            val horarioActualizado = predioRepository.updateHorarioPredioWithConnection(conn, horario)
                            if(!horarioActualizado) throw SQLException("Error al actualizar horario ${horario.dia}")
                        }

                        conn.commit()

                        withContext(Dispatchers.Main) {
                            showToast("Cambios guardados correctamente")
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast("Error al guardar cambios")
                            Log.e("Database Error", "Database connection null")
                        }
                    }
                } catch (e: SQLException) {
                    conn?.rollback()
                    withContext(Dispatchers.Main) {
                        showToast("Error al guardar cambios")
                        Log.e("Database Error", e.message ?: "Unknown error")
                    }
                } finally {
                    conn?.autoCommit = true
                    conn?.close()
                }
            }
        } else {
            showToast("Error. Verifique los datos ingresados")
        }
    }

}