package com.edblueprint.app.data.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.edblueprint.app.data.remote.UserDto
import kotlinx.serialization.json.Json

/**
 * Keeps the login token and last known user on the device, encrypted.
 * The cached user is only used to show something while offline;
 * the server still decides what the user may access.
 */
class SessionStore(context: Context, private val json: Json) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    val cachedUser: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let {
            runCatching { json.decodeFromString<UserDto>(it) }.getOrNull()
        }

    fun saveSession(token: String, user: UserDto) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, json.encodeToString(UserDto.serializer(), user))
        }
    }

    fun saveUser(user: UserDto) {
        prefs.edit {
            putString(KEY_USER, json.encodeToString(UserDto.serializer(), user))
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        const val FILE_NAME = "ed_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }
}
