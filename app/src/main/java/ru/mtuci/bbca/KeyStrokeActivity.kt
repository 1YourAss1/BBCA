package ru.mtuci.bbca

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class KeyStrokeActivity : AppCompatActivity() {
    private lateinit var textViewKeystroke: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_stroke)

        val userActivityDataWriter = userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "keystroke",
            activityName = "keystroke",
            activityColumns = listOf("timestamp", "orientation", "ascii", "letter")
        )

        textViewKeystroke = findViewById(R.id.textViewKeystroke)
        textViewKeystroke.text = "${getString(R.string.keystroke_task_text)} 100"

        findViewById<EditText>(R.id.editTextKeystroke).doOnTextChanged { text, start, before, count ->
            textViewKeystroke.text = "${getString(R.string.keystroke_task_text)} ${if(100 - text?.length!! >= 0) 100 - text?.length!! else 0}"
            if (before == 1) {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, "8", "del")
                )
            }
            if (count == 1) {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, text.last().code, text.last())
                )
            }
        }
    }
}