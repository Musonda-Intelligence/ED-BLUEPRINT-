package com.edblueprint.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edblueprint.app.ui.app.Load
import com.edblueprint.app.ui.theme.EdError
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdOutline
import com.edblueprint.app.ui.theme.EdSurface
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

/** A rounded dark card. Pass [onClick] to make the whole card tappable. */
@Composable
fun EdCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: androidx.compose.ui.graphics.Color = EdOutline,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = EdSurface)
    val border = BorderStroke(1.dp, borderColor)
    val shape = RoundedCornerShape(16.dp)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            border = border,
            content = {
                Column(modifier = Modifier.padding(16.dp), content = content)
            }
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            border = border,
            content = {
                Column(modifier = Modifier.padding(16.dp), content = content)
            }
        )
    }
}

/** Outlined orange button for secondary actions. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (enabled) EdOrange else EdOutline)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) EdOrange else EdTextSecondary
        )
    }
}

/** Shows a spinner, an error with a retry button, or the loaded content. */
@Composable
fun <T> LoadContent(
    state: Load<T>,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit
) {
    when (state) {
        Load.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = EdOrange,
                strokeWidth = 3.dp
            )
        }
        is Load.Failed -> EdCard {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = EdError
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(text = "Try again", onClick = onRetry)
        }
        is Load.Ready -> content(state.data)
    }
}

/** Page with a back arrow and title, for screens opened from a tab. Scrolls. */
@Composable
fun DetailScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            BackHeader(title = title, onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = EdTextPrimary
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = EdTextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}
