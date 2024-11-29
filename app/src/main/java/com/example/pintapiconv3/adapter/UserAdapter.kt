package com.example.pintapiconv3.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Const.AccountStatus.BLOCKED
import com.example.pintapiconv3.utils.Const.AccountStatus.DELETED
import com.example.pintapiconv3.utils.Const.AccountStatus.NOT_VERIFIED
import com.example.pintapiconv3.utils.Const.AccountStatus.SUSPENDED
import com.example.pintapiconv3.utils.Const.AccountStatus.VERIFIED

class UserAdapter(context: Context, users: List<User>): ArrayAdapter<User>(context, 0, users) {

    private class ViewHolder(view: View) {
        val userName: TextView = view.findViewById(R.id.tv_userName)
        val userEmail: TextView = view.findViewById(R.id.tv_userEmail)
        val userRol: TextView = view.findViewById(R.id.tv_userRole)
        val userState: TextView = view.findViewById(R.id.tv_userState)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val holder: ViewHolder

        if(convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.abm_user_list_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val user = getItem(position)
        Log.d("UserAdapter", "Usuario: ${user?.nombre}, ${user?.email}, ${user?.isAdmin}")

        holder.userName.text = user?.nombre
        holder.userEmail.text = user?.email
        holder.userRol.text = if (user?.isAdmin == 0) "Usuario" else "Administrador"
        holder.userState.text = when (user?.estado) {
            NOT_VERIFIED -> "No verificada"
            VERIFIED -> "Verificada"
            DELETED -> "Eliminada"
            SUSPENDED -> "Suspendida"
            BLOCKED -> "Bloqueada"
            else -> "Desconocido"
        }

        if(user?.estado == DELETED)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.lightgrey))
        else
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

        return view
    }
}