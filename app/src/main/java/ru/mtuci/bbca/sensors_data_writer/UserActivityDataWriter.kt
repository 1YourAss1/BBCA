package ru.mtuci.bbca.sensors_data_writer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import ru.mtuci.bbca.Sensors
import java.io.File
import java.io.FileOutputStream

class UserActivityDataWriter(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val currentSessionPath: String,
    private val directoryName: String,
    private val activityName: String,
    private val activityColumns: List<String>,
) : SensorEventListener, DefaultLifecycleObserver {

    private val resources = context.resources

    private val sessionActivityPath = currentSessionPath + File.separator + directoryName + File.separator
    private val sessionSensorsPath = sessionActivityPath + "sensors" + File.separator

    private var accFileOutput: FileOutputStream? = null
    private var gyroFileOutput: FileOutputStream? = null
    private var gravFileOutput: FileOutputStream? = null
    private var laccFileOutput: FileOutputStream? = null
    private var magnFileOutput: FileOutputStream? = null

    private var lightFileOutput: FileOutputStream? = null
    private var proxFileOutput: FileOutputStream? = null
    private var tempFileOutput: FileOutputStream? = null
    private var presFileOutput: FileOutputStream? = null
    private var humFileOutput: FileOutputStream? = null

    private var activityFileOutput: FileOutputStream? = null

    private var sensorManager: SensorManager? = null

    init {
        if (!File(sessionActivityPath).exists()) {
            File(sessionActivityPath).mkdir()
            File(sessionSensorsPath).mkdir()
            FileOutputStream("${sessionActivityPath}${File.separator}${activityName}.csv").apply { write("${activityColumns.joinToString(separator = ",")}\n".toByteArray()) }
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
        }

        lifecycle.addObserver(this)
    }

    fun writeActivity(data: List<Any>) {
        check(data.size == activityColumns.size)
        activityFileOutput?.write("${data.joinToString(separator = ",")}\n".toByteArray())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensors.ACC.type -> accFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.GYRO.type -> gyroFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.GRAV.type -> gravFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.LACC.type -> laccFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())
            Sensors.MAGN.type -> magnFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)},${event.values?.get(1)},${event.values?.get(2)}\n".toByteArray())

            Sensors.LIGHT.type -> lightFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.PROX.type -> proxFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.TEMP.type -> tempFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.PRES.type -> presFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
            Sensors.HUM.type -> humFileOutput?.write("${System.currentTimeMillis()},${resources.configuration.orientation},${event.values?.get(0)}\n".toByteArray())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager?.unregisterListener(this)

        accFileOutput?.close()
        gyroFileOutput?.close()
        gravFileOutput?.close()
        laccFileOutput?.close()
        magnFileOutput?.close()

        lightFileOutput?.close()
        proxFileOutput?.close()
        tempFileOutput?.close()
        presFileOutput?.close()
        humFileOutput?.close()

        activityFileOutput?.close()

        accFileOutput = null
        gyroFileOutput = null
        gravFileOutput = null
        laccFileOutput = null
        magnFileOutput = null

        lightFileOutput = null
        proxFileOutput = null
        tempFileOutput = null
        presFileOutput = null
        humFileOutput = null

        activityFileOutput = null

        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        sensorManager?.let { manager ->
            Sensors.values().forEach { sensor ->
                manager.registerListener(this, manager.getDefaultSensor(sensor.type), SensorManager.SENSOR_DELAY_NORMAL)
            }
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

        activityFileOutput = FileOutputStream("${sessionActivityPath}${File.separator}${activityName}.csv", true)
    }
}