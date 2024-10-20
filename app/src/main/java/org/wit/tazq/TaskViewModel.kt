// TaskViewModel.kt
package org.wit.tazq

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {

    // Internal mutable list of tasks
    private val _tasks = MutableLiveData<MutableList<TaskModel>>(mutableListOf())

    // External immutable LiveData
    val tasks: LiveData<MutableList<TaskModel>> get() = _tasks

    // Function to add a new task
    fun addTask(title: String) {
        val currentList = _tasks.value
        currentList?.let {
            val newId = if (it.isEmpty()) 1 else it.maxOf { task -> task.id } + 1
            val newTask = TaskModel(id = newId, title = title)
            it.add(newTask)
            _tasks.value = it
        }
    }
}
