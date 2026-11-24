package com.edblueprint.app.ui.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edblueprint.app.AppContainer
import com.edblueprint.app.ui.auth.AuthViewModel

/** One place that says how each ViewModel is built. Add new ViewModels here. */
fun edViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { AppViewModel(container.authRepository) }
    initializer { AuthViewModel(container.authRepository) }
}
