package com.example.data.repository

import com.example.data.database.ChatMessageDao
import com.example.data.database.ChatMessageEntity
import com.example.data.database.ConversationDao
import com.example.data.database.ConversationEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allConversations: Flow<List<Conversation>> =
        conversationDao.getAllConversations().map { list ->
            list.map { it.toDomain() }
        }

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getMessagesList(conversationId: String): List<ChatMessage> =
        chatMessageDao.getMessagesList(conversationId).map { it.toDomain() }
// Memory support: search relevant messages from old conversations.
suspend fun searchMessagesForMemory(
    query: String,
    limit: Int = 20
): List<ChatMessage> =
    chatMessageDao.searchMessagesForMemory(query, limit)
        .map { it.toDomain() }

// Memory support: get recent messages across all conversations.
suspend fun getRecentMessagesForMemory(
    limit: Int = 50
): List<ChatMessage> =
    chatMessageDao.getRecentMessagesForMemory(limit)
        .map { it.toDomain() }

// Memory support: get recent user messages across all conversations.
suspend fun getRecentUserMessages(
    limit: Int = 30
): List<ChatMessage> =
    chatMessageDao.getRecentUserMessages(limit)
        .map { it.toDomain() }
    suspend fun createConversation(id: String = UUID.randomUUID().toString(), title: String): String {
        val now = System.currentTimeMillis()
        conversationDao.insertOrUpdate(
            ConversationEntity(
                id = id,
                title = title,
                createdAt = now,
                updatedAt = now
            )
        )
        return id
    }

    suspend fun updateConversationTitle(id: String, newTitle: String) {
        val existing = conversationDao.getConversationById(id)
        if (existing != null) {
            conversationDao.insertOrUpdate(
                existing.copy(
                    title = newTitle,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun saveMessage(
        conversationId: String,
        text: String,
        fromUser: Boolean,
        messageId: String = UUID.randomUUID().toString(),
        timestamp: Long = System.currentTimeMillis()
    ): ChatMessage {
        val entity = ChatMessageEntity(
            id = messageId,
            conversationId = conversationId,
            text = text,
            fromUser = fromUser,
            timestamp = timestamp
        )
        chatMessageDao.insertMessage(entity)

        // Touch conversation updated time
        val existing = conversationDao.getConversationById(conversationId)
        if (existing != null) {
            conversationDao.insertOrUpdate(
                existing.copy(updatedAt = timestamp)
            )
        }
        return entity.toDomain()
    }

    suspend fun deleteConversation(id: String) {
        chatMessageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversation(id)
    }

    suspend fun clearMessagesForConversation(conversationId: String) {
        chatMessageDao.deleteMessagesForConversation(conversationId)
    }

    private fun ConversationEntity.toDomain() = Conversation(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        conversationId = conversationId,
        text = text,
        fromUser = fromUser,
        timestamp = timestamp
    )
}
