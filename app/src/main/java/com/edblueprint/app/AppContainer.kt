package com.edblueprint.app

import android.content.Context
import com.edblueprint.app.data.auth.AuthRepository
import com.edblueprint.app.data.content.ContentRepository
import com.edblueprint.app.data.local.SessionStore
import com.edblueprint.app.data.remote.ApiClient
import kotlinx.serialization.json.Json

/** Creates the shared objects the app needs, once, when the app starts. */
class AppContainer(context: Context) {

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val sessionStore = SessionStore(context, json)
    private val api = ApiClient.create(sessionStore, json)

    val authRepository = AuthRepository(api, sessionStore, json)
    val contentRepository = ContentRepository(api, context.cacheDir)

    /** Used by the video player, which makes its own requests and must send the login token. */
    val authToken: String?
        get() = sessionStore.token
}
