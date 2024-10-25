package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.main.admin.fragments.EditCanchasFragment
import com.example.pintapiconv3.main.admin.fragments.EditHorariosFragment
import com.example.pintapiconv3.main.admin.fragments.EditPredioFragment
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPredioActivity : AppCompatActivity() {

    private lateinit var predio: Predio
    private lateinit var direccion: Direccion

    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnGuardarCambios: Button

    private lateinit var fragmentList: List<Fragment>

    private var currentFragmentIndex = 0

    private val predioRepository = PredioRepository()

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

        predio = intent.getSerializableExtra("EXTRA_PREDIO") as Predio
        direccion = intent.getSerializableExtra("EXTRA_DIRECCION") as Direccion

        fragmentList = listOf (
            EditPredioFragment.newInstance(predio, direccion),
            EditCanchasFragment.newInstance(predio.id),
            EditHorariosFragment.newInstance(predio.id)
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

        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        when(currentFragmentIndex) {
            0 -> {
                btnAnterior.visibility = View.GONE
                btnSiguiente.visibility = View.VISIBLE
                btnGuardarCambios.visibility = View.GONE
            }
            fragmentList.size - 1 -> {
                btnAnterior.visibility = View.VISIBLE
                btnSiguiente.visibility = View.GONE
                btnGuardarCambios.visibility = View.VISIBLE
            }
            else -> {
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
        val updatedPredio = (fragmentList[0] as? EditPredioFragment)?.getUpdatedPredio()
        val updatedDireccion = (fragmentList[0] as? EditPredioFragment)?.getUpdatedDireccion()

        if(updatedPredio != null && updatedDireccion != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val predioActualizado = predioRepository.updatePredio(updatedPredio)
                val direccionActualizada = predioRepository.updateDireccion(updatedDireccion)

                withContext(Dispatchers.Main) {
                    if(predioActualizado && direccionActualizada) {
                        showToast("Cambios guardados")
                    } else {
                        showToast("Error al guardar los cambios")
                    }
                }
            }
        } else {
            showToast("ERROR: Verifique los datos")
        }
    }
}