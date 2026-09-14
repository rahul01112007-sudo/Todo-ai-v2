package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("todo_ai_prefs", Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var activeModelName: String?
        get() = prefs.getString(KEY_ACTIVE_MODEL_NAME, null)
        set(value) = prefs.edit().putString(KEY_ACTIVE_MODEL_NAME, value).apply()

    var contextLength: Int
        get() = prefs.getInt(KEY_CONTEXT_LENGTH, 2048)
        set(value) = prefs.edit().putInt(KEY_CONTEXT_LENGTH, value).apply()

    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()

    var maxResponseTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, 512)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()

    var lastConversationId: String?
        get() = prefs.getString(KEY_LAST_CONVERSATION_ID, null)
        set(value) = prefs.edit().putString(KEY_LAST_CONVERSATION_ID, value).apply()

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_ACTIVE_MODEL_NAME = "active_model_name"
        private const val KEY_CONTEXT_LENGTH = "context_length"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_LAST_CONVERSATION_ID = "last_conversation_id"
    }
}
