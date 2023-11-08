package ru.mtuci.bbca.clicks

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.R
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.data.Task
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class ClicksActivity : AppCompatActivity() {
    private val viewModel: ClicksViewModel by viewModels()

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "clicks",
            activityName = "clicks",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        sensorsDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "clicks",
        )

        setContentView(R.layout.activity_clicks)

        val buttons = findViewById<GridLayout>(R.id.buttons).children
        val startButton = findViewById<Button>(R.id.startButton)
        val progressView = findViewById<TextView>(R.id.progress)

        startButton.setOnClickListener {
            viewModel.onStartClick()
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.onButtonClick(index)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.visibleButtonsIndexes.collect { indexes ->
                        buttons.forEachIndexed { index, button ->
                            button.isInvisible = !indexes.contains(index)
                        }
                    }
                }

                launch {
                    viewModel.progress.collect { progress ->
                        progressView.text = "${resources.getText(R.string.click_task)}: $progress/10"
                    }
                }

                launch {
                    viewModel.startButtonState.collect { state ->
                        when (state) {
                            ClicksStartButtonState.IN_PROGRESS -> {
                                startButton.isVisible = true
                                startButton.isEnabled = false
                            }
                            ClicksStartButtonState.IDLE -> {
                                startButton.isVisible = true
                                startButton.isEnabled = true
                            }
                            ClicksStartButtonState.GONE -> {
                                startButton.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    viewModel.taskDoneSideEffect.collect {
                        Toast.makeText(this@ClicksActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                            .show()

                        sendBroadcast(
                            Intent(MainActivity.TASK_DONE_KEY).apply {
                                putExtra(MainActivity.TASK_DONE_KEY, Task.CLICKS)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                userActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure)
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }
}