package com.edblueprint.app.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edblueprint.app.data.auth.AuthRepository
import kotlinx.coroutines.launch

/** Holds the signed-in / signed-out state for the whole app. */
class AppViewModel(private val repository: AuthRepository) : ViewModel() {

    val session = repository.state

    init {
        viewModelScope.launch { repository.restoreSession() }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }
}
