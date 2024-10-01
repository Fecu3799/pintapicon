package com.example.pintapiconv3.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.databinding.ActivityMainBinding
import com.example.pintapiconv3.utils.UserRepository
import com.example.pintapiconv3.utils.UserViewModel
import com.example.pintapiconv3.utils.UserViewModelFactory
import com.example.pintapiconv3.main.fragments.HomeFragment
import com.example.pintapiconv3.main.fragments.NotifFragment
import com.example.pintapiconv3.main.fragments.ProfileFragment
import com.example.pintapiconv3.main.fragments.SearchFragment
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.JWToken
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private var sqlServerHelper = SQLServerHelper()
    private var dialog: Dialog? = null
    private var isSessionDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        userRepository = UserRepository()
        val viewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        setupUI()
        setupNavigation()
        loadUserData()

        if (savedInstanceState == null) {
            switchFragment(HomeFragment(), R.string.home)
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun setupUI() {
        binding.mainMenu.btnMenu.setOnClickListener {
            if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item, ->
            when(item.itemId) {
                R.id.nav_home -> switchFragment(HomeFragment(), R.string.home)
                R.id.nav_search -> switchFragment(SearchFragment(), R.string.search)
                R.id.nav_notif -> switchFragment(NotifFragment(), R.string.notif)
                R.id.nav_profile -> switchFragment(ProfileFragment(), R.string.profile)
            }
            true
        }

        binding.navMenu.setNavigationItemSelectedListener {
            handleNavigationItem(it.itemId)
            true
        }

    }

    private fun handleNavigationItem(itemId: Int) {
        when (itemId) {
            R.id.item_partidos -> { /* Handle partidos action */ }
            R.id.item_canchas -> { /* Handle canchas action */ }
            R.id.item_equipos -> { /* Handle equipos action */ }
            R.id.item_amigos -> { /* Handle amigos action */ }
            R.id.item_ayuda -> { /* Handle ayuda action */ }
            R.id.item_ajustes -> { /* Handle ajustes action */ }
            R.id.item_signout -> logoutUser()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }


    private fun setupNavigation() {
        val headerView = binding.navMenu.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.tv_userName)
        val userRolTextView: TextView = headerView.findViewById(R.id.user_rol)

        userViewModel.user.observe(this, Observer { user ->
            user?.let {
                userNameTextView.text = it.nombre
                if(it.isAdmin != 0) userRolTextView.text = "Administrador"
                else userRolTextView.text = "Usuario"
            }
        })
    }

    private fun switchFragment(fragment: Fragment, titleRes: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
        binding.mainMenu.tvMainTitle.setText(titleRes)
    }

    private fun loadUserData() {
        val loginData = getLoginData()
        if(loginData != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val userData = userRepository.getUserData(loginData.first, loginData.second)
                withContext(Dispatchers.Main) {
                    if(userData.isNotEmpty()) {
                        setViewModel(userData)
                    } else {
                        showToast("Error al cargar datos del usuario")
                    }
                }
            }
        }
    }

    private fun getLoginData(): Pair<String, String>? {
        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
        val email = sharedPref.getString("userEmail", null)
        val password = sharedPref.getString("userPassword", null)

        return if (email != null && password != null) {
            email to password
        } else {
            redirectToLogin()
            null
        }
    }


    private fun setViewModel(userData: HashMap<String, String>) {
        val user = User (
            id = userData["id"]?.toInt() ?: 0,
            email = userData["email"] ?: "",
            password = userData["contraseña"] ?: "",
            nombre = userData["nombre"] ?: "",
            apellido = userData["apellido"] ?: "",
            fechaNacimiento = userData["fecha_nacimiento"] ?: "",
            telefono = userData["telefono"] ?: "",
            idDireccion = userData["idDireccion"]?.toInt() ?: 0,
            calle = userData["calle"] ?: "",
            numero = userData["numero"]?.toInt() ?: 0,
            idBarrio = userData["idBarrio"]?.toInt() ?: 0,
            barrio = userData["barrio"] ?: "",
            localidad = userData["localidad"] ?: "",
            provincia = userData["provincia"] ?: "",
            pais = userData["pais"] ?: "",
            estado = userData["idEstado"]?.toInt() ?: 0,
            genero = userData["idGenero"]?.toInt() ?: 0,
            habilidad = userData["idHabilidad"]?.toInt() ?: 0,
            posicion = userData["idPosicion"]?.toInt() ?: 0,
            isAdmin = userData["isAdmin"]?.toInt() ?: 0
        )

        userViewModel.setUser(user)
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun logoutUser() {
        userRepository.clearUserData(this)
        userRepository.clearSession(this)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        finish()
    }

    private fun showSessionExpiredDialog() {
        if(!isFinishing && !isSessionDialogShown) {
            isSessionDialogShown = true
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sesion expirada")
            builder.setMessage("Su tiempo de sesión ha expirado. Por favor, inicie sesión nuevamente")
            builder.setCancelable(false)
            builder.setPositiveButton("Aceptar") { _, _ ->
                logoutUser()
            }
            builder.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val token = userRepository.getSession(this)
        Log.d("MainActivity", "Token obtenido: $token")

        if(token == null || !JWToken.validateToken(token)) {
            Log.d("MainActivity", "Token invalido o expirado")
            showSessionExpiredDialog()
            return false
        }

        if(event?.action == MotionEvent.ACTION_DOWN) {
            userRepository.renewSession(this)
        }

        return super.dispatchTouchEvent(event)
    }
}