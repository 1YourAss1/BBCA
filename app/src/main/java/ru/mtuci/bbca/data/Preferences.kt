package ru.mtuci.bbca.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {
    private const val PREFS_NAME = "bbca_shared_prefs"
    private const val CURRENT_SESSION_PATH_KEY = "current_session_path_key"
    private const val BASE_TASK_DONE_KEY = "task_done_key"

    private var _sharedPreferences: SharedPreferences? = null
    private val sharedPreferences get() = requireNotNull(_sharedPreferences)

    fun init(context: Context, identifier: String) {
        _sharedPreferences = context.getSharedPreferences(
            "${PREFS_NAME}_$identifier",
            Context.MODE_PRIVATE
        )
    }

    fun saveSessionPath(path: String) {
        sharedPreferences.edit {
            putString(CURRENT_SESSION_PATH_KEY, path)
        }
    }

    fun getSessionPath() =
        sharedPreferences.getString(CURRENT_SESSION_PATH_KEY, "") ?: ""

    fun saveTaskDone(task: Task) {
        sharedPreferences.edit {
            putBoolean(task.key(), true)
        }
    }

    fun isTaskDone(task: Task) =
        sharedPreferences.getBoolean(task.key(), false)

    fun reset() {
        sharedPreferences.edit { clear() }
    }

    private fun Task.key() = "${BASE_TASK_DONE_KEY}_${name.lowercase()}"
}