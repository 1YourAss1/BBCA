package ru.mtuci.bbca.scroll

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.mtuci.bbca.R
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter


class ScrollActivity : AppCompatActivity() {
    private lateinit var textViewTouch: TextView
    private lateinit var textViewToRead: TextView

    private val viewModel: ScrollViewModel by viewModels()

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "scroll",
            activityName = "scroll",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scroll)
        textViewTouch = findViewById(R.id.textViewTouch)

        textViewToRead = findViewById(R.id.textViewToRead)
        textViewToRead.movementMethod = ScrollingMovementMethod()

        textViewToRead.setOnScrollChangeListener { _, _, _, _, _ ->
            viewModel.onScroll(isScrolledToBottom = !textViewToRead.canScrollVertically(1))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskDoneSideEffect.collect {
                    Toast.makeText(this@ScrollActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_MOVE -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
                textViewTouch.text ="X: %.3f ".format(event?.x) + "Y: %.3f ".format(event?.y) + "P: %.3f".format(event?.pressure)
            }
            MotionEvent.ACTION_UP -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
                textViewTouch.text = ""
            }

        }

        return super.dispatchTouchEvent(event)
    }
}