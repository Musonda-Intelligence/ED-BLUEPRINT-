package com.edblueprint.app.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.edblueprint.app.BuildConfig
import com.edblueprint.app.ui.app.rememberContainer
import com.edblueprint.app.ui.components.BackHeader
import com.edblueprint.app.ui.components.BrandBackground
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.theme.EdError

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Plays a lesson video from the protected endpoint, sending the login token with every request.
 * The server answers 403 for premium videos when the subscription is not active.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    materialId: Long,
    title: String,
    onBack: () -> Unit,
    onSubscriptionRequired: () -> Unit
) {
    val context = LocalContext.current
    val token = rememberContainer().authToken
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    val player = remember(attempt) {
        val headers = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
        val dataSourceFactory = DefaultHttpDataSource.Factory().setDefaultRequestProperties(headers)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri("${BuildConfig.API_BASE_URL}materials/$materialId/file"))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val cause = error.cause
                if (cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) {
                    onSubscriptionRequired()
                } else {
                    errorMessage = "This video could not be played. Check your connection and try again."
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // Leaving the screen always puts the phone back to its normal orientation.
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            BackHeader(title = title, onBack = onBack)

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        setFullscreenButtonClickListener { isFullScreen ->
                            viewContext.findActivity()?.requestedOrientation =
                                if (isFullScreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
                update = { it.player = player }
            )

            val message = errorMessage
            if (message != null) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EdCard {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EdError
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "Try again",
                            onClick = {
                                errorMessage = null
                                attempt += 1
                            }
                        )
                    }
                }
            }
        }
    }
}
