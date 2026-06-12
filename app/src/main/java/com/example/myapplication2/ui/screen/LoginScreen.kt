package com.example.myapplication2.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.model.User
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.network.ApiService // <-- Import ApiService langsung
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (User, String) -> Unit
) {
    // AuthRepository dihapus
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Selamat Datang", fontSize = 28.sp, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    coroutineScope.launch {
                        // Memanggil ApiService.login secara langsung
                        when (val result = ApiService.login(email, password)) {
                            is ApiResponse.Success -> {
                                Toast.makeText(context, "Berhasil Login", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(result.data.first, result.data.second)
                            }
                            is ApiResponse.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
                            else -> isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(text = "Masuk")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
            Text("Belum punya akun? Daftar di sini")
        }
    }
}