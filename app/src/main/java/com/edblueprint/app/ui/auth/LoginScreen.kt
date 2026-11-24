package com.edblueprint.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edblueprint.app.ui.components.EdTextField
import com.edblueprint.app.ui.components.PasswordField
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdTextSecondary

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onCreateAccount: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthLayout(
        title = "Welcome back",
        subtitle = "Log in to continue learning."
    ) {
        EdTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            enabled = !state.loading
        )
        PasswordField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = "Password",
            imeAction = ImeAction.Done,
            enabled = !state.loading,
            onDone = { viewModel.login(email, password) }
        )

        ErrorMessage(state.error)

        PrimaryButton(
            text = "Log in",
            onClick = { viewModel.login(email, password) },
            loading = state.loading
        )

        Text(
            text = "New here? Create an account",
            style = MaterialTheme.typography.labelLarge,
            color = EdOrange,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(enabled = !state.loading, role = Role.Button, onClick = onCreateAccount)
                .padding(12.dp)
        )

        Text(
            text = "Master Engineering Drawing. Build Your Future.",
            style = MaterialTheme.typography.bodyMedium,
            color = EdTextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
