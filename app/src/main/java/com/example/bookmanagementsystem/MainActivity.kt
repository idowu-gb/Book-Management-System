package com.example.bookmanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bookmanagementsystem.ui.theme.BookManagementSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BookDatabase.getDatabase(applicationContext)
        val bookDao = db.bookDao()

        enableEdgeToEdge()
        setContent {
            BookManagementSystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Host the BookApp composable
                        BookApp(bookDao = bookDao)
                    }
                }
            }
        }
    }
}


@Composable
fun BookApp(bookDao: BookDao) {
    val navController = rememberNavController()
    val storage = BookStorage(bookDao)
    val viewModel: BookViewModel = viewModel(
        factory = ViewModelFactory(storage)
    )

    NavHost(navController = navController, startDestination = "bookList") {
        composable("bookList") {
            BookListScreen(
                viewModel = viewModel,
                onBookClick = { book ->
                    navController.navigate("bookDetail/${book.id}")
                },
                onAddBook = {
                    navController.navigate("addBook")
                }
            )
        }
        composable("bookDetail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")?.toInt() ?: 0
            val books by viewModel.allBooks.collectAsState(initial = emptyList())
            val book = books.find { it.id == bookId }
            if (book != null) {
                BookDetailScreen(book = book, onSave = { updatedBook ->
                    viewModel.update(updatedBook)
                    navController.popBackStack()
                })
            }
        }
        composable("addBook") {
            AddBookScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}


@Composable
fun BookListScreen(viewModel: BookViewModel, onBookClick: (Book) -> Unit, onAddBook: () -> Unit) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val filteredBooks = viewModel.getFilteredBooks(books)

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = viewModel.searchQuery,
            onValueChange = { query ->
                viewModel.searchQuery = query
            },
            label = { Text("Search by title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.activateSearch()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddBook,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Book")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredBooks) { book ->
                BookItem(
                    book = book,
                    onBookClick = onBookClick,
                    onDelete = {
                        viewModel.delete(book)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@Composable
fun BookItem(book: Book, onBookClick: (Book) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            Text(text = "By ${book.author}", style = MaterialTheme.typography.bodyMedium)
            if (!book.genre.isNullOrEmpty()) {
                Text(text = "Genre: ${book.genre}", style = MaterialTheme.typography.bodySmall)
            }
            // Format the date using SimpleDateFormat
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(book.dateAdded))
            Text(text = "Date Added: $formattedDate", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Progress: ${book.getReadingProgress()}% (${book.pagesRead}/${book.totalPages} pages)",
                style = MaterialTheme.typography.bodySmall
            )
            // Progress bar
            LinearProgressIndicator(
                progress = book.getReadingProgress() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(top = 8.dp)
            )
            // Delete button
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}


@Composable
fun AddBookScreen(viewModel: BookViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var pagesRead by remember { mutableStateOf("") }
    var totalPages by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = pagesRead,
            onValueChange = { pagesRead = it },
            label = { Text("Pages Read") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = totalPages,
            onValueChange = { totalPages = it },
            label = { Text("Total Pages") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val book = Book(
                    title = title,
                    author = author,
                    genre = genre,
                    pagesRead = pagesRead.toIntOrNull() ?: 0,
                    totalPages = totalPages.toIntOrNull() ?: 0
                )
                viewModel.insert(book)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Book")
        }
    }
}

@Composable
fun BookDetailScreen(book: Book, onSave: (Book) -> Unit) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var genre by remember { mutableStateOf(book.genre ?: "") }
    var pagesRead by remember { mutableStateOf(book.pagesRead.toString()) }
    var totalPages by remember { mutableStateOf(book.totalPages.toString()) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = pagesRead,
            onValueChange = { pagesRead = it },
            label = { Text("Pages Read") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = totalPages,
            onValueChange = { totalPages = it },
            label = { Text("Total Pages") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onSave(
                    book.copy(
                        title = title,
                        author = author,
                        genre = genre,
                        pagesRead = pagesRead.toIntOrNull() ?: 0,
                        totalPages = totalPages.toIntOrNull() ?: 0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}