package com.edblueprint.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edblueprint.app.data.remote.UserDto
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.components.TabScreen
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdOutline
import com.edblueprint.app.ui.theme.EdSurface
import com.edblueprint.app.ui.theme.EdSurfaceHigh
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

@Composable
fun ProfileScreen(user: UserDto, onLogout: () -> Unit) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    TabScreen(title = "Profile", subtitle = "Your account") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EdSurface),
            border = BorderStroke(1.dp, EdOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                ProfileRow("Full name", user.fullname)
                ProfileRow("Email", user.email)
                ProfileRow(
                    "Plan",
                    if (user.subscriptionActive) "Premium" else "Free"
                )
            }
        }

        PrimaryButton(text = "Log out", onClick = { confirmLogout = true })
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = EdSurfaceHigh,
            title = { Text("Log out?", color = EdTextPrimary) },
            text = { Text("You will need to log in again to continue learning.", color = EdTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) {
                    Text("Log out", color = EdOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("Cancel", color = EdTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = EdOrange
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = EdTextPrimary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
