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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

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
        taskAdapter = TaskAdapter(mutableListOf()) { task ->
            showEditTaskDialog(task)
        }
        recyclerViewTasks.adapter = taskAdapter

        // Observe the tasks LiveData from the ViewModel
        taskViewModel.tasks.observe(this, Observer { tasks ->
            if (tasks.isEmpty()) {
                recyclerViewTasks.visibility = View.GONE
                textViewEmptyState.visibility = View.VISIBLE
            } else {
                recyclerViewTasks.visibility = View.VISIBLE
                textViewEmptyState.visibility = View.GONE
                taskAdapter.updateTasks(tasks)
            }
        })

        // Set up FloatingActionButton click listener to open Add Task dialog
        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Attach ItemTouchHelper for swipe-to-delete functionality
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // We are not implementing onMove in this case
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Get the position of the swiped item
                val position = viewHolder.adapterPosition
                // Get the task to be removed
                val removedTask = taskAdapter.getTaskAt(position)
                // Delete the task via ViewModel
                taskViewModel.deleteTask(removedTask)

                // Show Snackbar with Undo option
                Snackbar.make(recyclerViewTasks, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        // Restore the task via ViewModel
                        taskViewModel.addTaskAt(removedTask, position)
                    }
                    .setBackgroundTint(ContextCompat.getColor(this@MainActivity, R.color.primaryColor))
                    .setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    .show()
            }
        }

        // Attach the ItemTouchHelper to the RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewTasks)
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

        // Find the TextInputEditText within the dialog
        val editTextTaskTitle: EditText = dialogView.findViewById(R.id.editTextTaskTitle)

        // Set up the "Add" button
        builder.setPositiveButton("Add") { dialog, _ ->
            val taskTitle = editTextTaskTitle.text.toString().trim()
            if (taskTitle.isNotEmpty()) {
                // Add the task to the ViewModel
                taskViewModel.addTask(taskTitle)
                // Show a Snackbar confirming the addition
                Snackbar.make(recyclerViewTasks, "Task added", Snackbar.LENGTH_SHORT).show()
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
            button.isEnabled = editTextTaskTitle.text.toString().trim().isNotEmpty()

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
     * Displays an AlertDialog to edit an existing task.
     * Includes input validation to ensure the task title is not empty.
     */
    private fun showEditTaskDialog(task: TaskModel) {
        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")

        // Inflate the dialog_add_task.xml layout (reusing the same layout for add and edit)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)
        builder.setView(dialogView)

        // Find the TextInputEditText within the dialog and set the current task title
        val editTextTaskTitle: EditText = dialogView.findViewById(R.id.editTextTaskTitle)
        editTextTaskTitle.setText(task.title)

        // Set up the "Save" button
        builder.setPositiveButton("Save") { dialog, _ ->
            val updatedTitle = editTextTaskTitle.text.toString().trim()
            if (updatedTitle.isNotEmpty()) {
                // Create an updated TaskModel
                val updatedTask = task.copy(title = updatedTitle)
                // Update the task in the ViewModel
                taskViewModel.updateTask(updatedTask)
                // Show a Snackbar confirming the update
                Snackbar.make(recyclerViewTasks, "Task updated", Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                // If the input is empty, set an error on the EditText
                editTextTaskTitle.error = "Task title cannot be empty"
                Log.e(TAG, "Attempted to update a task with an empty title")
            }
        }

        // Set up the "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Create the AlertDialog
        val dialog = builder.create()

        // Enable the "Save" button only if input is not empty
        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = editTextTaskTitle.text.toString().trim().isNotEmpty()

            // Add a TextWatcher to enable the "Save" button when input is not empty
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
                // Handle the Settings action (to be implemented)
                Snackbar.make(recyclerViewTasks, "Settings clicked", Snackbar.LENGTH_SHORT).show()
                Log.d(TAG, "Settings menu item clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
