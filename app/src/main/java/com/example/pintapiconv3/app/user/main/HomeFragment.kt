package com.example.pintapiconv3.app.user.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.app.user.MainActivity
import com.example.pintapiconv3.models.Partido
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.viewmodel.SharedMatchData
import com.example.pintapiconv3.viewmodel.SharedUserData
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var teamButton: ImageButton
    private lateinit var txtTeamButton: TextView
    private lateinit var matchButton: ImageButton
    private lateinit var txtMatchButton: TextView
    private lateinit var userViewModel: UserViewModel

    private val equipoRepository = EquipoRepository()
    private val partidoRepository = PartidoRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = SharedUserData.userViewModel!!

        //val sharedPref = requireContext().getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
        //val partidoId = sharedPref.getInt("partidoId", -1)

        teamButton = view.findViewById(R.id.btn_equipo)
        matchButton = view.findViewById(R.id.btn_partido)
        txtTeamButton = view.findViewById(R.id.txt_btn_equipo)
        txtMatchButton = view.findViewById(R.id.txt_btn_partido)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                if(userViewModel.hasTeam.value == null) {
                    checkIfUserHasTeam(it.id)
                } else {
                    setupTeamButton(userViewModel.hasTeam.value == true)
                }

                if(userViewModel.isMatch.value == null) {
                    checkIfUserIsMatch(it.id)
                } else {
                    setupMatchButton(userViewModel.isMatch.value == true)
                }
                /*if(partidoId != -1) {
                    setupMatchButton(true)
                }*/
            }
        }

        userViewModel.hasTeam.observe(viewLifecycleOwner) { hasTeam ->
            setupTeamButton(hasTeam)
        }
        userViewModel.isMatch.observe(viewLifecycleOwner) { isMatch ->
            setupMatchButton(isMatch)
        }

        parentFragmentManager.setFragmentResultListener("matchCreated", viewLifecycleOwner) { _, _ ->
            userViewModel.setIsMatch(true)
        }
    }

    override fun onResume() {
        super.onResume()
        userViewModel.user.value?.let { user ->
            checkIfUserIsMatch(user.id)
        }
    }


    private fun checkIfUserHasTeam(userID: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            val hasTeam = withContext(Dispatchers.IO) {
                equipoRepository.hasTeam(userID)
            }
            userViewModel.setHasTeam(hasTeam)
            setupTeamButton(hasTeam)
        }
    }

    private fun checkIfUserIsMatch(userId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            val isMatch = withContext(Dispatchers.IO) {
                partidoRepository.isParticipantInActiveMatch(userId)
            }
            userViewModel.setIsMatch(isMatch)
            setupMatchButton(isMatch)
        }
    }

    private fun setupTeamButton(hasTeam: Boolean) {
        if(hasTeam) {
            txtTeamButton.text = "Mi equipo"
            teamButton.setOnClickListener {
                val intent = Intent(requireContext(), TeamManagementActivity::class.java)
                intent.putExtra("userId", userViewModel.user.value?.id)
                startActivity(intent)
            }
        } else {
            txtTeamButton.text = "Crear un equipo"
            teamButton.setOnClickListener {
                val dialog = CreateTeamDialog()
                dialog.show(childFragmentManager, "CreateTeamDialog")
            }
        }
    }

    private fun setupMatchButton(isMatch: Boolean) {
        if (isMatch) {
            txtMatchButton.text = "Ver partido"
            matchButton.setOnClickListener {
                participantInMatch()
            }
        } else {
            txtMatchButton.text = "Crear partido"
            matchButton.setOnClickListener {
                val dialog = CreateMatchDialog()
                dialog.show(childFragmentManager, "CreateMatchDialog")
            }
        }

        /*if(isMatch) {
            txtMatchButton.text = "Ver partido"
            matchButton.setOnClickListener {
                val intent = Intent(requireContext(), MatchDetailsActivity::class.java)
                intent.putExtra("userId", userViewModel.user.value?.id)
                startActivity(intent)
            }
        } else {
            txtMatchButton.text = "Crear partido"
            matchButton.setOnClickListener {
                val dialog = CreateMatchDialog()
                dialog.show(childFragmentManager, "CreateMatchDialog")
            }
        }*/
    }

    private fun participantInMatch() {
        val sharedPref = requireContext().getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
        val currentUserId = userViewModel.user.value?.id

        val partidoId = sharedPref.getInt("partidoId_$currentUserId", -1)

        if(partidoId != -1) {
            lifecycleScope.launch {
                val isParticipant = withContext(Dispatchers.IO) {
                    partidoRepository.esParticipanteDelPartido(partidoId, currentUserId!!)
                }

                if(isParticipant) {
                    val intent = Intent(requireContext(), MatchDetailsActivity::class.java)
                    //intent.putExtra("userId", currentUserId)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Has sido expulsado del partido", Toast.LENGTH_SHORT).show()
                    with(sharedPref.edit()) {
                        remove("partidoId_$currentUserId")
                        apply()
                    }
                    userViewModel.setIsMatch(false)
                    SharedMatchData.clear()
                }
            }
        } else {
            userViewModel.setIsMatch(false)
            SharedMatchData.clear()
        }
    }
}