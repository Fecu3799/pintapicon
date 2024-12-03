package com.example.pintapiconv3.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.pintapiconv3.repository.NotifRepository
import com.example.pintapiconv3.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotifViewModel(private val notifRepository: NotifRepository) : ViewModel() {

    private val _hasNotifications = MutableLiveData<Boolean>()
    val hasNotification: LiveData<Boolean> get() = _hasNotifications

    fun checkPendingNotifications(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPending = notifRepository.hasPendingNotifications(userId)
            Log.d("NotifViewModel", "User: $userId has pending notifications: $hasPending")
            withContext(Dispatchers.Main) {
                _hasNotifications.postValue(hasPending)
            }
        }
    }
}

class NotifViewModelFactory(private val notifRepository: NotifRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NotifViewModel::class.java)) {
            return NotifViewModel(notifRepository) as T
        }
        throw IllegalArgumentException("Uknown ViewModel class")
    }
}



object SharedNotifData {
    var notifViewModel: NotifViewModel? = null

    fun init(viewModelStoreOwner: ViewModelStoreOwner, notifRepository: NotifRepository) {
        if(notifViewModel == null) {
            val notifViewModelFactory = NotifViewModelFactory(notifRepository)
            notifViewModel = ViewModelProvider(viewModelStoreOwner, notifViewModelFactory)[NotifViewModel::class.java]
        }
    }

    fun clear() {
        notifViewModel = null
    }
}