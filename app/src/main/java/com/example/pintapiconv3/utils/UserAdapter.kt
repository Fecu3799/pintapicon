package com.example.pintapiconv3.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.User

class UserAdapter(context: Context, users: List<User>): ArrayAdapter<User>(context, 0, users) {

    private class ViewHolder(view: View) {
        val userName: TextView = view.findViewById(R.id.tv_userName)
        val userEmail: TextView = view.findViewById(R.id.tv_userEmail)
        val userRol: TextView = view.findViewById(R.id.tv_userRol)
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

        return view
    }
}