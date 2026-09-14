package com.example.data.model

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val text: String,
    val fromUser: Boolean,
    val timestamp: Long
)
