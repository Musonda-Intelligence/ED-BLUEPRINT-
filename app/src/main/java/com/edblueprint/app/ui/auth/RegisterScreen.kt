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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edblueprint.app.ui.components.EdTextField
import com.edblueprint.app.ui.components.PasswordField
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.theme.EdOrange

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var fullname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    AuthLayout(
        title = "Create your account",
        subtitle = "Free past papers are available as soon as you register."
    ) {
        EdTextField(
            value = fullname,
            onValueChange = {
                fullname = it
                viewModel.clearError()
            },
            label = "Full name",
            capitalization = KeyboardCapitalization.Words,
            enabled = !state.loading
        )
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
            label = "Password (at least 8 characters)",
            enabled = !state.loading
        )
        PasswordField(
            value = confirm,
            onValueChange = {
                confirm = it
                viewModel.clearError()
            },
            label = "Confirm password",
            imeAction = ImeAction.Done,
            enabled = !state.loading,
            onDone = { viewModel.register(fullname, email, password, confirm) }
        )

        ErrorMessage(state.error)

        PrimaryButton(
            text = "Create account",
            onClick = { viewModel.register(fullname, email, password, confirm) },
            loading = state.loading
        )

        Text(
            text = "Already registered? Log in",
            style = MaterialTheme.typography.labelLarge,
            color = EdOrange,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(enabled = !state.loading, role = Role.Button, onClick = onBackToLogin)
                .padding(12.dp)
        )
    }
}
