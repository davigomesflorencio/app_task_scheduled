package ufc.insight.ractivity.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ufc.insight.ractivity.R
import ufc.insight.ractivity.data.database.TaskDatabase
import ufc.insight.ractivity.data.model.TaskModel
import ufc.insight.ractivity.ui.RunTaskActivity
import ufc.insight.ractivity.util.TimeUtils
import ufc.insight.ractivity.util.ToastUtils


class TaskListAdapter(private val list: List<TaskModel>) :
    RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        return TaskViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    class TaskViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val db by lazy {
            TaskDatabase.getDatabase(itemView.context)
        }

        fun bind(task: TaskModel) {
            val nameTask: TextView = itemView.findViewById(R.id.nameTask)
            val txtHora: TextView = itemView.findViewById(R.id.txtHora)
            val deleteTask: ImageButton = itemView.findViewById(R.id.deletarAtividade)
            val editTask: ImageButton = itemView.findViewById(R.id.visualizarAtividade)

            with(itemView) {
                nameTask.text = task.name
                txtHora.text = "  " +
                        TimeUtils.updateTime(task.timeStart) + " - " + TimeUtils.updateTime(task.timeEnd)
            }

            deleteTask.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO) {
                    db.taskDao().deleteById(task.id)
                }
            }

            editTask.setOnClickListener {
                val myIntent = Intent(itemView.context, RunTaskActivity::class.java)
                myIntent.putExtra("id", task.id)
                startActivity(itemView.context, myIntent, null)
            }

        }

    }


}