package com.edblueprint.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Topics and materials ----------

@Serializable
data class TopicDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("material_count") val materialCount: Int = 0,
    @SerialName("quiz_count") val quizCount: Int = 0
)

@Serializable
data class TopicsResponse(val topics: List<TopicDto>)

/**
 * One video, document, note or past paper.
 * `locked` is worked out by the server for the signed-in student.
 * It only decides what the app shows; the server checks again when the file is requested.
 */
@Serializable
data class MaterialDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val type: String,
    val access: String = "premium",
    val locked: Boolean = false,
    @SerialName("topic_id") val topicId: Long? = null,
    @SerialName("published_at") val publishedAt: String? = null
) {
    val isVideo: Boolean get() = type == "video"

    val typeLabel: String
        get() = when (type) {
            "video" -> "Video"
            "document" -> "Document"
            "note" -> "Notes"
            "paper" -> "Past paper"
            "marking_scheme" -> "Marking scheme"
            else -> "Material"
        }

    val isPremium: Boolean get() = access == "premium"
}

@Serializable
data class MaterialsResponse(val materials: List<MaterialDto>)

// ---------- Quizzes ----------

@Serializable
data class QuizSummaryDto(
    val id: Long,
    val title: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    @SerialName("question_count") val questionCount: Int = 0,
    @SerialName("total_marks") val totalMarks: Int = 0
)

@Serializable
data class QuizzesResponse(val quizzes: List<QuizSummaryDto>)

@Serializable
data class OptionDto(val id: Long, val text: String)

/** Questions sent to the app never contain the correct answer. */
@Serializable
data class QuestionDto(
    val id: Long,
    val text: String,
    val marks: Int = 1,
    val options: List<OptionDto>
)

@Serializable
data class QuizDetailDto(
    val id: Long,
    val title: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    val questions: List<QuestionDto>
)

@Serializable
data class QuizDetailResponse(val quiz: QuizDetailDto)

/** Map of question id (as text) to the chosen option id. */
@Serializable
data class SubmitRequest(val answers: Map<String, Long>)

@Serializable
data class ReviewItemDto(
    val question: String,
    @SerialName("your_answer") val yourAnswer: String? = null,
    @SerialName("correct_answer") val correctAnswer: String,
    val correct: Boolean,
    val marks: Int = 0
)

/** The score is calculated by the server. The app only displays it. */
@Serializable
data class AttemptDto(
    val id: Long,
    @SerialName("quiz_id") val quizId: Long,
    @SerialName("quiz_title") val quizTitle: String = "",
    val score: Int,
    val total: Int,
    @SerialName("submitted_at") val submittedAt: String? = null,
    val review: List<ReviewItemDto> = emptyList()
) {
    val percent: Int get() = if (total > 0) (score * 100) / total else 0
}

@Serializable
data class AttemptResponse(val attempt: AttemptDto)

@Serializable
data class AttemptsResponse(val attempts: List<AttemptDto>)
