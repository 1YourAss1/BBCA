package ru.mtuci.bbca.app_logger

import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import ru.mtuci.bbca.R
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Calendar


class CrashLogger(private val activity: Activity) : Thread.UncaughtExceptionHandler {

    private val newLine = "\n"
    private val errorMessage = StringBuilder()
    private val softwareInfo = StringBuilder()
    private val dateInfo = StringBuilder()
    private val logFile = File("${activity.filesDir}/$FILE_NAME")
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))

        errorMessage.append(stackTrace.toString())

        softwareInfo.append("SDK: ")
        softwareInfo.append(Build.VERSION.SDK_INT)
        softwareInfo.append(newLine)
        softwareInfo.append("Release: ")
        softwareInfo.append(Build.VERSION.RELEASE)
        softwareInfo.append(newLine)
        softwareInfo.append("Incremental: ")
        softwareInfo.append(Build.VERSION.INCREMENTAL)
        softwareInfo.append(newLine)

        dateInfo.append(Calendar.getInstance().time)
        dateInfo.append(newLine)

        Log.d("Error" , errorMessage.toString())
        Log.d("Software" , softwareInfo.toString())
        Log.d("Date" , dateInfo.toString())

        logFile.appendText("\n-----------------------\n")
        logFile.appendText("Error: $errorMessage")
        logFile.appendText("Software: $softwareInfo")
        logFile.appendText("Date: $dateInfo")
        logFile.appendText("-----------------------")

        Toast.makeText(activity.applicationContext, R.string.error, Toast.LENGTH_LONG).show()

        activity.finishAffinity()

        defaultHandler?.uncaughtException(thread, exception)
    }

    companion object {
        const val FILE_NAME = "logs.txt"
    }
}