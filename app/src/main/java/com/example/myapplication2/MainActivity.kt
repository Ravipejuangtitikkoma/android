package com.example.myapplication2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication2.model.User
import com.example.myapplication2.ui.screen.DashboardScreen
import com.example.myapplication2.ui.screen.LoginScreen
import com.example.myapplication2.ui.screen.RegisterScreen
import com.example.myapplication2.ui.screen.TambahPostScreen
import com.example.myapplication2.ui.theme.MyApplication2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication2Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sharedPrefe: SharedPreferences = context.getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
    val navController = rememberNavController()

    // Cek status login untuk menentukan halaman pertama
    val sudahLogin = sharedPrefe.getBoolean("SUDAH_LOGIN", false)
    val startDest = if (sudahLogin) "dashboard" else "login"

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { user, token ->
                        // Simpan ke SharedPreferences
                        sharedPrefe.edit().apply {
                            putBoolean("SUDAH_LOGIN", true)
                            putInt("ID_USER", user.id)
                            putString("NAMA_USER", user.name)
                            putString("EMAIL_USER", user.email)
                            putString("TOKEN_LOGIN", token)
                            apply()
                        }
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable("dashboard") {
                // Ambil data dari SharedPreferences
                val id = sharedPrefe.getInt("ID_USER", 0)
                val nama = sharedPrefe.getString("NAMA_USER", "") ?: ""
                val email = sharedPrefe.getString("EMAIL_USER", "") ?: ""
                val token = sharedPrefe.getString("TOKEN_LOGIN", "") ?: ""

                DashboardScreen(
                    user = User(id, nama, email),
                    token = token,
                    onLogout = {
                        // Hapus sesi
                        sharedPrefe.edit().clear().apply()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    },
                    onTambah = {
                        navController.navigate("tambah_post")
                    }
                )
            }

            composable ("tambah_post"){
                val token = sharedPrefe.getString("TOKEN_LOGIN", "") ?: ""

                TambahPostScreen(
                    token = token,
                    onNavigateBack = {
                        // Kembali ke halaman sebelumnya (Dashboard)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}