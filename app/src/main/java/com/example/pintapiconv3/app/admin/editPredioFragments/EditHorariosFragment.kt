package com.example.pintapiconv3.app.admin.editPredioFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EditHorarioAdapter
import com.example.pintapiconv3.viewmodel.PredioViewModel

class EditHorariosFragment : Fragment() {

    private lateinit var rvHorarios: RecyclerView
    private lateinit var horarioAdapter: EditHorarioAdapter
    private lateinit var viewModel: PredioViewModel

    companion object {
        fun newInstance() : EditHorariosFragment {
            return EditHorariosFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_horarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHorarios = view.findViewById(R.id.rv_horarios)
        viewModel = ViewModelProvider(requireActivity())[PredioViewModel::class.java]

        horarioAdapter = EditHorarioAdapter(
            horarios = viewModel.horarios.value ?: mutableListOf(),
            onHorarioChanged = { horario ->
                viewModel.addOrUpdateHorario(horario)
            }
        )

        rvHorarios.adapter = horarioAdapter
        rvHorarios.layoutManager = LinearLayoutManager(requireContext())

        viewModel.horarios.observe(viewLifecycleOwner) { updatedHorarios ->
            horarioAdapter.updateHorarios(updatedHorarios.toMutableList())
        }
    }

}