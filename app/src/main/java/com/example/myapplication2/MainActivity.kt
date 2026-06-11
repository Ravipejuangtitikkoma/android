package com.example.myapplication2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable // Pastikan import ini benar
import androidx.navigation.compose.rememberNavController
import com.example.myapplication2.ui.screen.DashboardScreen
import com.example.myapplication2.ui.screen.LoginScreen
import com.example.myapplication2.ui.screen.RegisterScreen
import com.example.myapplication2.ui.theme.MyApplication2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplication2Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            // PERBAIKAN: Hapus tanda @ sebelum composable
            composable("login") {
                LoginScreen(
                    modifier = Modifier,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { user, token ->
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    modifier = Modifier,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    modifier = Modifier,
                    // Isi dengan 3 parameter yang diminta: id (Int), name (String), email (String)
                    user = com.example.myapplication2.model.User(
                        id = 1,
                        name = "Default",
                        email = "user@example.com"
                    ),
                    token = "",
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}