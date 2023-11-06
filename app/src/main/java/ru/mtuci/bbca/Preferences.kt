package ru.mtuci.bbca

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {
    private const val PREFS_NAME = "bbca_shared_prefs"
    private const val CURRENT_SESSION_PATH_KEY = "current_session_path_key"

    private var _sharedPreferences: SharedPreferences? = null
    private val sharedPreferences get() = requireNotNull(_sharedPreferences)

    fun init(context: Context) {
        _sharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
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
}