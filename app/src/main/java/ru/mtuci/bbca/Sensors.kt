package ru.mtuci.bbca

import android.hardware.Sensor

enum class Sensors(var type: Int, var fileName: String) {
    ACC(Sensor.TYPE_ACCELEROMETER, "sensor_acc.csv"),
    GYRO(Sensor.TYPE_GYROSCOPE, "sensor_gyro.csv"),
    GRAV(Sensor.TYPE_GRAVITY, "sensor_grav.csv"),
    LACC(Sensor.TYPE_LINEAR_ACCELERATION, "sensor_lacc.csv"),
    MAGN(Sensor.TYPE_MAGNETIC_FIELD, "sensor_magn.csv"),

    LIGHT(Sensor.TYPE_LIGHT, "sensor_light.csv"),
    PROX(Sensor.TYPE_PROXIMITY, "sensor_prox.csv"),
    TEMP(Sensor.TYPE_AMBIENT_TEMPERATURE, "sensor_temp.csv"),
    PRES(Sensor.TYPE_PRESSURE, "sensor_pres.csv"),
    HUM(Sensor.TYPE_RELATIVE_HUMIDITY, "sensor_hum.csv")

}