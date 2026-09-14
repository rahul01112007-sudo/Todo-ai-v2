package com.example.data.model

data class ModelInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isInstalled: Boolean,
    val isLoaded: Boolean,
    val contextLength: Int = 2048,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512
) {
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 MB"
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0)
            } else {
                String.format(java.util.Locale.US, "%.1f MB", mb)
            }
        }
}
