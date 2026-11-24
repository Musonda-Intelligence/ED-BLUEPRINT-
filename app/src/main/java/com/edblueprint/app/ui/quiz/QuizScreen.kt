package com.edblueprint.app.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edblueprint.app.data.remote.AttemptDto
import com.edblueprint.app.ui.app.rememberContainer
import com.edblueprint.app.ui.components.DetailScreen
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.PrimaryButton
import com.edblueprint.app.ui.components.SecondaryButton
import com.edblueprint.app.ui.components.SectionTitle
import com.edblueprint.app.ui.theme.EdError
import com.edblueprint.app.ui.theme.EdMaroon
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdOutline
import com.edblueprint.app.ui.theme.EdSuccess
import com.edblueprint.app.ui.theme.EdSurface
import com.edblueprint.app.ui.theme.EdSurfaceHigh
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

@Composable
fun QuizScreen(quizId: Long, title: String, onExit: () -> Unit) {
    val repository = rememberContainer().contentRepository
    val viewModel: QuizViewModel = viewModel(
        key = "quiz-$quizId",
        factory = viewModelFactory { initializer { QuizViewModel(repository, quizId) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScreen(title = title, onBack = onExit) {
        when (val current = state) {
            QuizUi.Loading -> Text("Loading quiz...", color = EdTextSecondary)
            is QuizUi.Failed -> {
                EdCard {
                    Text(current.message, color = EdError, style = MaterialTheme.typography.bodyMedium)
                }
                SecondaryButton(text = "Try again", onClick = viewModel::load)
            }
            is QuizUi.Taking -> TakingQuiz(current, viewModel)
            is QuizUi.Done -> QuizResult(current.attempt, onExit)
        }
    }
}

@Composable
private fun TakingQuiz(state: QuizUi.Taking, viewModel: QuizViewModel) {
    var confirmSubmit by rememberSaveable { mutableStateOf(false) }

    val questions = state.quiz.questions
    val question = questions[state.index]
    val isLast = state.index == questions.lastIndex

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Question ${state.index + 1} of ${questions.size}",
            style = MaterialTheme.typography.labelSmall,
            color = EdOrange
        )
        state.secondsLeft?.let { seconds ->
            Text(
                text = "%d:%02d left".format(seconds / 60, seconds % 60),
                style = MaterialTheme.typography.labelSmall,
                color = if (seconds <= 60) EdError else EdTextSecondary
            )
        }
    }

    EdCard {
        Text(
            text = question.text,
            style = MaterialTheme.typography.titleMedium,
            color = EdTextPrimary
        )
        Text(
            text = if (question.marks == 1) "1 mark" else "${question.marks} marks",
            style = MaterialTheme.typography.bodyMedium,
            color = EdTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    question.options.forEach { option ->
        val selected = state.answers[question.id] == option.id
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.RadioButton, enabled = !state.submitting) {
                    viewModel.choose(question.id, option.id)
                },
            shape = RoundedCornerShape(12.dp),
            color = if (selected) EdMaroon else EdSurface,
            border = BorderStroke(1.dp, if (selected) EdOrange else EdOutline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.padding(8.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = EdOrange,
                        unselectedColor = EdTextSecondary
                    )
                )
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = EdTextPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }

    state.error?.let {
        Text(it, color = EdError, style = MaterialTheme.typography.bodyMedium)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(
            text = "Previous",
            onClick = { viewModel.goTo(state.index - 1) },
            enabled = state.index > 0 && !state.submitting,
            modifier = Modifier.weight(1f)
        )
        if (isLast) {
            PrimaryButton(
                text = "Submit",
                onClick = { confirmSubmit = true },
                loading = state.submitting,
                modifier = Modifier.weight(1f)
            )
        } else {
            SecondaryButton(
                text = "Next",
                onClick = { viewModel.goTo(state.index + 1) },
                enabled = !state.submitting,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (confirmSubmit) {
        AlertDialog(
            onDismissRequest = { confirmSubmit = false },
            containerColor = EdSurfaceHigh,
            title = { Text("Submit quiz?", color = EdTextPrimary) },
            text = {
                Text(
                    text = if (state.unanswered > 0) {
                        "You have ${state.unanswered} unanswered question(s). You cannot change your answers after submitting."
                    } else {
                        "You cannot change your answers after submitting."
                    },
                    color = EdTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmSubmit = false
                    viewModel.submit()
                }) { Text("Submit", color = EdOrange) }
            },
            dismissButton = {
                TextButton(onClick = { confirmSubmit = false }) {
                    Text("Keep working", color = EdTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun QuizResult(attempt: AttemptDto, onExit: () -> Unit) {
    EdCard(borderColor = EdOrange) {
        Text(
            text = "YOUR SCORE",
            style = MaterialTheme.typography.labelSmall,
            color = EdOrange
        )
        Text(
            text = "${attempt.score} / ${attempt.total}",
            style = MaterialTheme.typography.headlineMedium,
            color = EdTextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "${attempt.percent}%",
            style = MaterialTheme.typography.titleMedium,
            color = EdTextSecondary
        )
    }

    if (attempt.review.isNotEmpty()) {
        SectionTitle("Review")
        attempt.review.forEachIndexed { number, item ->
            EdCard {
                Text(
                    text = "${number + 1}. ${item.question}",
                    style = MaterialTheme.typography.titleMedium,
                    color = EdTextPrimary
                )
                Text(
                    text = if (item.correct) "Correct" else "Incorrect",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.correct) EdSuccess else EdError,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "Your answer: ${item.yourAnswer ?: "Not answered"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EdTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (!item.correct) {
                    Text(
                        text = "Correct answer: ${item.correctAnswer}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EdTextPrimary
                    )
                }
            }
        }
    }

    PrimaryButton(text = "Done", onClick = onExit)
}
