package com.wongchoi500.babylog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wongchoi500.babylog.data.AppDatabase
import com.wongchoi500.babylog.data.LogRepository
import com.wongchoi500.babylog.ui.BabyInfoScreen
import com.wongchoi500.babylog.ui.HomeScreen
import com.wongchoi500.babylog.ui.HomeViewModel
import com.wongchoi500.babylog.ui.WelcomeScreen
import com.wongchoi500.babylog.ui.theme.BabyLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LogRepository(database.logDao())
        val prefs = getSharedPreferences("babylog_prefs", Context.MODE_PRIVATE)

        setContent {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(repository, prefs)
            )

            var currentGender by remember { mutableStateOf(viewModel.babyGender) }

            BabyLogTheme(babyGender = currentGender) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val startDestination = if (viewModel.shouldShowWelcome) "welcome" else "home"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") {
                            WelcomeScreen(
                                onComplete = { nickname, birthday, gender ->
                                    viewModel.saveBabyInfo(nickname, birthday, gender)
                                    viewModel.markWelcomeCompleted()
                                    currentGender = gender
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onSkip = {
                                    viewModel.markWelcomeCompleted()
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToBabyInfo = {
                                    navController.navigate("baby_info")
                                }
                            )
                        }
                        composable("baby_info") {
                            BabyInfoScreen(
                                initialNickname = viewModel.babyNickname,
                                initialBirthday = viewModel.babyBirthday,
                                initialGender = viewModel.babyGender,
                                onSave = { nickname, birthday, gender ->
                                    viewModel.saveBabyInfo(nickname, birthday, gender)
                                    currentGender = gender
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
