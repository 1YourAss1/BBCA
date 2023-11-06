package ru.mtuci.bbca.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import ru.mtuci.bbca.KeyStrokeActivity
import ru.mtuci.bbca.R
import ru.mtuci.bbca.ScaleActivity
import ru.mtuci.bbca.Sensors
import ru.mtuci.bbca.app_logger.CrashLogger
import ru.mtuci.bbca.clicks.ClicksActivity
import ru.mtuci.bbca.long_click.LongClickActivity
import ru.mtuci.bbca.paint.PaintActivity
import ru.mtuci.bbca.scroll.ScrollActivity
import ru.mtuci.bbca.swipe.SwipeActivity
import ru.mtuci.bbca.video.VideoActivity
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
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

            writeDataTo(contentResolver.openOutputStream(uri))

            Toast.makeText(this, "Данные сохранены в указанный файл!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    private val buttonKeyStroke by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonKeyStroke)
    }

    private val buttonScroll by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonScroll)
    }

    private val buttonSwipe by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonSwipe)
    }

    private val buttonScale by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonScale)
    }

    private val buttonClicks by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonClicks)
    }

    private val buttonVideo by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonVideo)
    }

    private val buttonLongClicks by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonLongClicks)
    }

    private val buttonPaint by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.buttonPaint)
    }

    private val sessionCounter by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.sessionCounter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.getStringExtra(TASK_DONE_KEY)) {
                    KeyStrokeActivity.KEY_STROKE_TASK -> {
                        viewModel.setKeyStrokeTaskDone()
                    }
                    ScaleActivity.SCALE_TASK -> {
                        viewModel.setScaleTaskDone()
                    }
                    VideoActivity.VIDEO_TASK -> {
                        viewModel.setVideoTaskDone()
                    }
                    SwipeActivity.SWIPE_TASK -> {
                        viewModel.setSwipeTaskDone()
                    }
                    ScrollActivity.SCROLL_TASK -> {
                        viewModel.setScrollTaskDone()
                    }
                    PaintActivity.PAINT_TASK -> {
                        viewModel.setPaintTaskDone()
                    }
                    LongClickActivity.LONG_CLICKS_TASK -> {
                        viewModel.setLongClicksTaskDone()
                    }
                    ClicksActivity.CLICKS_TASK -> {
                        viewModel.setClicksTaskDone()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        setContentView(R.layout.activity_main)
        // Init sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (savedInstanceState == null) {
            viewModel.startNewSession(
                context = this,
                sensorManager = sensorManager,
                identifier = intent.getStringExtra("identifier") ?: "null"
            )
        }

        // Set up buttons
        findViewById<FloatingActionButton>(R.id.buttonDownloadData).setOnClickListener {
            saveArchivedData.launch("user_data.zip")
        }

        buttonKeyStroke.setOnClickListener {
            startActivity(Intent(this, KeyStrokeActivity::class.java))
        }

        buttonScroll.setOnClickListener {
            startActivity(Intent(this, ScrollActivity::class.java))
        }

        buttonSwipe.setOnClickListener{
            startActivity(Intent(this, SwipeActivity::class.java))
        }

        buttonScale.setOnClickListener{
            startActivity(Intent(this, ScaleActivity::class.java))
        }

        buttonClicks.setOnClickListener{
            startActivity(Intent(this, ClicksActivity::class.java))
        }

        buttonVideo.setOnClickListener{
            startActivity(Intent(this, VideoActivity::class.java))
        }

        buttonLongClicks.setOnClickListener{
            startActivity(Intent(this, LongClickActivity::class.java))
        }

        buttonPaint.setOnClickListener{
            startActivity(Intent(this, PaintActivity::class.java))
        }

        findViewById<Button>(R.id.buttonNewSession).setOnClickListener {
            viewModel.startNewSession(
                context = this,
                sensorManager = sensorManager,
                identifier = intent.getStringExtra("identifier") ?: "null"
            )

            Toast.makeText(this, R.string.new_session_success, Toast.LENGTH_LONG).show()
        }

        findViewById<FloatingActionButton>(R.id.buttonShareData).setOnClickListener {
            val cacheTempDir = File(cacheDir, "temp")
            cacheTempDir.mkdirs()
            val tempZipDataFile = File(cacheTempDir, "user_data.zip")
            val tempZipDataUri = FileProvider.getUriForFile(this, "ru.mtuci.bbca.provider", tempZipDataFile);

            writeDataTo(contentResolver.openOutputStream(tempZipDataUri))

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, tempZipDataUri)
            }

            startActivity(
                Intent.createChooser(intent,  null).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasksState.collect { state ->
                        buttonKeyStroke.setBackgroundColor(colorFromState(state.isKeyStrokeDone))
                        buttonScroll.setBackgroundColor(colorFromState(state.isScrollDone))
                        buttonSwipe.setBackgroundColor(colorFromState(state.isSwipeDone))
                        buttonScale.setBackgroundColor(colorFromState(state.isScaleDone))
                        buttonClicks.setBackgroundColor(colorFromState(state.isClicksDone))
                        buttonVideo.setBackgroundColor(colorFromState(state.isVideoDone))
                        buttonLongClicks.setBackgroundColor(colorFromState(state.isLongClicksDone))
                        buttonPaint.setBackgroundColor(colorFromState(state.isPaintDone))
                    }
                }

                launch {
                    viewModel.currentSessionNumber.collect { session ->
                        sessionCounter.text = getString(R.string.session_counter, session)
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            IntentFilter(TASK_DONE_KEY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    @ColorInt
    private fun colorFromState(isTaskDone: Boolean): Int {
        return if (isTaskDone) getColor(R.color.done) else getColor(R.color.alert)
    }

    private fun writeDataTo(outputStream: OutputStream?) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
            filesDir.walkTopDown()
                .asSequence()
                .filter { file -> file.absolutePath.contains("user_data") || file.endsWith(CrashLogger.FILE_NAME) }
                .forEach { file ->
                    val zipFileName = file.absolutePath.removePrefix(filesDir.absolutePath).removePrefix("/")
                    val entry = ZipEntry( "$zipFileName${(if (file.isDirectory) "/" else "" )}")
                    zos.putNextEntry(entry)
                    if (file.isFile) {
                        file.inputStream().use { fis -> fis.copyTo(zos) }
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

    companion object {
        const val TASK_DONE_KEY = "TASK_DONE_KEY"
    }
}