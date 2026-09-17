package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Local document/image text reader for TODO AI.
 *
 * Supports:
 * - Images
 * - PDF files
 * - Latin/English OCR
 * - Devanagari/Hindi OCR
 *
 * OCR is performed on-device.
 */
class DocumentReader(
    private val context: Context
) {

    private val latinRecognizer: TextRecognizer =
        TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

    private val devanagariRecognizer: TextRecognizer =
        TextRecognition.getClient(
            DevanagariTextRecognizerOptions.Builder().build()
        )

    /**
     * Main entry point.
     */
    suspend fun extractText(
        uri: Uri,
        mimeType: String
    ): String {

        return when {
            mimeType.startsWith("image/") -> {
                extractTextFromImage(uri)
            }

            mimeType == "application/pdf" ||
                    mimeType.contains("pdf") -> {
                extractTextFromPdf(uri)
            }

            mimeType.startsWith("text/") -> {
                readPlainText(uri)
            }

            else -> {
                // Try image processing as a fallback.
                extractTextFromImage(uri)
            }
        }
    }

    /**
     * Extract text from an image.
     */
    private suspend fun extractTextFromImage(
        uri: Uri
    ): String {

        val image = InputImage.fromFilePath(
            context,
            uri
        )

        return runOcr(image)
    }

    /**
     * Run both Latin and Devanagari OCR.
     *
     * This helps with mixed Hindi + English documents.
     */
    private suspend fun runOcr(
        image: InputImage
    ): String {

        val latinText = processWithRecognizer(
            latinRecognizer,
            image
        ).trim()

        val devanagariText = processWithRecognizer(
            devanagariRecognizer,
            image
        ).trim()

        return combineOcrResults(
            latinText,
            devanagariText
        )
    }

    /**
     * Process an image with an ML Kit recognizer.
     */
    private suspend fun processWithRecognizer(
        recognizer: TextRecognizer,
        image: InputImage
    ): String =
        suspendCancellableCoroutine { continuation ->

            recognizer
                .process(image)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) {
                        continuation.resume(result.text)
                    }
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                }
        }

    /**
     * Combine OCR results.
     *
     * If only one recognizer finds text, use that result.
     * If both find text, keep both so Hindi + English content
     * is available to the AI.
     */
    private fun combineOcrResults(
        latinText: String,
        devanagariText: String
    ): String {

        if (latinText.isBlank() && devanagariText.isBlank()) {
            return ""
        }

        if (latinText.isBlank()) {
            return devanagariText
        }

        if (devanagariText.isBlank()) {
            return latinText
        }

        // If Devanagari text is actually present, preserve
        // both language results for mixed Hindi/English documents.
        val hasDevanagari = devanagariText.any {
            it.code in 0x0900..0x097F
        }

        return if (hasDevanagari) {
            buildString {
                append(latinText)
                append("\n\n")
                append(devanagariText)
            }
        } else {
            latinText
        }
    }

    /**
     * Extract text from every PDF page by rendering the page
     * to a bitmap and running local OCR.
     */
    private suspend fun extractTextFromPdf(
        uri: Uri
    ): String {

        val fileDescriptor: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(
                uri,
                "r"
            ) ?: throw IOException(
                "Unable to open PDF file."
            )

        val renderer = PdfRenderer(fileDescriptor)

        return try {

            val pages = mutableListOf<String>()

            for (pageIndex in 0 until renderer.pageCount) {

                val page = renderer.openPage(pageIndex)

                try {

                    // Render at a reasonable resolution to
                    // avoid excessive memory usage.
                    val scale = 2

                    val width = page.width * scale
                    val height = page.height * scale

                    val bitmap = Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    )

                    bitmap.eraseColor(
                        android.graphics.Color.WHITE
                    )

                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    val pageImage = InputImage.fromBitmap(
                        bitmap,
                        0
                    )

                    val pageText = runOcr(
                        pageImage
                    ).trim()

                    if (pageText.isNotBlank()) {
                        pages.add(
                            "----- Page ${pageIndex + 1} -----\n$pageText"
                        )
                    }

                    bitmap.recycle()

                } finally {
                    page.close()
                }
            }

            pages.joinToString("\n\n")

        } finally {
            renderer.close()
            fileDescriptor.close()
        }
    }

    /**
     * Read normal text files directly.
     */
    private fun readPlainText(
        uri: Uri
    ): String {

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: throw IOException(
                    "Unable to open text file."
                )

        return inputStream.use {
            it.bufferedReader().readText()
        }
    }

    /**
     * Release ML Kit resources when the reader is no longer needed.
     */
    fun close() {
        latinRecognizer.close()
        devanagariRecognizer.close()
    }
}
