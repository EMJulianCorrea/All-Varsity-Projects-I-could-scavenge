package com.example.palisadeinventoryapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palisadeinventoryapp.data.AppDatabase
import com.example.palisadeinventoryapp.data.entity.User
import com.example.palisadeinventoryapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authMessage = MutableStateFlow("")
    val authMessage: StateFlow<String> = _authMessage

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authMessage.value = "Please enter both username and password"
            _loginSuccess.value = false
            return
        }
        viewModelScope.launch {
            val hashed = AppDatabase.hashPassword(password)
            val user = userRepository.login(username, hashed)
            if (user == null) {
                _authMessage.value = "Incorrect username or password"
                _loginSuccess.value = false
            } else {
                _currentUser.value = user
                _authMessage.value = "Login successful"
                _loginSuccess.value = true
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String, role: String = "staff") {
        if (username.isBlank() || password.isBlank()) {
            _authMessage.value = "Please fill in all fields"
            return
        }
        if (password != confirmPassword) {
            _authMessage.value = "Passwords do not match"
            return
        }
        viewModelScope.launch {
            val existing = userRepository.getUserByUsername(username)
            if (existing != null) {
                _authMessage.value = "That username is already taken"
                return@launch
            }
            userRepository.insert(
                User(
                    username = username,
                    password = AppDatabase.hashPassword(password),
                    role = role
                )
            )
            _authMessage.value = "Account created. Please log in."
            _registerSuccess.value = true
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginSuccess.value = false
        _authMessage.value = ""
    }

    fun clearMessage() {
        _authMessage.value = ""
        _registerSuccess.value = false
    }

    companion object {
        fun Factory(userRepository: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(userRepository) as T
                }
            }
    }
}