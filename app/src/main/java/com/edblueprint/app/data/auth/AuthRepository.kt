package com.edblueprint.app.data.auth

import com.edblueprint.app.data.local.SessionStore
import com.edblueprint.app.data.remote.ApiService
import com.edblueprint.app.data.remote.AuthResponse
import com.edblueprint.app.data.remote.ErrorResponse
import com.edblueprint.app.data.remote.LoginRequest
import com.edblueprint.app.data.remote.RegisterRequest
import com.edblueprint.app.data.remote.UserDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: UserDto) : SessionState
}

/** An error whose message is safe to show to the person using the app. */
class AuthException(message: String) : Exception(message)

class AuthRepository(
    private val api: ApiService,
    private val session: SessionStore,
    private val json: Json
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /** Called once at app start: checks the saved token with the server. */
    suspend fun restoreSession() {
        if (session.token == null) {
            _state.value = SessionState.SignedOut
            return
        }
        try {
            val user = api.me().user
            session.saveUser(user)
            _state.value = SessionState.SignedIn(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // Token expired or revoked on the server.
                session.clear()
                _state.value = SessionState.SignedOut
            } else {
                useCachedUser()
            }
        } catch (e: Exception) {
            useCachedUser()
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> =
        authenticate { api.login(LoginRequest(email, password)) }

    /** The server logs the new student in straight away, so no second login is needed. */
    suspend fun register(fullname: String, email: String, password: String): Result<Unit> =
        authenticate { api.register(RegisterRequest(fullname, email, password)) }

    suspend fun logout() {
        try {
            api.logout()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Signing out on this device must work even without internet.
        }
        session.clear()
        _state.value = SessionState.SignedOut
    }

    private suspend fun authenticate(call: suspend () -> AuthResponse): Result<Unit> =
        try {
            val response = call()
            session.saveSession(response.token, response.user)
            _state.value = SessionState.SignedIn(response.user)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AuthException(describe(e)))
        }

    private fun useCachedUser() {
        val cached = session.cachedUser
        _state.value = if (cached != null) SessionState.SignedIn(cached) else SessionState.SignedOut
    }

    private fun describe(error: Exception): String = when (error) {
        is HttpException -> describeHttp(error)
        is IOException -> "Cannot reach the server. Check your internet connection and try again."
        else -> "Something went wrong. Please try again."
    }

    private fun describeHttp(error: HttpException): String {
        val body = error.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { json.decodeFromString<ErrorResponse>(it) }.getOrNull() }
        val fieldError = parsed?.errors?.values?.firstOrNull()?.firstOrNull()
        return fieldError
            ?: parsed?.message
            ?: when (error.code()) {
                401 -> "Incorrect email or password."
                422 -> "Please check the details you entered."
                429 -> "Too many attempts. Please wait a moment and try again."
                else -> "The server had a problem (${error.code()}). Please try again."
            }
    }
}
