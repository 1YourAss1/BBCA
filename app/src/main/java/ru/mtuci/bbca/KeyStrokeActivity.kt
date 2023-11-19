package ru.mtuci.bbca

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.data.Task
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class KeyStrokeActivity : AppCompatActivity() {
    private lateinit var textViewKeystroke: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        setContentView(R.layout.activity_keystroke)

        val userActivityDataWriter = userActivityDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "keystroke",
            activityName = "keystroke",
            activityColumns = listOf("timestamp", "orientation", "ascii")
        )

        sensorsDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "keystroke",
        )

        textViewKeystroke = findViewById(R.id.textViewKeystroke)
        textViewKeystroke.text = "${getString(R.string.keystroke_task)} 0/100"

        findViewById<EditText>(R.id.editTextKeystroke).doOnTextChanged { text, start, before, count ->
            textViewKeystroke.text = "${getString(R.string.keystroke_task)} ${if(text?.length!! <= 100) text?.length!! else 100}/100"
            if (before == 1) {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, "8")
                )
            }
            if (count == 1) {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, text.last().code)
                )
            }
            if (text.length >= 100) {
                sendBroadcast(
                    Intent(MainActivity.TASK_DONE_KEY).apply {
                        putExtra(MainActivity.TASK_DONE_KEY, Task.KEY_STROKE)
                    }
                )
            }
        }
    }
}