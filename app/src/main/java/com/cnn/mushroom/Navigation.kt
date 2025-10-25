package com.cnn.mushroom
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cnn.mushroom.ui.screens.MainScreen
import com.cnn.mushroom.ui.screens.SearchScreen
import com.cnn.mushroom.ui.screens.UserSettingScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val myApplication = context.applicationContext as MyApplication

    NavHost(navController = navController, startDestination = "main_content") {

        composable("main_content") {
            MainScreen(
                modifier = Modifier.padding(16.dp),
            )
        }
        composable("user_setting"){
            UserSettingScreen(modifier = Modifier.padding(16.dp))
        }

        composable("search_content") {
            SearchScreen(modifier = Modifier.padding(16.dp))
        }

    }
}