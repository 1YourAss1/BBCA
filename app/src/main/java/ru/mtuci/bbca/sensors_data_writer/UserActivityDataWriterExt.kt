package ru.mtuci.bbca.sensors_data_writer

import androidx.activity.ComponentActivity

fun ComponentActivity.userActivityDataWriter(
    currentSessionPath: String,
    directoryName: String,
    activityName: String,
    activityColumns: List<String>,
) = UserActivityDataWriter(
    context = this,
    lifecycle = lifecycle,
    currentSessionPath = currentSessionPath,
    directoryName = directoryName,
    activityName = activityName,
    activityColumns = activityColumns
)