package ru.mtuci.bbca.clicks

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
import ru.mtuci.bbca.R
import ru.mtuci.bbca.core.TrackingFrameLayout
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class ClicksActivity : AppCompatActivity() {
    private val viewModel: ClicksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicks)

        val userActivityDataWriter = userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "clicks",
            activityName = "clicks",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure")
        )

        val trackingLayout = findViewById<TrackingFrameLayout>(R.id.trackingLayout)
        val buttons = findViewById<GridLayout>(R.id.buttons).children
        val startButton = findViewById<Button>(R.id.startButton)
        val progressView = findViewById<TextView>(R.id.progress)

        trackingLayout.setOnTouchEventListener { event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    userActivityDataWriter.writeActivity(
                        listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure)
                    )
                }
            }
        }

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
                        progressView.text = "$progress/10"
                    }
                }

                launch {
                    viewModel.startButtonState.collect { state ->
                        when (state) {
                            ClicksStartButtonState.IN_PROGRESS -> {
                                startButton.isVisible = true
                                startButton.isEnabled = false
                                startButton.setText(R.string.click_all_buttons)
                            }
                            ClicksStartButtonState.IDLE -> {
                                startButton.isVisible = true
                                startButton.isEnabled = true
                                startButton.setText(R.string.start)
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
                    }
                }
            }
        }
    }
}