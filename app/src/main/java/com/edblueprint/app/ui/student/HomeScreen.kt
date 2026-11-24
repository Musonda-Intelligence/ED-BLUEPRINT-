package com.edblueprint.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edblueprint.app.data.remote.MaterialDto
import com.edblueprint.app.data.remote.UserDto
import com.edblueprint.app.ui.app.rememberContainer
import com.edblueprint.app.ui.app.rememberLoader
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.EmptyState
import com.edblueprint.app.ui.components.LoadContent
import com.edblueprint.app.ui.components.SectionTitle
import com.edblueprint.app.ui.components.TabScreen
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

@Composable
fun HomeScreen(
    user: UserDto,
    onOpenMaterial: (MaterialDto) -> Unit,
    onOpenQuiz: (id: Long, title: String) -> Unit
) {
    val repository = rememberContainer().contentRepository
    val recent = rememberLoader("recent") { repository.recentMaterials() }
    val quizzes = rememberLoader("home-quizzes") { repository.quizzes() }
    val attempts = rememberLoader("home-attempts") { repository.attempts() }
    val recentState by recent.state.collectAsStateWithLifecycle()
    val quizState by quizzes.state.collectAsStateWithLifecycle()
    val attemptState by attempts.state.collectAsStateWithLifecycle()

    TabScreen(
        title = "Hello, ${user.firstName}",
        subtitle = "Master Engineering Drawing. Build Your Future."
    ) {
        SubscriptionCard(user)

        SectionTitle("Recently added")
        LoadContent(state = recentState, onRetry = recent::reload) { list ->
            MaterialList(
                materials = list.take(5),
                emptyTitle = "Nothing published yet",
                emptyMessage = "New videos, documents and notes appear here as soon as they are published.",
                onOpen = onOpenMaterial
            )
        }

        SectionTitle("Available quizzes")
        LoadContent(state = quizState, onRetry = quizzes::reload) { list ->
            if (list.isEmpty()) {
                EmptyState(
                    title = "No quizzes yet",
                    message = "Published quizzes will be listed here."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    list.take(3).forEach { quiz ->
                        QuizCard(quiz = quiz, onClick = { onOpenQuiz(quiz.id, quiz.title) })
                    }
                }
            }
        }

        SectionTitle("Your progress")
        LoadContent(state = attemptState, onRetry = attempts::reload) { list ->
            if (list.isEmpty()) {
                EmptyState(
                    title = "No progress yet",
                    message = "Finish a quiz to see how you are doing."
                )
            } else {
                val average = list.map { it.percent }.average().toInt()
                val best = list.maxOf { it.percent }
                EdCard {
                    Text(
                        text = "${list.size} quiz attempts",
                        style = MaterialTheme.typography.titleMedium,
                        color = EdTextPrimary
                    )
                    Text(
                        text = "Average score $average%  ·  Best score $best%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EdTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(user: UserDto) {
    val active = user.subscriptionActive
    EdCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (active) EdOrange else com.edblueprint.app.ui.theme.EdOutline
    ) {
        Text(
            text = "SUBSCRIPTION",
            style = MaterialTheme.typography.labelSmall,
            color = EdOrange
        )
        Text(
            text = if (active) "Premium active" else "Free plan",
            style = MaterialTheme.typography.titleLarge,
            color = EdTextPrimary,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = if (active) {
                val until = user.subscriptionExpiresAt?.take(10)
                if (until != null) "Videos and documents are unlocked until $until."
                else "Videos and documents are unlocked."
            } else {
                "Past papers are open to you. Videos and course documents need a subscription."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = EdTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
