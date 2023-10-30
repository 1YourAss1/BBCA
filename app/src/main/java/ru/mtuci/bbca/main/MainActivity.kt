package ru.mtuci.bbca.main

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.mtuci.bbca.KeyStrokeActivity
import ru.mtuci.bbca.R
import ru.mtuci.bbca.ScaleActivity
import ru.mtuci.bbca.Sensors
import ru.mtuci.bbca.swipe.SwipeActivity
import ru.mtuci.bbca.clicks.ClicksActivity
import ru.mtuci.bbca.long_click.LongClickActivity
import ru.mtuci.bbca.paint.PaintActivity
import ru.mtuci.bbca.scroll.ScrollActivity
import ru.mtuci.bbca.video.VideoActivity
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var textViewTouch: TextView
    private lateinit var textViewAccelerometer: TextView
    private lateinit var textViewGyroscope: TextView
    private lateinit var textViewGravity: TextView
    private lateinit var textViewLinearAccelerometer: TextView
    private lateinit var textViewMagneticField: TextView
    private lateinit var textViewLight: TextView
    private lateinit var textViewProximity: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewPressure: TextView
    private lateinit var textViewHumidity: TextView
    private lateinit var sensorManager: SensorManager

    private val viewModel: MainViewModel by viewModels()

    private val saveArchivedData = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            if (File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}", "user_data.zip").exists()) File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}", "user_data.zip").delete()
            val inputDirectory = File("$filesDir/user_data")
            ZipOutputStream(BufferedOutputStream(contentResolver.openOutputStream(uri))).use { zos ->
                inputDirectory.walkTopDown().forEach { file ->
                    val zipFileName = file.absolutePath.removePrefix(inputDirectory.absolutePath).removePrefix("/")
                    val entry = ZipEntry( "$zipFileName${(if (file.isDirectory) "/" else "" )}")
                    zos.putNextEntry(entry)
                    if (file.isFile) {
                        file.inputStream().use { fis -> fis.copyTo(zos) }
                    }
                }
            }
            Toast.makeText(this, "Данные сохранены в указанный файл!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Init sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        viewModel.initializeSession(context = this, sensorManager = sensorManager)

        // Set up buttons
        findViewById<FloatingActionButton>(R.id.buttonDownloadData).setOnClickListener {
            saveArchivedData.launch("user_data.zip")
        }

        findViewById<Button>(R.id.buttonKeyStroke).setOnClickListener { startActivity(Intent(this, KeyStrokeActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonScroll).setOnClickListener { startActivity(Intent(this, ScrollActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonSwipe).setOnClickListener{ startActivity(Intent(this, SwipeActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonScale).setOnClickListener{ startActivity(Intent(this, ScaleActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonClicks).setOnClickListener{ startActivity(Intent(this, ClicksActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonVideo).setOnClickListener{ startActivity(Intent(this, VideoActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonLongClicks).setOnClickListener{ startActivity(Intent(this, LongClickActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        findViewById<Button>(R.id.buttonPaint).setOnClickListener{ startActivity(Intent(this, PaintActivity::class.java).putExtra("currentSessionPath", viewModel.currentSessionPath)) }
        // Set up textviews for debug
        textViewTouch = findViewById(R.id.textViewTouch)
        textViewAccelerometer = findViewById(R.id.textViewAccelerometer)
        textViewGyroscope = findViewById(R.id.textViewGyroscope)
        textViewGravity = findViewById(R.id.textViewGravity)
        textViewLinearAccelerometer = findViewById(R.id.textViewLinearAccelerometer)
        textViewMagneticField = findViewById(R.id.textViewMagneticField)
        textViewLight = findViewById(R.id.textViewLight)
        textViewProximity = findViewById(R.id.textViewProximity)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewPressure = findViewById(R.id.textViewPressure)
        textViewHumidity = findViewById(R.id.textViewHumidity)
    }

    override fun onResume() {
        super.onResume()
        // Start listening sensors when activity is active
        Sensors.values().forEach { sensor ->
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensor.type), SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Debug touch data
        when (event?.actionMasked) {
            MotionEvent.ACTION_MOVE -> textViewTouch.text =
                        "X: %.3f ".format(event?.x) +
                        "Y: %.3f ".format(event?.y) +
                        "P: %.3f".format(event?.pressure)
            MotionEvent.ACTION_UP -> textViewTouch.text = ""
        }
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Debug data from sensors
        when (event?.sensor?.type) {
            Sensors.ACC.type -> textViewAccelerometer.text = "${getString(R.string.accelerometer)}\n" +
                        "X: %.3f\n".format(event?.values?.get(0)) +
                        "Y: %.3f\n".format(event?.values?.get(1)) +
                        "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.GYRO.type -> textViewGyroscope.text = "${getString(R.string.gyroscope)}\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.GRAV.type -> textViewGravity.text = "${getString(R.string.gravity)}\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.LACC.type -> textViewLinearAccelerometer.text = "${getString(R.string.accelerometer_linear)}\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.MAGN.type -> textViewMagneticField.text = "${getString(R.string.magnetic_field)}\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))

            Sensors.LIGHT.type -> textViewLight.text = "${getString(R.string.light)}\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.PROX.type -> textViewProximity.text = "${getString(R.string.proximity)}\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.TEMP.type -> textViewTemperature.text = "${getString(R.string.temperature)}\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.PRES.type -> textViewPressure.text = "${getString(R.string.pressure)}\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.HUM.type -> textViewHumidity.text = "${getString(R.string.humidity)}\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {
        // TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        // Stop listening sensors when activity is inactive
        sensorManager.unregisterListener(this)
    }

}