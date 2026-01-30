package com.wongchoi500.babylog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wongchoi500.babylog.data.AppDatabase
import com.wongchoi500.babylog.data.LogRepository
import com.wongchoi500.babylog.ui.HomeScreen
import com.wongchoi500.babylog.ui.HomeViewModel
import com.wongchoi500.babylog.ui.theme.BabyLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LogRepository(database.logDao())
        val prefs = getSharedPreferences("babylog_prefs", Context.MODE_PRIVATE)

        setContent {
            BabyLogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val viewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.provideFactory(repository, prefs)
                    )

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}