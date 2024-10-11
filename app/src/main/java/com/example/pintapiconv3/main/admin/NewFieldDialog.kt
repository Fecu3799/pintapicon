/*package com.example.pintapiconv3.main.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository

class NewFieldDialog: DialogFragment() {

    private val predioRepository = PredioRepository()
    private var fieldCreationListener: FieldCreationListener? = null

    interface FieldCreationListener {
        fun onFieldCreated(newField: Predio)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is FieldCreationListener) {
            fieldCreationListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_new_field_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {

    }


}*/