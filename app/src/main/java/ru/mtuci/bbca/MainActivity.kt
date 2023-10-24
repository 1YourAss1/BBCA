package ru.mtuci.bbca

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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Init sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var currentSessionPath = "$filesDir/user_data"
        File(currentSessionPath).mkdirs()
        // Create info data
        val jsonInfoFile = File(currentSessionPath + File.separator + "info.json")
        if (!jsonInfoFile.exists()) {
            val jsonInfo = JSONObject()
            jsonInfo.put("screen", JSONObject().apply {
                put("width", resources.displayMetrics.widthPixels)
                put("height", resources.displayMetrics.heightPixels)
            })
            jsonInfo.put("device", JSONObject().apply {
                put("android_version", android.os.Build.VERSION.SDK_INT)
                put("device", android.os.Build.DEVICE)
                put("model", android.os.Build.MODEL)
                put("brand", android.os.Build.BRAND)
                put("manufacturer", android.os.Build.MANUFACTURER)
            })
            jsonInfo.put("sensors", JSONArray(sensorManager.getSensorList(Sensor.TYPE_ALL).map { sensor -> JSONObject("{\"name\": \"${sensor.name}\", \"vendor\": \"${sensor.vendor}\"}") }))
            FileOutputStream(jsonInfoFile).write(jsonInfo.toString().toByteArray())
        }
        // Create dir for new session
        if (File(currentSessionPath).listFiles()?.isNotEmpty() == true && File(currentSessionPath).listFiles().any { it.name.contains("session") }) {
            val newSession = "session${
                File(currentSessionPath).listFiles()?.last { it.name.contains("session") }?.name?.filter { it.isDigit() }?.toInt()?.inc()}"
            if (File("$currentSessionPath/$newSession").mkdirs()) currentSessionPath = "$currentSessionPath/$newSession"
        } else {
            if (File("$currentSessionPath/session1").mkdirs()) currentSessionPath = "$currentSessionPath/session1"
        }
        // Set up buttons
        findViewById<FloatingActionButton>(R.id.buttonDownloadData).setOnClickListener {
            if (File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}", "user_data.zip").exists()) File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}", "user_data.zip").delete()
            val inputDirectory = File("$filesDir/user_data")
            val outputZipFile = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}", "user_data.zip")
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zos ->
                inputDirectory.walkTopDown().forEach { file ->
                    val zipFileName = file.absolutePath.removePrefix(inputDirectory.absolutePath).removePrefix("/")
                    val entry = ZipEntry( "$zipFileName${(if (file.isDirectory) "/" else "" )}")
                    zos.putNextEntry(entry)
                    if (file.isFile) {
                        file.inputStream().use { fis -> fis.copyTo(zos) }
                    }
                }
            }
            Toast.makeText(this, "Данные сохранены в ${outputZipFile.path}", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.buttonKeyStroke).setOnClickListener { startActivity(Intent(this, KeyStrokeActivity::class.java).putExtra("currentSessionPath", currentSessionPath)) }
        findViewById<Button>(R.id.buttonScroll).setOnClickListener { startActivity(Intent(this, ScrollActivity::class.java).putExtra("currentSessionPath", currentSessionPath)) }
        findViewById<Button>(R.id.buttonSwipe).setOnClickListener{ startActivity(Intent(this, SwipeActivity::class.java).putExtra("currentSessionPath", currentSessionPath)) }
        findViewById<Button>(R.id.buttonScale).setOnClickListener{ startActivity(Intent(this, ScaleActivity::class.java).putExtra("currentSessionPath", currentSessionPath)) }
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
            Sensors.ACC.type -> textViewAccelerometer.text = "Acc\n" +
                        "X: %.3f\n".format(event?.values?.get(0)) +
                        "Y: %.3f\n".format(event?.values?.get(1)) +
                        "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.GYRO.type -> textViewGyroscope.text = "Gyro\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.GRAV.type -> textViewGravity.text = "Grav\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.LACC.type -> textViewLinearAccelerometer.text = "Lacc\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))
            Sensors.MAGN.type -> textViewMagneticField.text = "Magn\n" +
                    "X: %.3f\n".format(event?.values?.get(0)) +
                    "Y: %.3f\n".format(event?.values?.get(1)) +
                    "Z: %.3f\n".format(event?.values?.get(2))

            Sensors.LIGHT.type -> textViewLight.text = "Light\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.PROX.type -> textViewProximity.text = "Prox\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.TEMP.type -> textViewTemperature.text = "Temp\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.PRES.type -> textViewPressure.text = "Pres\n" +
                    "val: %.3f\n".format(event?.values?.get(0))
            Sensors.HUM.type -> textViewHumidity.text = "Hum\n" +
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