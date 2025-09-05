package com.example.bookmanagementsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class BookViewModel(private val storage: BookStorage) : ViewModel() {
    val allBooks: Flow<List<Book>> = storage.allBooks

    var searchQuery by mutableStateOf("")

    var isSearchActive by mutableStateOf(false)
        private set

    fun insert(book: Book) = viewModelScope.launch {
        storage.insert(book)
    }

    fun update(book: Book) = viewModelScope.launch {
        storage.update(book)
    }

    fun delete(book: Book) = viewModelScope.launch {
        storage.delete(book)
    }

    fun activateSearch() {
        isSearchActive = true
    }

    fun getFilteredBooks(books: List<Book>): List<Book> {
        return if (!isSearchActive || searchQuery.isBlank()) {
            books
        } else {
            books.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}