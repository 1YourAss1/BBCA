package ru.mtuci.bbca

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import java.io.File
import java.io.FileOutputStream

class SwipeActivity : FragmentActivity(), SensorEventListener {
    private lateinit var currentSessionPath: String
    private lateinit var sessionSensorsPath: String
    private lateinit var textViewTouch: TextView
    private lateinit var swipeFileOutput: FileOutputStream
    private lateinit var sensorManager: SensorManager
    private lateinit var accFileOutput: FileOutputStream
    private lateinit var gyroFileOutput: FileOutputStream
    private lateinit var gravFileOutput: FileOutputStream
    private lateinit var laccFileOutput: FileOutputStream
    private lateinit var magnFileOutput: FileOutputStream
    private lateinit var lightFileOutput: FileOutputStream
    private lateinit var proxFileOutput: FileOutputStream
    private lateinit var tempFileOutput: FileOutputStream
    private lateinit var presFileOutput: FileOutputStream
    private lateinit var humFileOutput: FileOutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)
        textViewTouch = findViewById(R.id.textViewTouch)
        // Set path, create dir and create data file
        currentSessionPath = intent.getStringExtra("currentSessionPath").toString() + File.separator + "swipe" + File.separator
        File(currentSessionPath).mkdir()
        // Set path and create sensors dir
        sessionSensorsPath = currentSessionPath + "sensors" + File.separator
        File(sessionSensorsPath).mkdir()
        FileOutputStream(currentSessionPath + "swipe.csv").apply { write("timestamp,orientation,x_coordinate,y_coordinate,pressure,action\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.ACC.fileName).apply { write("timestamp,orientation,x_axis,y_axis,z_axis\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.GYRO.fileName).apply { write("timestamp,orientation,x_axis,y_axis,z_axis\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.GRAV.fileName).apply { write("timestamp,orientation,x_axis,y_axis,z_axis\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.LACC.fileName).apply { write("timestamp,orientation,x_axis,y_axis,z_axis\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.MAGN.fileName).apply { write("timestamp,orientation,x_axis,y_axis,z_axis\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.LIGHT.fileName).apply { write("timestamp,orientation,sensor_data\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.PROX.fileName).apply { write("timestamp,orientation,sensor_data\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.TEMP.fileName).apply { write("timestamp,orientation,sensor_data\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.PRES.fileName).apply { write("timestamp,orientation,sensor_data\n".toByteArray()) }
        FileOutputStream(sessionSensorsPath + Sensors.HUM.fileName).apply { write("timestamp,orientation,sensor_data\n".toByteArray()) }

        val ranInts = generateSequence { (0..941).random() }.distinct().take(50).toSet().toIntArray()
        val adapter = NumberAdapter(this, ranInts)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = adapter
        viewPager.getChildAt(0).setOnTouchListener { v, event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> swipeFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                MotionEvent.ACTION_MOVE -> {
                    swipeFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                    textViewTouch.text ="X: %.3f ".format(event?.x) + "Y: %.3f ".format(event?.y) + "P: %.3f".format(event?.pressure)
                }
                MotionEvent.ACTION_UP -> {
                    swipeFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                    textViewTouch.text = ""
                }
            }
            v?.performClick() ?: true
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        // Start listening sensors when activity is active
        Sensors.values().forEach { sensor ->
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensor.type), SensorManager.SENSOR_DELAY_NORMAL)
        }
        accFileOutput = FileOutputStream(sessionSensorsPath + Sensors.ACC.fileName, true)
        gyroFileOutput = FileOutputStream(sessionSensorsPath + Sensors.GYRO.fileName, true)
        gravFileOutput = FileOutputStream(sessionSensorsPath + Sensors.GRAV.fileName, true)
        laccFileOutput = FileOutputStream(sessionSensorsPath + Sensors.LACC.fileName, true)
        magnFileOutput = FileOutputStream(sessionSensorsPath + Sensors.MAGN.fileName, true)

        lightFileOutput = FileOutputStream(sessionSensorsPath + Sensors.LIGHT.fileName, true)
        proxFileOutput = FileOutputStream(sessionSensorsPath + Sensors.PROX.fileName, true)
        tempFileOutput = FileOutputStream(sessionSensorsPath + Sensors.TEMP.fileName, true)
        presFileOutput = FileOutputStream(sessionSensorsPath + Sensors.PRES.fileName, true)
        humFileOutput = FileOutputStream(sessionSensorsPath + Sensors.HUM.fileName, true)

        swipeFileOutput = FileOutputStream(currentSessionPath + "swipe.csv", true)
    }

    override fun onPause() {
        super.onPause()
        // Stop listening sensors when activity is inactive
        sensorManager.unregisterListener(this)

        accFileOutput.close()
        gyroFileOutput.close()
        gravFileOutput.close()
        laccFileOutput.close()
        magnFileOutput.close()

        lightFileOutput.close()
        proxFileOutput.close()
        tempFileOutput.close()
        presFileOutput.close()
        humFileOutput.close()

        swipeFileOutput.close()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Write data from sensors to csv
        when (event?.sensor?.type) {
            Sensors.ACC.type -> accFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.GYRO.type -> gyroFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.GRAV.type -> gravFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.LACC.type -> laccFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.MAGN.type -> magnFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())

            Sensors.LIGHT.type -> lightFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.PROX.type -> proxFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.TEMP.type -> tempFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.PRES.type -> presFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.HUM.type -> humFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("Not yet implemented")
    }

}