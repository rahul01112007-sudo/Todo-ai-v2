package com.example.ai

import android.content.Context
import android.net.Uri
import com.example.data.model.AiEngineState
import com.example.data.model.ChatMessage
import com.example.data.model.ModelInfo
import com.example.data.model.ModelStatus
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LocalAiEngine(
    private val context: Context
) : AiEngine {

    private val _modelStatus =
        MutableStateFlow(ModelStatus.NOT_INSTALLED)

    override val modelStatus: StateFlow<ModelStatus> =
        _modelStatus.asStateFlow()

    private val _engineState =
        MutableStateFlow<AiEngineState>(AiEngineState.ModelRequired)

    override val engineState: StateFlow<AiEngineState> =
        _engineState.asStateFlow()

    private val _currentModelInfo =
        MutableStateFlow<ModelInfo?>(null)

    override val currentModelInfo: StateFlow<ModelInfo?> =
        _currentModelInfo.asStateFlow()

    private val _availableModels =
        MutableStateFlow<List<ModelInfo>>(emptyList())

    override val availableModels: StateFlow<List<ModelInfo>> =
        _availableModels.asStateFlow()

    private val modelsDirectory: File by lazy {
        File(context.filesDir, "models").apply {
            if (!exists()) mkdirs()
        }
    }

    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null

    override suspend fun initialize(): Unit =
        withContext(Dispatchers.IO) {

            refreshModelsList()

            val model = _availableModels.value
                .firstOrNull { it.name.endsWith(".task", true) }

            if (model != null) {
                loadModel(model)
            } else {
                _modelStatus.value = ModelStatus.NOT_INSTALLED
                _engineState.value = AiEngineState.ModelRequired
            }
        }

    override suspend fun refreshModelsList(): Unit =
        withContext(Dispatchers.IO) {

            val files = modelsDirectory.listFiles { file ->
                file.isFile &&
                    file.name.endsWith(".task", true)
            }?.toList() ?: emptyList()

            val list = files.map { file ->

                val loaded =
                    _currentModelInfo.value?.name == file.name &&
                        llmInference != null

                ModelInfo(
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    isInstalled = true,
                    isLoaded = loaded
                )
            }

            _availableModels.value = list

            if (list.isEmpty()) {
                _currentModelInfo.value = null
                _modelStatus.value = ModelStatus.NOT_INSTALLED
                _engineState.value = AiEngineState.ModelRequired
            }
        }

    override suspend fun loadModel(
        modelInfo: ModelInfo
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {

            val file = File(modelInfo.path)

            if (!file.exists()) {
                return@withContext Result.failure(
                    IllegalStateException("Model file not found")
                )
            }

            _modelStatus.value = ModelStatus.LOADING
            _engineState.value = AiEngineState.LoadingModel

            // Close previous model/session.
            session?.close()
            session = null

            llmInference?.close()
            llmInference = null

            val options =
                LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(file.absolutePath)
                    .setMaxTokens(1024)
                    .setMaxTopK(64)
                    .build()

            val inference =
                LlmInference.createFromOptions(
                    context,
                    options
                )

            val sessionOptions =
                LlmInferenceSession.LlmInferenceSessionOptions
                    .builder()
                    .setTemperature(0.7f)
                    .setTopK(40)
                    .setTopP(0.95f)
                    .build()

            val newSession =
                LlmInferenceSession.createFromOptions(
                    inference,
                    sessionOptions
                )

            llmInference = inference
            session = newSession

            val loadedInfo =
                modelInfo.copy(
                    isInstalled = true,
                    isLoaded = true
                )

            _currentModelInfo.value = loadedInfo

            _availableModels.value =
                _availableModels.value.map {
                    if (it.name == modelInfo.name) {
                        loadedInfo
                    } else {
                        it.copy(isLoaded = false)
                    }
                }

            _modelStatus.value = ModelStatus.READY
            _engineState.value =
                AiEngineState.Ready(modelInfo.name)

            Result.success(Unit)

        } catch (e: Exception) {

            _modelStatus.value = ModelStatus.ERROR

            _engineState.value =
                AiEngineState.Error(
                    "Model load failed: ${e.message}"
                )

            Result.failure(e)
        }
    }

    override suspend fun unloadModel(): Unit =
        withContext(Dispatchers.IO) {

            session?.close()
            session = null

            llmInference?.close()
            llmInference = null

            _currentModelInfo.value =
                _currentModelInfo.value?.copy(
                    isLoaded = false
                )

            _availableModels.value =
                _availableModels.value.map {
                    it.copy(isLoaded = false)
                }

            _modelStatus.value =
                ModelStatus.NOT_LOADED

            _engineState.value =
                AiEngineState.Idle
        }

    override suspend fun importModel(
        uri: Uri,
        displayName: String
    ): Result<ModelInfo> =
        withContext(Dispatchers.IO) {

            try {

                if (!displayName.endsWith(".task", true)) {
                    return@withContext Result.failure(
                        IllegalArgumentException(
                            "Please import a MediaPipe .task model."
                        )
                    )
                }

                val targetFile =
                    File(modelsDirectory, displayName)

                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->

                        FileOutputStream(targetFile)
                            .use { output ->

                                input.copyTo(output)
                            }
                    }
                    ?: return@withContext Result.failure(
                        IllegalStateException(
                            "Cannot open selected model."
                        )
                    )

                refreshModelsList()

                val imported =
                    _availableModels.value
                        .firstOrNull {
                            it.name == displayName
                        }
                        ?: ModelInfo(
                            name = displayName,
                            path = targetFile.absolutePath,
                            sizeBytes = targetFile.length(),
                            isInstalled = true,
                            isLoaded = false
                        )

                val loadResult =
                    loadModel(imported)

                if (loadResult.isFailure) {
                    return@withContext Result.failure(
                        loadResult.exceptionOrNull()
                            ?: IllegalStateException(
                                "Failed to load imported model"
                            )
                    )
                }

                Result.success(
                    _currentModelInfo.value ?: imported
                )

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    override suspend fun installStarterModel():
        Result<ModelInfo> =
        withContext(Dispatchers.IO) {

            // IMPORTANT:
            // Do NOT generate a fake GGUF file.
            //
            // A real .task model must be downloaded/imported.
            //
            // This method is intentionally disabled until
            // a real model asset is provided.

            Result.failure(
                IllegalStateException(
                    "Starter model is not bundled. " +
                        "Import a real Gemma .task model."
                )
            )
        }

    override suspend fun deleteModel(
        modelInfo: ModelInfo
    ): Result<Unit> =
        withContext(Dispatchers.IO) {

            try {

                if (_currentModelInfo.value?.name ==
                    modelInfo.name
                ) {
                    unloadModel()
                }

                val file = File(modelInfo.path)

                if (file.exists()) {
                    file.delete()
                }

                refreshModelsList()

                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(e)
            }
        }

    override fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        contextLength: Int,
        temperature: Float,
        maxTokens: Int
    ): Flow<String> = flow {

        val activeSession = session

        if (_modelStatus.value != ModelStatus.READY ||
            activeSession == null
        ) {

            _engineState.value =
                AiEngineState.ModelRequired

            emit(
                "Local AI model is not ready. " +
                    "Please load a valid Gemma .task model."
            )

            return@flow
        }

        try {

            _engineState.value =
                AiEngineState.Generating()

            /*
             * Add conversation history.
             *
             * We keep this simple first.
             * Once the basic inference works,
             * we can improve long-term conversation memory.
             */

            for (message in history.takeLast(8)) {

                val text =
                    message.toString()

                if (text.isNotBlank()) {
                    activeSession.addQueryChunk(text)
                }
            }

            activeSession.addQueryChunk(prompt)

            val result =
    withContext(Dispatchers.Default) {
        activeSession.generateResponseAsync().get()
    }

emit(result)

            val current =
                _currentModelInfo.value?.name
                    ?: "Local Model"

            _engineState.value =
                AiEngineState.Ready(current)

        } catch (e: Exception) {

            _engineState.value =
                AiEngineState.Error(
                    "Generation failed: ${e.message}"
                )

            emit(
                "⚠️ Local AI error: ${e.message}"
            )
        }
    }

    override fun cancelGeneration() {

        try {
            session?.cancelGenerateResponseAsync()
        } catch (_: Exception) {
        }

        val current =
            _currentModelInfo.value?.name
                ?: "Local Model"

        _engineState.value =
            AiEngineState.Ready(current)
    }
}
