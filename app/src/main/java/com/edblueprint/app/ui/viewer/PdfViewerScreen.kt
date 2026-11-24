package com.edblueprint.app.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edblueprint.app.data.content.ContentRepository
import com.edblueprint.app.data.content.PdfSession
import com.edblueprint.app.data.remote.isForbidden
import com.edblueprint.app.data.remote.userMessage
import com.edblueprint.app.ui.app.Load
import com.edblueprint.app.ui.app.rememberContainer
import com.edblueprint.app.ui.components.BackHeader
import com.edblueprint.app.ui.components.BrandBackground
import com.edblueprint.app.ui.components.LoadContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Opens one PDF and closes (and deletes) it when the screen is left. */
class PdfViewModel(
    private val repository: ContentRepository,
    private val materialId: Long
) : ViewModel() {

    private val _state = MutableStateFlow<Load<PdfSession>>(Load.Loading)
    val state: StateFlow<Load<PdfSession>> = _state.asStateFlow()

    private var session: PdfSession? = null

    init {
        load()
    }

    fun load() {
        _state.value = Load.Loading
        viewModelScope.launch {
            _state.value = try {
                val opened = repository.openPdf(materialId)
                session = opened
                Load.Ready(opened)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Load.Failed(e.userMessage(), e.isForbidden())
            }
        }
    }

    override fun onCleared() {
        session?.close()
        session = null
    }
}

@Composable
fun PdfViewerScreen(
    materialId: Long,
    title: String,
    onBack: () -> Unit,
    onSubscriptionRequired: () -> Unit
) {
    val repository = rememberContainer().contentRepository
    val viewModel: PdfViewModel = viewModel(
        key = "pdf-$materialId",
        factory = viewModelFactory { initializer { PdfViewModel(repository, materialId) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The server said no: send the student to the subscription screen instead.
    val forbidden = (state as? Load.Failed)?.forbidden == true
    LaunchedEffect(forbidden) {
        if (forbidden) onSubscriptionRequired()
    }

    BrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            BackHeader(title = title, onBack = onBack)
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                LoadContent(state = state, onRetry = viewModel::load) { session ->
                    PdfPages(session)
                }
            }
        }
    }
}

@Composable
private fun PdfPages(session: PdfSession) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(count = session.pageCount, key = { it }) { index ->
                val bitmap by produceState<ImageBitmap?>(initialValue = null, index, widthPx) {
                    value = session.renderPage(index, widthPx).asImageBitmap()
                }
                val image = bitmap
                if (image != null) {
                    Image(
                        bitmap = image,
                        contentDescription = "Page ${index + 1} of ${session.pageCount}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    // Keeps the scroll position steady while the page is drawn.
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
