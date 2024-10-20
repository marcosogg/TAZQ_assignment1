// TaskViewModel.kt
package org.wit.tazq

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "TaskViewModel"

    // Initialize _tasks as non-nullable and with an empty list
    private val _tasks = MutableLiveData<MutableList<TaskModel>>().apply { value = mutableListOf() }
    val tasks: LiveData<MutableList<TaskModel>> get() = _tasks

    private val gson = Gson()
    private val filename = "tasks.json"

    init {
        loadTasksFromJson()
    }

    /**
     * Adds a new task with a unique ID.
     */
    fun addTask(title: String) {
        val currentList = _tasks.value ?: mutableListOf()
        val newId = if (currentList.isEmpty()) 1 else currentList.maxOf { it.id } + 1
        val newTask = TaskModel(id = newId, title = title)
        currentList.add(newTask)
        _tasks.value = currentList
        saveTasksToJson()
        Log.d(TAG, "Added task: $newTask")
    }

    /**
     * Adds a task at a specific position (used for Undo).
     */
    fun addTaskAt(task: TaskModel, position: Int) {
        val currentList = _tasks.value ?: mutableListOf()
        if (position >= 0 && position <= currentList.size) {
            currentList.add(position, task)
            _tasks.value = currentList
            saveTasksToJson()
            Log.d(TAG, "Restored task: $task at position $position")
        } else {
            Log.e(TAG, "Invalid position: $position for restoring task: $task")
        }
    }

    /**
     * Deletes a specific task.
     */
    fun deleteTask(task: TaskModel) {
        val currentList = _tasks.value ?: mutableListOf()
        if (currentList.remove(task)) {
            _tasks.value = currentList
            saveTasksToJson()
            Log.d(TAG, "Deleted task: $task")
        } else {
            Log.e(TAG, "Attempted to delete a task that doesn't exist: $task")
        }
    }

    /**
     * Updates an existing task.
     */
    fun updateTask(updatedTask: TaskModel) {
        val currentList = _tasks.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            currentList[index] = updatedTask
            _tasks.value = currentList
            saveTasksToJson()
            Log.d(TAG, "Updated task: $updatedTask")
        } else {
            Log.e(TAG, "Task to update not found: $updatedTask")
        }
    }

    /**
     * Saves the current task list to a JSON file.
     */
    private fun saveTasksToJson() {
        val jsonString = gson.toJson(_tasks.value)
        try {
            getApplication<Application>().openFileOutput(filename, Application.MODE_PRIVATE).use { output ->
                output.write(jsonString.toByteArray())
            }
            Log.d(TAG, "Tasks saved to JSON")
        } catch (e: IOException) {
            Log.e(TAG, "Error saving tasks to JSON", e)
        }
    }

    /**
     * Loads tasks from the JSON file into LiveData.
     */
    private fun loadTasksFromJson() {
        try {
            val jsonString = getApplication<Application>().openFileInput(filename).bufferedReader().useLines { lines ->
                lines.fold("") { some, text -> "$some$text" }
            }
            val listType = object : TypeToken<MutableList<TaskModel>>() {}.type
            val loadedTasks: MutableList<TaskModel> = gson.fromJson(jsonString, listType) ?: mutableListOf()
            _tasks.value = loadedTasks
            Log.d(TAG, "Tasks loaded from JSON")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading tasks from JSON", e)
            _tasks.value = mutableListOf()
        }
    }
}
