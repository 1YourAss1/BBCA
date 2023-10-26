package ru.mtuci.bbca

import android.os.Bundle
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import ru.mtuci.bbca.sensors_data_writer.sensorsDataWriter
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class SwipeActivity : FragmentActivity(),
    GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
    private lateinit var textViewTouch: TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorsDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "swipe",
        )

        setContentView(R.layout.activity_swipe)

        textViewTouch = findViewById(R.id.textViewTouch)

        val ranInts = generateSequence { (0..941).random() }.distinct().take(50).toSet().toIntArray()
        val adapter = NumberAdapter(this, ranInts)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = adapter
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                doubleClickActivityDataWriter.writeActivity(
                    listOf(timeMillisOf(event.eventTime), resources.configuration.orientation, event.x, event.y, event.pressure, 2)
                )

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
                textViewTouch.text ="X: %.3f ".format(event?.x) + "Y: %.3f ".format(event?.y) + "P: %.3f".format(event?.pressure)
            }
            MotionEvent.ACTION_UP -> {
                swipeActivityDataWriter.writeActivity(
                    listOf(System.currentTimeMillis(), resources.configuration.orientation, event.x, event.y, event.pressure, event.action)
                )
                textViewTouch.text = ""
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
}