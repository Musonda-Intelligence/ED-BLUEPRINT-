package com.edblueprint.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edblueprint.app.data.content.ContentRepository
import com.edblueprint.app.data.remote.AttemptDto
import com.edblueprint.app.data.remote.QuizDetailDto
import com.edblueprint.app.data.remote.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface QuizUi {
    data object Loading : QuizUi

    data class Failed(val message: String) : QuizUi

    /** [secondsLeft] is null when the quiz has no time limit. */
    data class Taking(
        val quiz: QuizDetailDto,
        val answers: Map<Long, Long> = emptyMap(),
        val index: Int = 0,
        val secondsLeft: Int? = null,
        val submitting: Boolean = false,
        val error: String? = null
    ) : QuizUi {
        val unanswered: Int get() = quiz.questions.count { it.id !in answers }
    }

    data class Done(val attempt: AttemptDto) : QuizUi
}

/**
 * Runs one quiz attempt. The app only collects the chosen answers;
 * the server marks them and returns the score, so a score cannot be changed on the phone.
 */
class QuizViewModel(
    private val repository: ContentRepository,
    private val quizId: Long
) : ViewModel() {

    private val _state = MutableStateFlow<QuizUi>(QuizUi.Loading)
    val state: StateFlow<QuizUi> = _state.asStateFlow()

    private var timer: Job? = null

    init {
        load()
    }

    fun load() {
        _state.value = QuizUi.Loading
        viewModelScope.launch {
            try {
                val quiz = repository.quiz(quizId)
                if (quiz.questions.isEmpty()) {
                    _state.value = QuizUi.Failed("This quiz has no questions yet.")
                    return@launch
                }
                val seconds = if (quiz.durationMinutes > 0) quiz.durationMinutes * 60 else null
                _state.value = QuizUi.Taking(quiz = quiz, secondsLeft = seconds)
                if (seconds != null) startTimer()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = QuizUi.Failed(e.userMessage())
            }
        }
    }

    fun choose(questionId: Long, optionId: Long) = updateTaking {
        it.copy(answers = it.answers + (questionId to optionId), error = null)
    }

    fun goTo(index: Int) = updateTaking {
        it.copy(index = index.coerceIn(0, it.quiz.questions.lastIndex))
    }

    fun submit() {
        val current = _state.value as? QuizUi.Taking ?: return
        if (current.submitting) return
        updateTaking { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            try {
                val attempt = repository.submitQuiz(quizId, current.answers)
                timer?.cancel()
                _state.value = QuizUi.Done(attempt)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateTaking { it.copy(submitting = false, error = e.userMessage()) }
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value as? QuizUi.Taking ?: return@launch
                val left = (current.secondsLeft ?: return@launch) - 1
                if (left <= 0) {
                    updateTaking { it.copy(secondsLeft = 0) }
                    submit()
                    return@launch
                }
                updateTaking { it.copy(secondsLeft = left) }
            }
        }
    }

    private fun updateTaking(change: (QuizUi.Taking) -> QuizUi.Taking) {
        _state.update { if (it is QuizUi.Taking) change(it) else it }
    }

    override fun onCleared() {
        timer?.cancel()
    }
}
