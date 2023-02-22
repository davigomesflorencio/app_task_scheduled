package ufc.insight.ractivity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class TaskModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var done: Boolean = false,
    var scheduled: Boolean = false,
    var timeStart:Long,
    var timeEnd:Long,
)