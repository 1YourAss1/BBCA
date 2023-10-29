package ru.mtuci.bbca.paint

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
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

class PaintActivity : AppCompatActivity() {
    private val paintView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<PaintView>(R.id.paintView)
    }

    private val clearButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.clearButton)
    }

    private val nextButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.nextButton)
    }

    private val progressView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.progress)
    }

    private val currentSessionPath by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("currentSessionPath").toString()
    }

    private val currentSessionNumber by lazy(LazyThreadSafetyMode.NONE) {
        currentSessionPath.split("session")[1]
    }

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = currentSessionPath,
            directoryName = "paint",
            activityName = "paint",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action_type")
        )
    }

    private val viewModel: PaintViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_paint)

        clearButton.setOnClickListener {
            paintView.clear()
        }

        nextButton.setOnClickListener {
            viewModel.onNextClick()
            paintView.clear()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.taskDoneSideEffect.collect {
                        Toast.makeText(this@PaintActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                launch {
                    viewModel.progress.collect { progress ->
                        progressView.text = getString(R.string.draw_task, currentSessionNumber, progress)
                    }
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
            }
            MotionEvent.ACTION_UP -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }
}