package com.edblueprint.app.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edblueprint.app.AppContainer
import com.edblueprint.app.EdBlueprintApp
import com.edblueprint.app.data.remote.isForbidden
import com.edblueprint.app.data.remote.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The three things a screen that loads from the server can be showing. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Failed(val message: String, val forbidden: Boolean = false) : Load<Nothing>
    data class Ready<T>(val data: T) : Load<T>
}

/** Loads one thing from the server, and can load it again. Used by every list screen. */
class LoadViewModel<T>(private val loader: suspend () -> T) : ViewModel() {

    private val _state = MutableStateFlow<Load<T>>(Load.Loading)
    val state: StateFlow<Load<T>> = _state.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        _state.value = Load.Loading
        viewModelScope.launch {
            _state.value = try {
                Load.Ready(loader())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Load.Failed(e.userMessage(), e.isForbidden())
            }
        }
    }
}

@Composable
fun rememberContainer(): AppContainer {
    val context = LocalContext.current
    return remember { (context.applicationContext as EdBlueprintApp).container }
}

/** Gives a screen its own LoadViewModel. The [key] must be different for each kind of data. */
@Composable
@Suppress("UNCHECKED_CAST")
fun <T> rememberLoader(key: String, loader: suspend () -> T): LoadViewModel<T> {
    val factory = viewModelFactory {
        initializer { LoadViewModel(loader) }
    }
    return viewModel(LoadViewModel::class.java, key = key, factory = factory) as LoadViewModel<T>
}
