// MainActivity.kt
package org.wit.tazq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Tag for logging
    private val TAG = "MainActivity"

    // Initialize the TaskViewModel using the viewModels delegate
    private val taskViewModel: TaskViewModel by viewModels()

    // Declare UI components
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var textViewEmptyState: TextView
    private lateinit var fabAddTask: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to activity_main.xml
        setContentView(R.layout.activity_main)

        // Initialize views by finding them by their IDs
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        textViewEmptyState = findViewById(R.id.textViewEmptyState)
        fabAddTask = findViewById(R.id.fabAddTask)

        // Set up RecyclerView with a LinearLayoutManager and TaskAdapter
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(emptyList())
        recyclerViewTasks.adapter = taskAdapter

        // Observe the tasks LiveData from the ViewModel
        taskViewModel.tasks.observe(this, Observer { tasks ->
            Log.d(TAG, "Task list updated: $tasks")
            if (tasks.isEmpty()) {
                // If no tasks, show the empty state view and hide RecyclerView
                recyclerViewTasks.visibility = View.GONE
                textViewEmptyState.visibility = View.VISIBLE
            } else {
                // If tasks exist, show RecyclerView and hide empty state view
                recyclerViewTasks.visibility = View.VISIBLE
                textViewEmptyState.visibility = View.GONE
                // Update the adapter with the new list of tasks
                taskAdapter.updateTasks(tasks)
            }
        })

        // Set up FloatingActionButton click listener to open Add Task dialog
        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    /**
     * Displays an AlertDialog to add a new task.
     * Includes input validation to ensure the task title is not empty.
     */
    private fun showAddTaskDialog() {
        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Task")

        // Inflate the dialog_add_task.xml layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)
        builder.setView(dialogView)

        // Find the EditText within the dialog
        val editTextTaskTitle: EditText = dialogView.findViewById(R.id.editTextTaskTitle)

        // Set up the "Add" button
        builder.setPositiveButton("Add") { dialog, _ ->
            val taskTitle = editTextTaskTitle.text.toString().trim()
            if (taskTitle.isNotEmpty()) {
                // Add the task to the ViewModel
                taskViewModel.addTask(taskTitle)
                // Show a Toast message confirming the addition
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                // If the input is empty, set an error on the EditText
                editTextTaskTitle.error = "Task title cannot be empty"
                Log.e(TAG, "Attempted to add a task with an empty title")
            }
        }

        // Set up the "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Create the AlertDialog
        val dialog = builder.create()

        // Disable the "Add" button initially
        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = false

            // Add a TextWatcher to enable the "Add" button when input is not empty
            editTextTaskTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    button.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // Show the dialog
        dialog.show()
    }

    /**
     * Inflates the menu resource into the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handles menu item selections.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle the Settings action
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Settings menu item clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
