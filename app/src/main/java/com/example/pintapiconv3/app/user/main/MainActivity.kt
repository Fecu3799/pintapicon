package com.example.pintapiconv3.app.user.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.pintapiconv3.R
import com.example.pintapiconv3.databinding.ActivityMainBinding
import com.example.pintapiconv3.app.LoginActivity
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.NotifRepository
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.utils.JWToken
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.NotifViewModel
import com.example.pintapiconv3.viewmodel.NotifViewModelFactory
import com.example.pintapiconv3.viewmodel.PartidoViewModel
import com.example.pintapiconv3.viewmodel.PartidoViewModelFactory
import com.example.pintapiconv3.viewmodel.SharedMatchData
import com.example.pintapiconv3.viewmodel.SharedNotifData
import com.example.pintapiconv3.viewmodel.SharedUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateNotificationsRunnable: Runnable
    private val updateInterval: Long = 10000

    private var dialog: Dialog? = null
    private var isSessionDialogShown = false

    private val userRepository = UserRepository()
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(userRepository)
    }
    private val notifRepository = NotifRepository()
    private val notifViewModel: NotifViewModel by viewModels {
        NotifViewModelFactory(notifRepository)
    }
    private val partidoRepository = PartidoRepository()
    private val partidoViewModel: PartidoViewModel by viewModels {
        PartidoViewModelFactory(partidoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        SharedUserData.init(this, userRepository)
        SharedNotifData.init(this, notifRepository)
        SharedMatchData.init(this, partidoRepository)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .commit()
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        setupUI()
        setupNavigation()
        loadUserData()
        setupNotificationObserver()
        setupNotificationUpdater()
    }

    private fun setupUI() {
        binding.mainMenu.btnMenu.setOnClickListener {
            if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            checkSession()
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

        binding.navMenu.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_partidos -> { /* Handle partidos action */ }
                R.id.item_canchas -> { /* Handle canchas action */ }
                R.id.item_equipos -> { showTeamListDialog() }
                R.id.item_amigos -> { /* Handle amigos action */ }
                R.id.item_ayuda -> { /* Handle ayuda action */ }
                R.id.item_ajustes -> { /* Handle ajustes action */ }
                R.id.item_signout -> logoutUser()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if(item.itemId != R.id.item_signout) checkSession()
            true
        }
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

    private fun setupNotificationObserver() {
        notifViewModel.hasNotification.observe(this) { hasNotifications ->
            updateNotificationIcon(hasNotifications)
        }

    }

    private fun updateNotificationIcon(hasNotification: Boolean) {
        val notifMenuItem = binding.bottomNav.menu.findItem(R.id.nav_notif)
        if(hasNotification) {
            notifMenuItem.setIcon(R.drawable.ic_notif_on)
        } else {
            notifMenuItem.setIcon(R.drawable.ic_notif_off)
        }
    }

    private fun setupNotificationUpdater() {
        updateNotificationsRunnable = object : Runnable {
            override fun run() {
                val userId = userViewModel.user.value?.id
                if(userId != null) {
                    Log.d("MainActivity", "Checking for pending notifications for user $userId")
                    notifViewModel.checkPendingNotifications(userId)
                } else {
                    Log.d("MainActivity", "User ID is null")
                }
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    private fun switchFragment(fragment: Fragment, titleRes: Int, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment)
        if(addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        checkSession()
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

    private fun showTeamListDialog() {
        val teamListDialog = TeamListDialog()
        teamListDialog.show(supportFragmentManager, "TeamListDialog")
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun logoutUser() {
        userRepository.clearUserData(this)
        userRepository.clearSession(this)

        SharedUserData.clear()

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

    override fun onResume() {
        super.onResume()
        handler.post(updateNotificationsRunnable)

        val userId = userViewModel.user.value?.id
        if(userId != null) {
            notifViewModel.checkPendingNotifications(userId)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateNotificationsRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    fun checkSession() {
        val token = userRepository.getSession(this)
        if(token == null || !JWToken.validateToken(token)) {
            Log.d("MainActivity", "Token invalido o expirado")
            showSessionExpiredDialog()
        }
        userRepository.renewSession(this)
        Log.d("MainActivity", "Token renovado")
    }
}