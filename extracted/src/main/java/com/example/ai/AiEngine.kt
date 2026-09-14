package com.example.ai

import android.net.Uri
import com.example.data.model.AiEngineState
import com.example.data.model.ChatMessage
import com.example.data.model.ModelInfo
import com.example.data.model.ModelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AiEngine {
    val modelStatus: StateFlow<ModelStatus>
    val engineState: StateFlow<AiEngineState>
    val currentModelInfo: StateFlow<ModelInfo?>
    val availableModels: StateFlow<List<ModelInfo>>

    suspend fun initialize()
    suspend fun refreshModelsList()
    suspend fun loadModel(modelInfo: ModelInfo): Result<Unit>
    suspend fun unloadModel()
    suspend fun importModel(uri: Uri, displayName: String): Result<ModelInfo>
    suspend fun installStarterModel(): Result<ModelInfo>
    suspend fun deleteModel(modelInfo: ModelInfo): Result<Unit>

    fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        contextLength: Int,
        temperature: Float,
        maxTokens: Int
    ): Flow<String>

    fun cancelGeneration()
}
