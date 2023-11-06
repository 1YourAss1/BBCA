package ru.mtuci.bbca

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
    }
}