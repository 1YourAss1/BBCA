package ru.mtuci.bbca

import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.otaliastudios.zoom.ZoomImageView
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class ScaleActivity : AppCompatActivity() {
    private lateinit var imageView: ZoomImageView

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "scale",
            activityName = "scale",
            activityColumns = listOf("timestamp", "orientation", "x1_coordinate", "y1_coordinate", "pressure1", "x2_coordinate", "y2_coordinate", "pressure2", "action_type")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorsDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "scale",
        )

        setContentView(R.layout.activity_scale)
        imageView = findViewById(R.id.zoomageView)

        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.scale))

        val characterList: ArrayList<String> = arrayListOf("Уолли (Где Уолли?)", "Бэтмен", "Каонаси (Унесённые призраками)", "Саб-Зиро (Mortal Kombat)", "Скорпион (Mortal Kombat)")
        var currentCharacter = 0
        var taskDone = false
        findViewById<TextView>(R.id.textViewFindCharacter).apply {
            text =
                "${resources.getText(R.string.scale_task)}: ${characterList[0]}\n$currentCharacter/${characterList.size}"
            setOnClickListener {
                if (currentCharacter.inc() == characterList.size) taskDone = true
                currentCharacter = currentCharacter.inc().mod(characterList.size)
                text =
                    "${resources.getText(R.string.scale_task)}: ${characterList[currentCharacter]}\n${if (!taskDone) currentCharacter else characterList.size}/${characterList.size}"
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                userActivityDataWriter.writeActivity(
                    listOf(
                        System.currentTimeMillis(), resources.configuration.orientation,
                        event.getX(0), event.getY(0), event.getPressure(0),
                        event.getX(1), event.getY(1), event.getPressure(1),
                        0
                    )
                )
            }

            MotionEvent.ACTION_MOVE -> if (event.pointerCount == 2) {
                userActivityDataWriter.writeActivity(
                    listOf(
                        System.currentTimeMillis(), resources.configuration.orientation,
                        event.getX(0), event.getY(0), event.getPressure(0),
                        event.getX(1), event.getY(1), event.getPressure(1),
                        2
                    )
                )
            }

            MotionEvent.ACTION_POINTER_UP -> {
                userActivityDataWriter.writeActivity(
                    listOf(
                        System.currentTimeMillis(), resources.configuration.orientation,
                        event.getX(0), event.getY(0), event.getPressure(0),
                        event.getX(1), event.getY(1), event.getPressure(1),
                        1
                    )
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }
}