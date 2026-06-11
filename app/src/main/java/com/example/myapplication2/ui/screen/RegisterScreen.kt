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
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, onNavigateToLogin: () -> Unit) {
    val authRepo = remember { AuthRepository() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Daftar Akun Baru", fontSize = 28.sp, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    coroutineScope.launch {
                        when (val result = authRepo.register(name, email, password)) {
                            is ApiResponse.Success -> {
                                Toast.makeText(context, "Daftar berhasil, silakan login", Toast.LENGTH_LONG).show()
                                onNavigateToLogin()
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
            else Text(text = "Daftar Sekarang")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) { Text("Sudah punya akun? Kembali ke Login") }
    }
}