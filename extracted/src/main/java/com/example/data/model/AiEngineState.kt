package com.example.data.model

enum class ModelStatus {
    NOT_INSTALLED,
    NOT_LOADED,
    LOADING,
    READY,
    ERROR
}

sealed interface AiEngineState {
    data object Idle : AiEngineState
    data object LoadingModel : AiEngineState
    data class Ready(val modelName: String) : AiEngineState
    data class Generating(val partialText: String = "") : AiEngineState
    data class Error(val message: String) : AiEngineState
    data object ModelRequired : AiEngineState
}
