package com.example.myapplication2.ui.screen

// import androidx.compose.material3.OutlinedTextField // <-- HAPUS
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // <-- Cukup ini untuk semua komponen Material3
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
fun LoginScreen(
    modifier: Modifier = Modifier, // <--- TAMBAHKAN INI
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (user: com.example.myapplication2.model.User, token: String) -> Unit
) {
    val authRepo = remember { AuthRepository() }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Gunakan parameter modifier di sini agar padding dari Scaffold terbawa
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (kode lainnya tetap sama)
        Text(text = "Welcome Back", fontSize = 28.sp, modifier = Modifier.padding(bottom = 32.dp))

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
                // Jangan lupa lengkapi logika authRepo.login di sini
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    // Masukkan logika login Anda di sini
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(text = "Sign In")
            }
        }

        TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
            Text("Don't have an account? Sign Up here")
        }
    }
}