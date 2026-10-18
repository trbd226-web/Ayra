package com.example

import android.app.Application
import com.example.data.db.AyraDatabase
import com.example.data.preferences.AyraPreferences

class AyraApplication : Application() {

    lateinit var database: AyraDatabase
        private set

    lateinit var preferences: AyraPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        database = AyraDatabase.getDatabase(this)
        preferences = AyraPreferences(this)
    }
}
