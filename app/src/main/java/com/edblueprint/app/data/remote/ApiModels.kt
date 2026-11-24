package com.edblueprint.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)

/** Returned by the server after a successful login or registration. */
@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class MeResponse(
    val user: UserDto
)

/**
 * The signed-in user as the server describes them.
 * Role and subscription come from the server only; the app never decides them.
 */
@Serializable
data class UserDto(
    val id: Long,
    val fullname: String,
    val email: String,
    val role: String = "student",
    @SerialName("subscription_active") val subscriptionActive: Boolean = false,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null
) {
    val isAdmin: Boolean get() = role == "admin"
    val firstName: String get() = fullname.trim().substringBefore(' ')
}

/** Error body in the Laravel style: a message plus optional per-field errors. */
@Serializable
data class ErrorResponse(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)
