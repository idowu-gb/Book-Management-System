package com.example.bookmanagementsystem

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val genre: String? = null, // Optional field
    val dateAdded: Long = System.currentTimeMillis(), // Store as timestamp
    val pagesRead: Int = 0,
    val totalPages: Int = 0
) {
    fun getReadingProgress(): Int {
        return if (totalPages > 0) (pagesRead * 100 / totalPages) else 0
    }
}
