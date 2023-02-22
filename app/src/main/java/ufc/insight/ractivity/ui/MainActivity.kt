package ufc.insight.ractivity.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import ufc.insight.ractivity.R
import ufc.insight.ractivity.database.TaskDatabase
import ufc.insight.ractivity.databinding.ActivityMainBinding
import ufc.insight.ractivity.model.TaskModel
import ufc.insight.ractivity.ui.adapter.TaskListAdapter

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerViewAtividades: RecyclerView
    val taskModels = arrayListOf<TaskModel>()
    var taskListAdapter = TaskListAdapter(taskModels)

    val db by lazy {
        TaskDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerViewAtividades = binding.recyclerview

        recyclerViewAtividades.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.taskListAdapter
            val divider = DividerItemDecoration(
                baseContext, (layoutManager as LinearLayoutManager).orientation
            )
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.divider)!!)
            addItemDecoration(divider)

        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddTaskActivity::class.java))
        }

        db.taskDao().getAllTasks().observe(this) {
            if (!it.isNullOrEmpty()) {
                taskModels.clear()
                taskModels.addAll(it)
                taskListAdapter.notifyDataSetChanged()
            } else {
                taskModels.clear()
                taskListAdapter.notifyDataSetChanged()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (!hasLocationPermissions()) {
            EasyPermissions.requestPermissions(
                this,
                "MainActivity",
                LOCATION_PERMISSION_REQUEST,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            EasyPermissions.requestPermissions(
                this,
                "MainActivity",
                LOCATION_PERMISSION_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            EasyPermissions.requestPermissions(
                this,
                "MainActivity",
                LOCATION_PERMISSION_REQUEST,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) && EasyPermissions.hasPermissions(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) && EasyPermissions.hasPermissions(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

//                && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && EasyPermissions.hasPermissions(
//            this,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION
//        ))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d("MainActivity", "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d("MainActivity", "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d("MainActivity", "onRationaleAccepted:" + requestCode)
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d("MainActivity", "onRationaleDenied:" + requestCode)
    }

}