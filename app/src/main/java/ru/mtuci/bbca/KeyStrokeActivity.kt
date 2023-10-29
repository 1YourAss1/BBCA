package ru.mtuci.bbca

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class KeyStrokeActivity : AppCompatActivity() {
    private lateinit var textViewKeystroke: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keystroke)

        val userActivityDataWriter = userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "keystroke",
            activityName = "keystroke",
            activityColumns = listOf("timestamp", "orientation", "ascii", "letter")
        )

        sensorsDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "keystroke",
        )

        textViewKeystroke = findViewById(R.id.textViewKeystroke)
        textViewKeystroke.text = "${getString(R.string.keystroke_task)} 0/100"

        findViewById<EditText>(R.id.editTextKeystroke).doOnTextChanged { text, start, before, count ->
            textViewKeystroke.text = "${getString(R.string.keystroke_task)} ${if(text?.length!! <= 100) text?.length!! else 100}/100"
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