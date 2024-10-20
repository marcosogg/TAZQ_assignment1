// TaskAdapter.kt
package org.wit.tazq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: MutableList<TaskModel>,
    private val onTaskClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    /**
     * ViewHolder class that holds the view for each task item.
     */
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTaskTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Inflate the item_task.xml layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Bind the task title to the TextView
        val task = tasks[position]
        holder.textViewTitle.text = task.title

        // Set click listener for editing
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    /**
     * Removes a task at the specified position.
     */
    fun removeTask(position: Int): TaskModel {
        val removedTask = tasks.removeAt(position)
        notifyItemRemoved(position)
        return removedTask
    }

    /**
     * Restores a task at the specified position.
     */
    fun restoreTask(task: TaskModel, position: Int) {
        tasks.add(position, task)
        notifyItemInserted(position)
    }

    /**
     * Updates the entire task list.
     */
    fun updateTasks(newTasks: MutableList<TaskModel>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
