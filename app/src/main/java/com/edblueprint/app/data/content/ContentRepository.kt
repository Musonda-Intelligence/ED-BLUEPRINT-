package com.edblueprint.app.data.content

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.edblueprint.app.data.remote.ApiService
import com.edblueprint.app.data.remote.AttemptDto
import com.edblueprint.app.data.remote.MaterialDto
import com.edblueprint.app.data.remote.QuizDetailDto
import com.edblueprint.app.data.remote.QuizSummaryDto
import com.edblueprint.app.data.remote.SubmitRequest
import com.edblueprint.app.data.remote.TopicDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Everything the student screens read from the server. */
class ContentRepository(
    private val api: ApiService,
    private val cacheDir: File
) {
    suspend fun topics(): List<TopicDto> = api.topics().topics

    suspend fun topicMaterials(topicId: Long): List<MaterialDto> =
        api.topicMaterials(topicId).materials

    suspend fun recentMaterials(): List<MaterialDto> = api.recentMaterials().materials

    suspend fun papers(): List<MaterialDto> = api.papers().materials

    suspend fun quizzes(): List<QuizSummaryDto> = api.quizzes().quizzes

    suspend fun quiz(id: Long): QuizDetailDto = api.quiz(id).quiz

    suspend fun attempts(): List<AttemptDto> = api.attempts().attempts

    suspend fun submitQuiz(id: Long, answers: Map<Long, Long>): AttemptDto {
        val body = SubmitRequest(answers.mapKeys { it.key.toString() })
        return api.submitQuiz(id, body).attempt
    }

    /**
     * Fetches a PDF from the protected endpoint into a temporary file and opens it.
     * The server refuses with 403 when a premium file is requested without a subscription.
     */
    suspend fun openPdf(materialId: Long): PdfSession = withContext(Dispatchers.IO) {
        val file = File.createTempFile("pdf_", ".tmp", cacheDir)
        try {
            api.materialFile(materialId).use { body ->
                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            try {
                PdfSession(file, descriptor, PdfRenderer(descriptor))
            } catch (e: Exception) {
                descriptor.close()
                throw e
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }
}
