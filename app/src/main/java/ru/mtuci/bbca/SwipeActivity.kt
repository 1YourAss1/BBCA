package ru.mtuci.bbca

import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import ru.mtuci.bbca.sensors_data_writer.userActivityDataWriter

class SwipeActivity : FragmentActivity() {
    private lateinit var textViewTouch: TextView

    private val userActivityDataWriter by lazy(LazyThreadSafetyMode.NONE) {
        userActivityDataWriter(
            currentSessionPath = intent.getStringExtra("currentSessionPath").toString(),
            directoryName = "swipe",
            activityName = "swipe",
            activityColumns = listOf("timestamp", "orientation", "x_coordinate", "y_coordinate", "pressure", "action")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)
        textViewTouch = findViewById(R.id.textViewTouch)



        val ranInts = generateSequence { (0..941).random() }.distinct().take(50).toSet().toIntArray()
        val adapter = NumberAdapter(this, ranInts)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = adapter
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