package com.cnn.mushroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme

import com.cnn.mushroom.ui.theme.CNNTheme
import dagger.hilt.android.AndroidEntryPoint

import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.MaterialTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val systemUiController = rememberSystemUiController()

            CNNTheme(darkTheme = darkTheme) {
                val backgroundColor = MaterialTheme.colorScheme.background

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = backgroundColor,
                        darkIcons = !darkTheme
                    )
                }

                MainNavigation()
            }
        }

    }
}

