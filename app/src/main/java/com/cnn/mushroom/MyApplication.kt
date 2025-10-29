package com.cnn.mushroom

import android.app.Application
import com.cnn.mushroom.data.AppDatabase
import com.cnn.mushroom.data.DefaultMushroomRepository
import com.cnn.mushroom.data.MushroomRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository: MushroomRepository by lazy {
        DefaultMushroomRepository(database.mushroomDao())
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }


}
