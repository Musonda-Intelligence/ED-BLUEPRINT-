package com.edblueprint.app.data.content

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * An opened PDF. Pages are drawn on demand and the temporary file is deleted
 * as soon as the student leaves the screen, so nothing is kept on the phone.
 */
class PdfSession(
    private val file: File,
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer
) {
    val pageCount: Int = renderer.pageCount

    // PdfRenderer can only have one page open at a time.
    private val lock = Mutex()

    suspend fun renderPage(index: Int, widthPx: Int): Bitmap = lock.withLock {
        withContext(Dispatchers.Default) {
            val page = renderer.openPage(index)
            try {
                val height = (widthPx.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } finally {
                page.close()
            }
        }
    }

    fun close() {
        runCatching { renderer.close() }
        runCatching { descriptor.close() }
        runCatching { file.delete() }
    }
}
