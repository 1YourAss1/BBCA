package ru.mtuci.bbca

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class ScaleActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var currentSessionPath: String
    private lateinit var sessionSensorsPath: String
    private lateinit var imageView: ImageView
    private lateinit var sensorManager: SensorManager
    private lateinit var scaleFileOutput: FileOutputStream
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
        setContentView(R.layout.activity_scale)
        imageView = findViewById(R.id.zoomageView)
        // Set path, create dir and create data file
        currentSessionPath = intent.getStringExtra("currentSessionPath")
            .toString() + File.separator + "scale" + File.separator
        File(currentSessionPath).mkdir()
        // Set path and create sensors dir
        sessionSensorsPath = currentSessionPath + "sensors" + File.separator
        File(sessionSensorsPath).mkdir()
        FileOutputStream(currentSessionPath + "scale.csv").apply { write("timestamp,orientation,x1_coordinate,y1_coordinate,pressure1,x2_coordinate,y2_coordinate,pressure2,action_type\n".toByteArray()) }
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

        val characterList: ArrayList<String> = arrayListOf("Волдо", "Бэтмен")
        var currentCharacter = 0
        var taskDone = false
        findViewById<TextView>(R.id.textViewFindCharacter).apply {
            text =
                "${resources.getText(R.string.find_character)}: ${characterList[0]} ($currentCharacter/${characterList.size})"
            setOnClickListener {
                if (currentCharacter.inc() == characterList.size) taskDone = true
                currentCharacter = currentCharacter.inc().mod(characterList.size)
                text =
                    "${resources.getText(R.string.find_character)}: ${characterList[currentCharacter]} (${if (!taskDone) currentCharacter else characterList.size}/${characterList.size})"
            }
        }

        imageView.setOnTouchListener { v, event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> scaleFileOutput.write(
                    ("${System.currentTimeMillis()},${resources.configuration.orientation}," +
                            "${event.getX(0)},${event.getY(0)},${event.getPressure(0)}," +
                            "${event.getX(1)},${event.getY(1)},${event.getPressure(1)},0\n").toByteArray()
                )

                MotionEvent.ACTION_MOVE -> if (event.pointerCount == 2) {
                    scaleFileOutput.write(
                        ("${System.currentTimeMillis()},${resources.configuration.orientation}," +
                                "${event.getX(0)},${event.getY(0)},${event.getPressure(0)}," +
                                "${event.getX(1)},${event.getY(1)},${event.getPressure(1)},2\n").toByteArray()
                    )
                }

                MotionEvent.ACTION_POINTER_UP -> scaleFileOutput.write(
                    ("${System.currentTimeMillis()},${resources.configuration.orientation}," +
                            "${event.getX(0)},${event.getY(0)},${event.getPressure(0)}," +
                            "${event.getX(1)},${event.getY(1)},${event.getPressure(1)},1\n").toByteArray()
                )

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

        scaleFileOutput = FileOutputStream(currentSessionPath + "scale.csv", true)
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

        scaleFileOutput.close()
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