package ru.mtuci.bbca.sensors_data_writer

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream

class UserActivityDataWriter(
    private val lifecycle: Lifecycle,
    private val currentSessionPath: String,
    private val directoryName: String,
    private val activityName: String,
    private val activityColumns: List<String>,
) : DefaultLifecycleObserver {

    private val sessionActivityPath = currentSessionPath + File.separator + directoryName + File.separator
    private val activityFilePath = "${sessionActivityPath}${File.separator}${activityName}.csv"

    private var activityFileOutput: FileOutputStream? = null

    init {
        if (!File(activityFilePath).exists()) {
            File(sessionActivityPath).mkdir()
            FileOutputStream("${sessionActivityPath}${File.separator}${activityName}.csv").apply { write("${activityColumns.joinToString(separator = ",")}\n".toByteArray()) }
        }

        lifecycle.addObserver(this)
    }

    fun writeActivity(data: List<Any>) {
        check(data.size == activityColumns.size)
        activityFileOutput?.write("${data.joinToString(separator = ",")}\n".toByteArray())
    }

    override fun onPause(owner: LifecycleOwner) {
        activityFileOutput?.close()
        activityFileOutput = null
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        activityFileOutput = FileOutputStream("${sessionActivityPath}${File.separator}${activityName}.csv", true)
    }
}