package com.edblueprint.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edblueprint.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            showError("Enter your email and password.")
            return
        }
        submit { repository.login(cleanEmail, password) }
    }

    fun register(fullname: String, email: String, password: String, confirm: String) {
        val cleanName = fullname.trim()
        val cleanEmail = email.trim()

        val problem = when {
            cleanName.length < 2 -> "Enter your full name."
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> "Enter a valid email address."
            password.length < MIN_PASSWORD_LENGTH ->
                "Your password must be at least $MIN_PASSWORD_LENGTH characters."
            password != confirm -> "The two passwords do not match."
            else -> null
        }
        if (problem != null) {
            showError(problem)
            return
        }
        submit { repository.register(cleanName, cleanEmail, password) }
    }

    private fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    /**
     * On success the repository switches the app to the signed-in screens,
     * so there is nothing more to do here. The server repeats every check.
     */
    private fun submit(call: suspend () -> Result<Unit>) {
        if (_state.value.loading) return
        _state.value = AuthUiState(loading = true)

        viewModelScope.launch {
            call()
                .onSuccess { _state.value = AuthUiState() }
                .onFailure { _state.value = AuthUiState(error = it.message ?: "Something went wrong.") }
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
