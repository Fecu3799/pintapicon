package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> get() = _updateStatus

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

}

class UserViewModelFactory(private val userRepository: UserRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Uknown ViewModel class")
    }
}