package com.cnn.mushroom
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cnn.mushroom.ui.screens.MainScreen
import com.cnn.mushroom.ui.screens.MushroomDetailScreen
import com.cnn.mushroom.ui.screens.SearchScreen
import com.cnn.mushroom.ui.screens.UserSettingScreen



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val myApplication = context.applicationContext as MyApplication


    NavHost(navController = navController, startDestination = "main_content") {

        composable("main_content") {
            MainScreen(
                modifier = Modifier.padding(16.dp),
                navController = navController
            )
        }
        composable("user_setting"){
            UserSettingScreen(modifier = Modifier.padding(16.dp))
        }

        composable("search_content") {
            SearchScreen(navController, modifier = Modifier.padding(16.dp))
        }

        composable(
            "search_content/{mushroomId}",
            arguments = listOf(navArgument("mushroomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val mushroomId = backStackEntry.arguments!!.getInt("mushroomId")
            MushroomDetailScreen(mushroomId = mushroomId)
        }

    }
}