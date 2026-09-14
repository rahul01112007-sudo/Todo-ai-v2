package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiEngine
import com.example.ai.LocalAiEngine
import com.example.data.database.AppDatabase
import com.example.data.database.NoteEntity
import com.example.data.model.AiEngineState
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.ModelInfo
import com.example.data.model.ModelStatus
import com.example.data.repository.ChatRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatRepository = ChatRepository(db.conversationDao(), db.chatMessageDao())
    private val notesRepository = NotesRepository(db.noteDao())
    val settingsRepository = SettingsRepository(application)
    val aiEngine: AiEngine = LocalAiEngine(application)

    val isOnboardingCompleted = MutableStateFlow(settingsRepository.isOnboardingCompleted)

    val conversations: StateFlow<List<Conversation>> = chatRepository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessage>> = _currentConversationId
        .flatMapLatest { id ->
            if (id != null) {
                chatRepository.getMessagesForConversation(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = notesRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modelStatus: StateFlow<ModelStatus> = aiEngine.modelStatus
    val engineState: StateFlow<AiEngineState> = aiEngine.engineState
    val currentModelInfo: StateFlow<ModelInfo?> = aiEngine.currentModelInfo
    val availableModels: StateFlow<List<ModelInfo>> = aiEngine.availableModels

    val contextLength = MutableStateFlow(settingsRepository.contextLength)
    val temperature = MutableStateFlow(settingsRepository.temperature)
    val maxTokens = MutableStateFlow(settingsRepository.maxResponseTokens)

    val searchQuery = MutableStateFlow("")
    val isSearching = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<ChatMessage>> = searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else chatRepository.searchMessages(q.trim())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var currentGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            aiEngine.initialize()
            // Try restore last conversation or create one
            val lastId = settingsRepository.lastConversationId
            if (lastId != null) {
                _currentConversationId.value = lastId
            } else {
                startNewChat()
            }
        }
    }

    fun completeOnboarding() {
        settingsRepository.isOnboardingCompleted = true
        isOnboardingCompleted.value = true
    }

    fun startNewChat() {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            chatRepository.createConversation(id, "New Chat")
            _currentConversationId.value = id
            settingsRepository.lastConversationId = id

            // Insert initial greeting welcome message
            val welcomeText = "Namaste! 👋\nMain TODO hoon.\nTumhara Offline AI Assistant.\nKuch bhi pucho, main yahan hoon help karne ke liye."
            chatRepository.saveMessage(
                conversationId = id,
                text = welcomeText,
                fromUser = false
            )
        }
    }

    fun loadConversation(id: String) {
        _currentConversationId.value = id
        settingsRepository.lastConversationId = id
        isSearching.value = false
        searchQuery.value = ""
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val convId = _currentConversationId.value ?: return

        viewModelScope.launch {
            // Save user message
            chatRepository.saveMessage(
                conversationId = convId,
                text = trimmed,
                fromUser = true
            )

            // Auto-update conversation title if it's the first user message
            val existingMessages = chatRepository.getMessagesList(convId)
            val userMessages = existingMessages.filter { it.fromUser }
            if (userMessages.size == 1) {
                val title = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                chatRepository.updateConversationTitle(convId, title)
            }

            // Check if model is ready
            if (modelStatus.value != ModelStatus.READY) {
                chatRepository.saveMessage(
                    conversationId = convId,
                    text = "⚠️ Local AI model required.\n\nPlease open 'Model & Settings' from the menu to import or load a local GGUF model.",
                    fromUser = false
                )
                return@launch
            }

            // Prepare AI placeholder message
            val aiMessageId = UUID.randomUUID().toString()
            val savedAiPlaceholder = chatRepository.saveMessage(
                conversationId = convId,
                text = "Thinking...",
                fromUser = false,
                messageId = aiMessageId
            )

            currentGenerationJob?.cancel()
            currentGenerationJob = launch {
                try {
                    aiEngine.generateResponse(
                        prompt = trimmed,
                        history = existingMessages,
                        contextLength = contextLength.value,
                        temperature = temperature.value,
                        maxTokens = maxTokens.value
                    ).collect { streamedText ->
                        // Update the message in database/state
                        chatRepository.saveMessage(
                            conversationId = convId,
                            text = streamedText,
                            fromUser = false,
                            messageId = aiMessageId,
                            timestamp = savedAiPlaceholder.timestamp
                        )
                    }
                } catch (e: Exception) {
                    chatRepository.saveMessage(
                        conversationId = convId,
                        text = "Error generating response: ${e.localizedMessage ?: "Unknown error"}",
                        fromUser = false,
                        messageId = aiMessageId,
                        timestamp = savedAiPlaceholder.timestamp
                    )
                }
            }
        }
    }

    fun cancelGeneration() {
        currentGenerationJob?.cancel()
        aiEngine.cancelGeneration()
    }

    fun clearCurrentChat() {
        val id = _currentConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.clearMessagesForConversation(id)
            val welcomeText = "Namaste! 👋\nMain TODO hoon.\nTumhara Offline AI Assistant.\nKuch bhi pucho, main yahan hoon help karne ke liye."
            chatRepository.saveMessage(
                conversationId = id,
                text = welcomeText,
                fromUser = false
            )
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                startNewChat()
            }
        }
    }

    fun updateModelSettings(newContextLength: Int, newTemp: Float, newMaxTokens: Int) {
        contextLength.value = newContextLength
        temperature.value = newTemp
        maxTokens.value = newMaxTokens

        settingsRepository.contextLength = newContextLength
        settingsRepository.temperature = newTemp
        settingsRepository.maxResponseTokens = newMaxTokens
    }

    fun loadModel(modelInfo: ModelInfo) {
        viewModelScope.launch {
            aiEngine.loadModel(modelInfo)
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            aiEngine.unloadModel()
        }
    }

    fun importModel(uri: Uri, displayName: String) {
        viewModelScope.launch {
            aiEngine.importModel(uri, displayName)
        }
    }

    fun installStarterModel() {
        viewModelScope.launch {
            aiEngine.installStarterModel()
        }
    }

    fun deleteModel(modelInfo: ModelInfo) {
        viewModelScope.launch {
            aiEngine.deleteModel(modelInfo)
        }
    }

    fun saveNote(title: String, content: String, id: Long = 0) {
        viewModelScope.launch {
            notesRepository.saveNote(title, content, id)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteNote(id)
        }
    }
}
