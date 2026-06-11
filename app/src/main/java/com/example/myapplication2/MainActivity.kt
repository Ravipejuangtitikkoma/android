package com.example.myapplication2

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpPaddingValues
import android.content.Context
import androidx.compose.ui.Modifier
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myapplication2.model.User
import com.example.myapplication2.ui.screen.DashboardScreen
import com.example.myapplication2.ui.screen.LoginScreen
import com.example.myapplication2.ui.screen.RegisterScreen
import com.example.myapplication2.ui.theme.MyApplication2Theme

class MainActivity : ComponentActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)

        setContent {
            MyApplication2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(padding = innerPadding)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun AppNavigation(padding: androidx.compose.ui.unit.DpPaddingValues) {
        val isLoggedIn = sharedPref.getBoolean("SUDAH_LOGIN", false)
        val savedName = sharedPref.getString("NAMA_USER", "") ?: ""
        val savedEmail = sharedPref.getString("EMAIL_USER", "") ?: ""
        val savedToken = sharedPref.getString("TOKEN_LOGIN", "") ?: ""

        var currentScreen by remember { mutableStateOf(if (isLoggedIn) "dashboard" else "login") }
        var currentUser by remember { mutableStateOf(User(0, savedName, savedEmail)) } // Inisialisasi User kosong jika tidak login
        var currentToken by remember { mutableStateOf(savedToken) }

        when (currentScreen) {
            "login" -> {
                LoginScreen(
                    modifier = Modifier.padding(padding),
                    onNavigateToRegister = { currentScreen = "register" },
                    onLoginSuccess = { user, token ->
                        saveLoginData(user, token)
                        currentUser = user
                        currentToken = token
                        currentScreen = "dashboard"
                    }
                )
            }
            "register" -> {
                RegisterScreen(
                    modifier = Modifier.padding(padding),
                    onNavigateToLogin = { currentScreen = "login" }
                )
            }
            "dashboard" -> {
                DashboardScreen(
                    modifier = Modifier.padding(padding),
                    user = currentUser,
                    token = currentToken,
                    onLogout = {
                        clearLoginData()
                        currentUser = User(0, "", "") // Reset User
                        currentToken = "" // Reset Token
                        currentScreen = "login"
                    }
                )
            }
        }
    }

    private fun saveLoginData(user: User, token: String) {
        with(sharedPref.edit()) {
            putBoolean("SUDAH_LOGIN", true)
            putString("NAMA_USER", user.name)
            putString("EMAIL_USER", user.email)
            putString("TOKEN_LOGIN", token)
            apply()
        }
    }

    private fun clearLoginData() {
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}