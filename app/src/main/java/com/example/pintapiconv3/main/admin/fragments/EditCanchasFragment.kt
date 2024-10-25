package com.example.pintapiconv3.main.admin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio

class EditCanchasFragment : Fragment() {

    companion object {
        private const val ARG_ID_PREDIO = "ARG_ID_PREDIO"

        fun newInstance(idPredio: Int) : EditCanchasFragment {
            val fragment = EditCanchasFragment()
            val args = Bundle()
            args.putSerializable(ARG_ID_PREDIO, idPredio)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_canchas, container, false)
    }

}