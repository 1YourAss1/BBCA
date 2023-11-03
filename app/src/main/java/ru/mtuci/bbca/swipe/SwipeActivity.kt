package ru.mtuci.bbca.swipe

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import ru.mtuci.bbca.NumberAdapter
import ru.mtuci.bbca.R
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.main.MainActivity
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class SwipeActivity : FragmentActivity(),
    GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    private val swipeActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "swipe",
            activityName = "swipe",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action")
        )
    }

    private val doubleClickActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "swipe",
            activityName = "double_click",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "click_number")
        )
    }

    private val gestureDetector by lazy(LazyThreadSafetyMode.NONE) {
        GestureDetectorCompat(this, this).apply {
            setOnDoubleTapListener(this@SwipeActivity)
        }
    }

    private val viewPager by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ViewPager2>(R.id.viewPager)
    }

    private val progressView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.textViewSwipeTask)
    }

    private val likeProgressView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.textViewSwipeLikeTask)
    }

    private val viewModel: SwipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        sensorsDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "swipe",
        )

        setContentView(R.layout.activity_swipe)

        val ranInts = (0..941).shuffled().take(50).toIntArray()
        val adapter = NumberAdapter(this, ranInts)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageSelected(position)
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.progress.collect { progress ->
                        progressView.text = getString(R.string.swipe_task, progress)
                    }
                }

                launch {
                    viewModel.likedPositions.collect { likedPositions ->
                        likeProgressView.text = getString(R.string.swipe_like_task, likedPositions.size)
                    }
                }

                launch {
                    viewModel.taskDoneSideEffect.collect {
                        Toast.makeText(this@SwipeActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                            .show()

                        sendBroadcast(
                            Intent(MainActivity.TASK_DONE_KEY).apply {
                                putExtra(MainActivity.TASK_DONE_KEY, SWIPE_TASK)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                doubleClickActivityDataWriter.writeActivity(
                    listOf(timeMillisOf(event.eventTime), resources.configuration.orientation, event.x, event.y, event.pressure, 2)
                )

                viewModel.onPageLiked(viewPager.currentItem)

                Toast.makeText(this, R.string.like, Toast.LENGTH_SHORT).show()

                return true
            }
        }

        return false
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                doubleClickActivityDataWriter.writeActivity(
                    listOf(timeMillisOf(event.eventTime), resources.configuration.orientation, event.x, event.y, event.pressure, 1)
                )
            }
        }

        return false
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                swipeActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_MOVE -> {
                swipeActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
            MotionEvent.ACTION_UP -> {
                swipeActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onSingleTapConfirmed(e: MotionEvent) = false

    override fun onDown(e: MotionEvent) = false

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent) = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ) = false

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ) = false

    private fun timeMillisOf(eventMillis: Long) =
        System.currentTimeMillis() - SystemClock.uptimeMillis() + eventMillis

    companion object {
        const val SWIPE_TASK = "SWIPE_TASK"
    }
}