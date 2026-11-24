package com.edblueprint.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.edblueprint.app.R
import com.edblueprint.app.ui.theme.EdBlack
import com.edblueprint.app.ui.theme.EdMaroon

/** The full ED Blueprint logo (emblem, name and tagline). */
@Composable
fun EdLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ed_logo),
        contentDescription = stringResource(R.string.logo_description),
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/** Black screen background with a faint maroon glow at the top, like the logo shading. */
@Composable
fun BrandBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EdMaroon.copy(alpha = 0.55f), EdBlack),
                    startY = 0f,
                    endY = 1400f
                )
            ),
        content = content
    )
}
