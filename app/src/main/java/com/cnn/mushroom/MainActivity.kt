package com.cnn.mushroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainNavigation()
            }
        }
    }

}

