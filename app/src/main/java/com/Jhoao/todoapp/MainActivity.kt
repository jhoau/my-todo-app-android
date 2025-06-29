package com.Jhoao.todoapp

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.Jhoao.todoapp.ui.theme.TodoAppTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                TodoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    // App state variables
    var taskText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var todoList by remember { mutableStateOf(listOf<TodoItem>()) }
    var nextId by remember { mutableStateOf(1) }

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var todoToDelete by remember { mutableStateOf<TodoItem?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "📝 My Todo List",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        // Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Task input field
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("What do you need to do?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date picker button
                    Button(
                        onClick = {
                            showDatePicker(context) { date ->
                                selectedDate = date
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedDate.isEmpty()) "Pick Date" else selectedDate)
                    }

                    // Add button
                    Button(
                        onClick = {
                            if (taskText.isNotBlank()) {
                                val newTodo = TodoItem(
                                    Id = nextId++,
                                    Description = taskText,
                                    DueDate = selectedDate
                                )
                                todoList = todoList + newTodo
                                taskText = ""
                                selectedDate = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        // Todo List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todoList) { todo ->
                TodoItemCard(
                    todo = todo,
                    onToggleComplete = { todoId ->
                        todoList = todoList.map {
                            if (it.Id == todoId) it.copy(IsCompleted = !it.IsCompleted)
                            else it
                        }
                    },
                    onEdit = { todo ->
                        editingTodo = todo
                        showEditDialog = true
                    },
                    onDelete = { todo ->
                        todoToDelete = todo
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && editingTodo != null) {
        EditTodoDialog(
            todo = editingTodo!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTodo ->
                todoList = todoList.map {
                    if (it.Id == updatedTodo.Id) updatedTodo else it
                }
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && todoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${todoToDelete!!.Description}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        todoList = todoList.filter { it.Id != todoToDelete!!.Id }
                        showDeleteDialog = false
                        todoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TodoItemCard(
    todo: TodoItem,
    onToggleComplete: (Int) -> Unit,
    onEdit: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.IsCompleted) Color(0xFFE8F5E8) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = todo.IsCompleted,
                onCheckedChange = { onToggleComplete(todo.Id) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.Description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (todo.IsCompleted) TextDecoration.LineThrough else null,
                    color = if (todo.IsCompleted) Color.Gray else Color.Black
                )

                if (todo.DueDate.isNotEmpty()) {
                    Text(
                        text = "📅 Due: ${todo.DueDate}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Status
                Text(
                    text = if (todo.IsCompleted) "✅ Done" else "⏰ Pending",
                    fontSize = 12.sp,
                    color = if (todo.IsCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action buttons
            Row {
                // Edit button
                IconButton(onClick = { onEdit(todo) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF2196F3)
                    )
                }

                // Delete button
                IconButton(onClick = { onDelete(todo) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoDialog(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    var editedDescription by remember { mutableStateOf(todo.Description) }
    var editedDate by remember { mutableStateOf(todo.DueDate) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Task Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        showDatePicker(context) { date ->
                            editedDate = date
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editedDate.isEmpty()) "Pick Date" else "📅 $editedDate")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (editedDescription.isNotBlank()) {
                                onSave(todo.copy(Description = editedDescription, DueDate = editedDate))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper function for date picker
fun showDatePicker(context: android.content.Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
        onDateSelected(date)
    }, year, month, day).show()
}