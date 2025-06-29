package com.Jhoao.todoapp

data class TodoItem(
    val Id: Int,
    var Description: String,
    var DueDate: String = "",
    var IsCompleted: Boolean = false
)