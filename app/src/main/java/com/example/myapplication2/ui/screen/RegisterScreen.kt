package com.example.myapplication2.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*          // Import semua Material3 - cukup ini
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.repository.AuthRepository
import com.example.myapplication2.network.ApiResponse
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    val authRepo = remember { AuthRepository() }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create New Account", fontSize = 28.sp, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@onClick
                }
                isLoading = true
                coroutineScope.launch {
                    when (val result = authRepo.register(name, email, password)) {
                        is ApiResponse.Success -> {
                            Toast.makeText(context, result.data, Toast.LENGTH_LONG).show()
                            onNavigateToLogin() // Pindah ke login setelah sukses
                        }
                        is ApiResponse.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            isLoading = false
                        }
                        is ApiResponse.Loading -> {
                            // Loading state already handled above
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(text = "Sign Up Now")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
            Text("Already have an account? Back to Login")
        }
    }
}