package com.edblueprint.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edblueprint.app.data.remote.MaterialDto
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.EmptyState
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

/** One lesson, document or paper in a list. Locked items show a padlock. */
@Composable
fun MaterialRow(material: MaterialDto, onClick: () -> Unit) {
    EdCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = if (material.isVideo) Icons.Filled.PlayCircle else Icons.Filled.Description,
                contentDescription = null,
                tint = EdOrange,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EdTextPrimary
                )
                Text(
                    text = "${material.typeLabel}  ·  ${if (material.isPremium) "Premium" else "Free"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EdTextSecondary
                )
                if (!material.description.isNullOrBlank()) {
                    Text(
                        text = material.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EdTextSecondary,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (material.locked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked. Subscription required",
                    tint = EdTextSecondary
                )
            }
        }
    }
}

@Composable
fun MaterialList(
    materials: List<MaterialDto>,
    emptyTitle: String,
    emptyMessage: String,
    onOpen: (MaterialDto) -> Unit
) {
    if (materials.isEmpty()) {
        EmptyState(title = emptyTitle, message = emptyMessage)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            materials.forEach { material ->
                MaterialRow(material = material, onClick = { onOpen(material) })
            }
        }
    }
}
