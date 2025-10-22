package com.cnn.mushroom

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import com.cnn.mushroom.data.AppDatabase
import com.cnn.mushroom.data.DefaultMushroomRepository
import com.cnn.mushroom.data.MushroomRepository

class MyApplication : Application(), DefaultLifecycleObserver {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val repository: MushroomRepository by lazy {
        DefaultMushroomRepository(database.mushroomDao())
    }
    override fun onCreate() {
        super<Application>.onCreate()
    }
}