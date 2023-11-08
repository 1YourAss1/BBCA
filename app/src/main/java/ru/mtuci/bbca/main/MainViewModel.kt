package ru.mtuci.bbca.main

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.data.Task
import java.io.File
import java.io.FileOutputStream

@SuppressLint("StaticFieldLeak") // application context cant be leaked
class MainViewModel(
    private val identifier: String,
    private val applicationContext: Context,
    private val sensors: List<Sensor>
) : ViewModel() {
    private var _currentSessionNumber = MutableStateFlow(0)
    val currentSessionNumber = _currentSessionNumber.asStateFlow()

    private var _tasksState = MutableStateFlow(
        Task.values().associateWith { false }
    )

    val tasksState = _tasksState.asStateFlow()

    init {
        Preferences.init(
            context = applicationContext,
            identifier = identifier
        )

        val lastSessionPath = Preferences.getSessionPath()

        if (lastSessionPath.isBlank()) {
            startNewSession()
        } else {
            updateTasksStateByPreferences()
            _currentSessionNumber.value = getNumberFromSessionPath(lastSessionPath)
        }
    }

    fun startNewSession() {
        val resources = applicationContext.resources
        var currentSessionPath = "${applicationContext.filesDir}/user_data_$identifier"
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
            jsonInfo.put("sensors", JSONArray(sensors.map { sensor -> JSONObject("{\"name\": \"${sensor.name}\", \"vendor\": \"${sensor.vendor}\"}") }))
            FileOutputStream(jsonInfoFile).write(jsonInfo.toString().toByteArray())
        }

        val lastSessionPath = Preferences.getSessionPath()

        Preferences.reset()

        _currentSessionNumber.value = getNumberFromSessionPath(lastSessionPath) + 1

        currentSessionPath = "$currentSessionPath/session${currentSessionNumber.value}"

        File(currentSessionPath).mkdirs()

        Preferences.saveSessionPath(currentSessionPath)

        updateTasksStateByPreferences()
    }

    fun setTaskDone(task: Task) {
        _tasksState.update { states ->
            states.toMutableMap().apply {
                set(task, true)
            }
        }

        Preferences.saveTaskDone(task)
    }

    private fun getNumberFromSessionPath(path: String) =
        path.substringAfterLast("session").toIntOrNull() ?: 0

    private fun updateTasksStateByPreferences() {
        _tasksState.update { states ->
            states.mapValues { (task, _) ->
                Preferences.isTaskDone(task)
            }
        }
    }

    object IdentifierKey : CreationExtras.Key<String>
    object AppContextKey : CreationExtras.Key<Context>
    object SensorsKey : CreationExtras.Key<List<Sensor>>

    companion object {
        val factory get() = viewModelFactory {
            initializer {
                MainViewModel(
                    identifier = requireNotNull(get(IdentifierKey)),
                    applicationContext = requireNotNull(get(AppContextKey)),
                    sensors = get(SensorsKey) ?: emptyList(),
                )
            }
        }
    }
}