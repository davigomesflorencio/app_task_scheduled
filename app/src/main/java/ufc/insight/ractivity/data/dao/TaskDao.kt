package ufc.insight.ractivity.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ufc.insight.ractivity.data.model.TaskModel

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table ORDER BY name ASC")
    fun getAllTasks(): LiveData<List<TaskModel>>

    @Insert
    suspend fun insert(task: TaskModel): Long

    @Query("Update task_table Set done = 1 where id=:id")
    fun finish(id: Long)

    @Query("Update task_table Set scheduled = 1 where id=:id")
    fun scheduled(id: Long)

    @Query("Update task_table Set scheduled = 0 where id=:id")
    fun finishScheduled(id: Long)

    @Query("Delete from task_table where id=:id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM task_table WHERE id=:id ")
    fun findById(id: Long): TaskModel

}