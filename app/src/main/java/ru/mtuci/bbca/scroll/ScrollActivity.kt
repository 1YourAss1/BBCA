package ru.mtuci.bbca.scroll

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import ru.mtuci.bbca.Sensors
import java.io.File
import java.io.FileOutputStream


class ScrollActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var currentSessionPath: String
    private lateinit var sessionSensorsPath: String
    private lateinit var textViewTouch: TextView
    private lateinit var textViewToRead: TextView
    private lateinit var sensorManager: SensorManager
    private lateinit var scrollFileOutput: FileOutputStream
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

    private val viewModel: ScrollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scroll)
        textViewTouch = findViewById(R.id.textViewTouch)
        // Set path, create dir and create data file
        currentSessionPath = intent.getStringExtra("currentSessionPath").toString() + File.separator + "scroll" + File.separator
        File(currentSessionPath).mkdir()
        // Set path and create sensors dir
        sessionSensorsPath = currentSessionPath + "sensors" + File.separator
        File(sessionSensorsPath).mkdir()
        FileOutputStream(currentSessionPath + "scroll.csv").apply { write("timestamp,orientation,x_coordinate,y_coordinate,pressure,action\n".toByteArray()) }
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

        textViewToRead = findViewById(R.id.textViewToRead)
        textViewToRead.movementMethod = ScrollingMovementMethod()
        textViewToRead.setOnTouchListener { v, event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> scrollFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                MotionEvent.ACTION_MOVE -> {
                    scrollFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                    textViewTouch.text ="X: %.3f ".format(event?.x) + "Y: %.3f ".format(event?.y) + "P: %.3f".format(event?.pressure)
                }
                MotionEvent.ACTION_UP -> {
                    scrollFileOutput.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.x},${event.y},${event.pressure},${event.action}\n".toByteArray())
                    textViewTouch.text = ""
                }

            }
            v?.performClick() ?: true
        }

        textViewToRead.setOnScrollChangeListener { _, _, _, _, _ ->
            viewModel.onScroll(isScrolledToBottom = !textViewToRead.canScrollVertically(1))
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskDoneSideEffect.collect {
                    Toast.makeText(this@ScrollActivity, R.string.task_successfully_done, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
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

        scrollFileOutput = FileOutputStream(currentSessionPath + "scroll.csv", true)
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

        scrollFileOutput.close()
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