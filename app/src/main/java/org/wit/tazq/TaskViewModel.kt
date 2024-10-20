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

    private val _tasks = MutableLiveData<MutableList<TaskModel>>(mutableListOf())
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
        val currentList = _tasks.value
        currentList?.let {
            val newId = if (it.isEmpty()) 1 else it.maxOf { task -> task.id } + 1
            val newTask = TaskModel(id = newId, title = title)
            it.add(newTask)
            _tasks.value = it
            saveTasksToJson()
            Log.d(TAG, "Added task: $newTask")
        }
    }

    /**
     * Adds a task at a specific position (used for Undo).
     */
    fun addTaskAt(task: TaskModel, position: Int) {
        _tasks.value?.add(position, task)
        _tasks.value = _tasks.value
        saveTasksToJson()
        Log.d(TAG, "Restored task: $task at position $position")
    }

    /**
     * Deletes a specific task.
     */
    fun deleteTask(task: TaskModel) {
        _tasks.value?.remove(task)
        _tasks.value = _tasks.value
        saveTasksToJson()
        Log.d(TAG, "Deleted task: $task")
    }

    /**
     * Updates an existing task.
     */
    fun updateTask(updatedTask: TaskModel) {
        val currentList = _tasks.value
        val index = currentList?.indexOfFirst { it.id == updatedTask.id }
        if (index != null && index != -1) {
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
