package com.edblueprint.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edblueprint.app.data.remote.QuizSummaryDto
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
fun QuizCard(quiz: QuizSummaryDto, onClick: () -> Unit) {
    EdCard(onClick = onClick) {
        Text(
            text = quiz.title,
            style = MaterialTheme.typography.titleMedium,
            color = EdTextPrimary
        )
        val time = if (quiz.durationMinutes > 0) "${quiz.durationMinutes} min" else "No time limit"
        Text(
            text = "${quiz.questionCount} questions  ·  ${quiz.totalMarks} marks  ·  $time",
            style = MaterialTheme.typography.bodyMedium,
            color = EdTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = "START QUIZ",
            style = MaterialTheme.typography.labelSmall,
            color = EdOrange,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun QuizzesScreen(onOpenQuiz: (id: Long, title: String) -> Unit) {
    val repository = rememberContainer().contentRepository
    val quizzes = rememberLoader("quizzes") { repository.quizzes() }
    val attempts = rememberLoader("attempts") { repository.attempts() }
    val quizState by quizzes.state.collectAsStateWithLifecycle()
    val attemptState by attempts.state.collectAsStateWithLifecycle()

    // Coming back from a finished quiz shows the new score straight away.
    LifecycleResumeEffect(Unit) {
        attempts.reload()
        onPauseOrDispose { }
    }

    TabScreen(title = "Quizzes", subtitle = "Test what you have learned") {
        SectionTitle("Available quizzes")
        LoadContent(state = quizState, onRetry = quizzes::reload) { list ->
            if (list.isEmpty()) {
                EmptyState(
                    title = "No quizzes yet",
                    message = "Published quizzes will be listed here."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    list.forEach { quiz ->
                        QuizCard(quiz = quiz, onClick = { onOpenQuiz(quiz.id, quiz.title) })
                    }
                }
            }
        }

        SectionTitle("Previous attempts")
        LoadContent(state = attemptState, onRetry = attempts::reload) { list ->
            if (list.isEmpty()) {
                EmptyState(
                    title = "No attempts yet",
                    message = "Your scores appear here after you finish a quiz."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    list.forEach { attempt ->
                        EdCard {
                            Text(
                                text = attempt.quizTitle.ifBlank { "Quiz ${attempt.quizId}" },
                                style = MaterialTheme.typography.titleMedium,
                                color = EdTextPrimary
                            )
                            Text(
                                text = "${attempt.score} / ${attempt.total}  (${attempt.percent}%)" +
                                    (attempt.submittedAt?.take(10)?.let { "  ·  $it" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium,
                                color = EdTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
