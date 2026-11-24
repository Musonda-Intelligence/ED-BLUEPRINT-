package com.edblueprint.app.ui.admin

import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.layout.padding
import com.edblueprint.app.data.remote.UserDto
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.components.SectionTitle
import com.edblueprint.app.ui.components.TabScreen
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdSurfaceHigh
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

/**
 * Administrator landing screen. Only shown when the server says the account is an admin.
 *
 * In this first version all management (uploading videos, PDFs and papers, quizzes,
 * students, subscriptions) is done in the ED Blueprint website admin dashboard, which
 * uses the same server and database. Anything published there appears in the student
 * app when students refresh or reopen it. Admin screens inside the app come later.
 */
@Composable
fun AdminHomeScreen(user: UserDto, onLogout: () -> Unit) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    TabScreen(
        title = "Admin",
        subtitle = "Signed in as ${user.fullname}"
    ) {
        SectionTitle("Managing content")
        EdCard(borderColor = EdOrange) {
            Text(
                text = "Use the website admin dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = EdTextPrimary
            )
            Text(
                text = "Upload and edit videos, documents, past papers and quizzes, publish or " +
                    "unpublish them, and manage students and subscriptions from the ED Blueprint " +
                    "website admin dashboard. Students see new material in this app as soon as " +
                    "they refresh or reopen it, with no app update needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = EdTextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        PrimaryButton(text = "Log out", onClick = { confirmLogout = true })
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = EdSurfaceHigh,
            title = { Text("Log out?", color = EdTextPrimary) },
            text = { Text("You will need to log in again.", color = EdTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) { Text("Log out", color = EdOrange) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("Cancel", color = EdTextSecondary)
                }
            }
        )
    }
}
