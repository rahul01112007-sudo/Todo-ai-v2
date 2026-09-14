package com.example.data.repository

import com.example.data.database.NoteDao
import com.example.data.database.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun saveNote(title: String, content: String, id: Long = 0): Long {
        return noteDao.insertNote(
            NoteEntity(
                id = id,
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNote(id)
    }
}
