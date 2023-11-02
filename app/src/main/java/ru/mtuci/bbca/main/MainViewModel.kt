package ru.mtuci.bbca.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class MainViewModel : ViewModel() {
    var currentSessionPath: String = ""
        private set

    private var _tasksState = MutableStateFlow(TasksState())
    val tasksState = _tasksState.asStateFlow()

    fun startNewSession(
        context: Context,
        sensorManager: SensorManager,
        identifier: String
    ) {
        _tasksState.value = TasksState()
        val resources = context.resources
        currentSessionPath = "${context.filesDir}/user_data_$identifier"
        File(currentSessionPath).mkdirs()
        // Create info data
        val jsonInfoFile = File(currentSessionPath + File.separator + "info.json")
        if (!jsonInfoFile.exists()) {
            val jsonInfo = JSONObject()
            jsonInfo.put("screen", JSONObject().apply {
                put("width", resources.displayMetrics.widthPixels)
                put("height", resources.displayMetrics.heightPixels)
            })
            jsonInfo.put("device", JSONObject().apply {
                put("android_version", android.os.Build.VERSION.SDK_INT)
                put("device", android.os.Build.DEVICE)
                put("model", android.os.Build.MODEL)
                put("brand", android.os.Build.BRAND)
                put("manufacturer", android.os.Build.MANUFACTURER)
            })
            jsonInfo.put("sensors", JSONArray(sensorManager.getSensorList(Sensor.TYPE_ALL).map { sensor -> JSONObject("{\"name\": \"${sensor.name}\", \"vendor\": \"${sensor.vendor}\"}") }))
            FileOutputStream(jsonInfoFile).write(jsonInfo.toString().toByteArray())
        }
        // Create dir for new session
        if (File(currentSessionPath).listFiles()?.isNotEmpty() == true && File(currentSessionPath).listFiles().any { it.name.contains("session") }) {
            val newSession = "session${
                File(currentSessionPath).listFiles()?.last { it.name.contains("session") }?.name?.filter { it.isDigit() }?.toInt()?.inc()}"
            if (File("$currentSessionPath/$newSession").mkdirs()) currentSessionPath = "$currentSessionPath/$newSession"
        } else {
            if (File("$currentSessionPath/session1").mkdirs()) currentSessionPath = "$currentSessionPath/session1"
        }
    }

    fun setKeyStrokeTaskDone() {
        _tasksState.update {
            it.copy(isKeyStrokeDone = true)
        }
    }

    fun setScrollTaskDone() {
        _tasksState.update {
            it.copy(isScrollDone = true)
        }
    }

    fun setSwipeTaskDone() {
        _tasksState.update {
            it.copy(isSwipeDone = true)
        }
    }

    fun setScaleTaskDone() {
        _tasksState.update {
            it.copy(isScaleDone = true)
        }
    }

    fun setClicksTaskDone() {
        _tasksState.update {
            it.copy(isClicksDone = true)
        }
    }

    fun setVideoTaskDone() {
        _tasksState.update {
            it.copy(isVideoDone = true)
        }
    }

    fun setLongClicksTaskDone() {
        _tasksState.update {
            it.copy(isLongClicksDone = true)
        }
    }

    fun setPaintTaskDone() {
        _tasksState.update {
            it.copy(isPaintDone = true)
        }
    }
}