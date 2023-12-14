package ru.mtuci.bbca.scale

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.coroutines.launch
import ru.mtuci.bbca.R
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.data.Preferences
import ru.mtuci.bbca.data.Task
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.scale.overlay_image_view.OverlayImageView
import ru.mtuci.bbca.scale.overlay_image_view.OverlayItem
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class ScaleActivity : AppCompatActivity() {

    private val imageView: OverlayImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.zoomageView)
    }

    private val progressView: TextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.textViewFindCharacter)
    }

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "scale",
            activityName = "scale",
            activityColumns = listOf("timestamp", "orientation", "x1_coordinate", "y1_coordinate", "pressure1", "x2_coordinate", "y2_coordinate", "pressure2", "action_type")
        )
    }

    private val characters by lazy(LazyThreadSafetyMode.NONE) {
        CharactersProvider.provide(resources)
    }

    private val viewModel: ScaleViewModel by viewModels(
        extrasProducer = {
            MutableCreationExtras().apply {
                set(
                    key = ScaleViewModel.CharactersKey,
                    t = characters
                )
            }
        },
        factoryProducer = { ScaleViewModel.factory }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        sensorsDataWriter(
            currentSessionPath = Preferences.getSessionPath(),
            directoryName = "scale",
        )

        setContentView(R.layout.activity_scale)

        imageView.setImage(ImageSource.asset("scale.jpg"))

        imageView.maxScale = 10f
        imageView.minScale = 1f
        imageView.items = viewModel.characters

        imageView.setOverlayItemsOnClickListener { items ->
            viewModel.onCharacterClick(items)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.progress.collect { (index, character) ->
                        progressView.text = getString(
                            R.string.scale_task,
                            character?.name ?: "",
                            index,
                            viewModel.characters.size
                        )

                        imageView.debugHighlightedItemId = character?.id

                        if (index != 0) {
                            imageView.triggerRipple()
                        }

                        if (imageView.isReady) {
                            imageView.animateScaleAndCenter(imageView.minScale, imageView.center)?.start()
                        }
                    }
                }

                launch {
                    viewModel.taskDoneSideEffect.collect {
                        Toast.makeText(this@ScaleActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                            .show()

                        sendBroadcast(
                            Intent(MainActivity.TASK_DONE_KEY).apply {
                                putExtra(MainActivity.TASK_DONE_KEY, Task.SCALE)
                            }
                        )
                    }
                }
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