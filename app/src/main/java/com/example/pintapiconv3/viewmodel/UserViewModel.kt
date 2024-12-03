package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> get() = _updateStatus

    private val _hasTeam = MutableLiveData<Boolean>()
    val hasTeam: LiveData<Boolean> get() = _hasTeam

    private val _isMatch = MutableLiveData<Boolean>()
    val isMatch: LiveData<Boolean> get() = _isMatch

    private val _isCaptain = MutableLiveData<Boolean>()
    val isCaptain: LiveData<Boolean> get() = _isCaptain

    fun setUser(user: User) {
        _user.value = user
    }

    fun updateUser(user: User) {
        _user.value = user
        viewModelScope.launch {
            val result = userRepository.updateUserDataInDB(user)
            _updateStatus.value = result
        }
    }

    fun setHasTeam(hasTeam: Boolean) {
        _hasTeam.value = hasTeam
    }

    fun setIsMatch(isMatch: Boolean) {
        _isMatch.value = isMatch
    }

    fun setIsCaptain(isCaptain: Boolean) {
        _isCaptain.value = isCaptain
    }


}

class UserViewModelFactory(private val userRepository: UserRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Uknown ViewModel class")
    }
}

object SharedUserData {
    var userViewModel: UserViewModel? = null

    fun init(viewModelStoreOwner: ViewModelStoreOwner, userRepository: UserRepository) {
        if(userViewModel == null) {
            val userViewModelFactory = UserViewModelFactory(userRepository)
            userViewModel = ViewModelProvider(viewModelStoreOwner, userViewModelFactory)[UserViewModel::class.java]
        }
    }

    fun clear() {
        userViewModel = null
    }
}