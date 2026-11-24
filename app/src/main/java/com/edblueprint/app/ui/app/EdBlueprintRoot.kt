package com.edblueprint.app.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edblueprint.app.EdBlueprintApp
import com.edblueprint.app.data.auth.SessionState
import com.edblueprint.app.ui.admin.AdminHomeScreen
import com.edblueprint.app.ui.auth.AuthNavHost
import com.edblueprint.app.ui.components.BrandBackground
import com.edblueprint.app.ui.components.EdLogo
import com.edblueprint.app.ui.student.StudentScaffold
import com.edblueprint.app.ui.theme.EdOrange

/**
 * Chooses what the person sees: a loading screen while the saved login is checked,
 * the login/register flow, the student app, or the administrator dashboard.
 */
@Composable
fun EdBlueprintRoot() {
    val app = LocalContext.current.applicationContext as EdBlueprintApp
    val factory = remember { edViewModelFactory(app.container) }
    val appViewModel: AppViewModel = viewModel(factory = factory)
    val session by appViewModel.session.collectAsStateWithLifecycle()

    when (val state = session) {
        SessionState.Loading -> SplashScreen()
        SessionState.SignedOut -> AuthNavHost(viewModelFactory = factory)
        is SessionState.SignedIn -> {
            if (state.user.isAdmin) {
                AdminHomeScreen(user = state.user, onLogout = appViewModel::logout)
            } else {
                StudentScaffold(user = state.user, onLogout = appViewModel::logout)
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    BrandBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            EdLogo(modifier = Modifier.fillMaxWidth())
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = EdOrange,
                strokeWidth = 3.dp
            )
        }
    }
}
