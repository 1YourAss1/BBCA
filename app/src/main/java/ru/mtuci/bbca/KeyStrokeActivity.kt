package ru.mtuci.bbca

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.data.Task
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter
import kotlin.math.abs

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

        val editTextKeystroke = findViewById<EditText>(R.id.editTextKeystroke)
        
        editTextKeystroke.filters = arrayOf(
            object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    val newDest = dest?.replaceRange(dstart, dend, source ?: "")

                    if (newDest.isNullOrBlank()) {
                        return null
                    }

                    if (abs((newDest.length) - (dest.length)) > 1) {
                        return dest.subSequence(dstart, dend)
                    }

                    return null
                }
            }
        )

        editTextKeystroke.addTextChangedListener(object : TextWatcher {
            var before = ""

            override fun afterTextChanged(s: Editable?) {
                val after = s?.toString() ?: ""

                val ascii = when {
                    after.length < before.length -> 8
                    else -> {
                        var newCharIndex = -1

                        for (i in 0 until Integer.max(before.length, after.length)) {
                            val beforeChar = before.getOrNull(i)
                            val newChar = after.getOrNull(i)

                            if (beforeChar != newChar) {
                                newCharIndex = i
                                break
                            }
                        }

                        after.getOrNull(newCharIndex)?.code
                    }
                }

                if (ascii != null) {
                    userActivityDataWriter.writeActivity(
                        listOf(
                            System.currentTimeMillis(),
                            resources.configuration.orientation,
                            ascii
                        )
                    )
                }

                textViewKeystroke.text = "${getString(R.string.keystroke_task)} ${if(after.length <= 100) after.length else 100}/100"

                if (after.length >= 100) {
                    sendBroadcast(
                        Intent(MainActivity.TASK_DONE_KEY).apply {
                            putExtra(MainActivity.TASK_DONE_KEY, Task.KEY_STROKE)
                        }
                    )
                }
            }

            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                before = text?.toString() ?: ""
            }

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit
        })
    }
}