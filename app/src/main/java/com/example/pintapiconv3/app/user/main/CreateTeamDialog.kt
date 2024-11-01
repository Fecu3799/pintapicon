package com.example.pintapiconv3.app.user.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

class CreateTeamDialog : DialogFragment() {

    private lateinit var teamNameInput: EditText
    private lateinit var teamDescriptionInput: EditText
    private lateinit var createTeamButton: Button

    private val equipoRepository = EquipoRepository()

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_create_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        }

        teamNameInput = view.findViewById(R.id.et_teamName)
        teamDescriptionInput = view.findViewById(R.id.et_teamDescription)
        createTeamButton = view.findViewById(R.id.btn_createTeam)

        createTeamButton.setOnClickListener {
            val teamName = teamNameInput.text.toString()
            val teamDescription = teamDescriptionInput.text.toString()
            if(teamName.isNotEmpty() && teamDescription.isNotEmpty()) {
                createTeam(teamName, teamDescription)
            } else {
                Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTeam(teamName: String, teamDescription: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userID = userViewModel.user.value?.id ?: return@launch
                if(equipoRepository.createTeam(userID, teamName, teamDescription)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Equipo creado exitosamente", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al crear el equipo", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            } catch (e: SQLException) {
                withContext(Dispatchers.Main) {
                    Log.e("Database Error", "${e.message}")
                }
            }
        }

    }
}