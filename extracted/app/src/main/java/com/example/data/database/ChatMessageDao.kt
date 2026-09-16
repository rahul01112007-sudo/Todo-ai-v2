package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("""
        SELECT * FROM chat_messages
        WHERE conversationId = :conversationId
        ORDER BY timestamp ASC
    """)
    fun getMessagesForConversation(
        conversationId: String
    ): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_messages
        WHERE conversationId = :conversationId
        ORDER BY timestamp ASC
    """)
    suspend fun getMessagesList(
        conversationId: String
    ): List<ChatMessageEntity>

    // Search old conversations for relevant memory.
    @Query("""
        SELECT * FROM chat_messages
        WHERE text LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun searchMessagesForMemory(
        query: String,
        limit: Int = 20
    ): List<ChatMessageEntity>

    @Query("""
        SELECT * FROM chat_messages
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentMessagesForMemory(
        limit: Int = 50
    ): List<ChatMessageEntity>

    @Query("""
        SELECT * FROM chat_messages
        WHERE fromUser = 1
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentUserMessages(
        limit: Int = 30
    ): List<ChatMessageEntity>

    @Query("""
        SELECT * FROM chat_messages
        WHERE id = :messageId
        LIMIT 1
    """)
    suspend fun getMessage(
        messageId: String
    ): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(
        message: ChatMessageEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(
        messages: List<ChatMessageEntity>
    )

    @Query("""
        DELETE FROM chat_messages
        WHERE conversationId = :conversationId
    """)
    suspend fun deleteMessagesForConversation(
        conversationId: String
    )

    @Query("""
        DELETE FROM chat_messages
        WHERE id = :messageId
    """)
    suspend fun deleteMessage(
        messageId: String
    )
}
