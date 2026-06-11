package com.example.myapplication2.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.R
import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.repository.PostRepository
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier, // <--- TAMBAHKAN INI
    user: User,
    token: String,
    onLogout: () -> Unit
) {
    val postRepo = remember { PostRepository() }
    var apiResponse by remember { mutableStateOf<ApiResponse<List<Post>>>(ApiResponse.Loading) }

    // Fungsi untuk memuat data (bisa dipanggil saat inisialisasi atau retry)
    fun loadPosts() {
        // Gunakan coroutine scope untuk memanggil repo
    }

    LaunchedEffect(token) {
        apiResponse = ApiResponse.Loading
        apiResponse = postRepo.getPosts(token)
    }

    Column(
        modifier = modifier // <--- GUNAKAN MODIFIER DI SINI
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_avatar),
            contentDescription = "User profile image",
            modifier = Modifier
                .size(100.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.CircleShape)
        )
        Text(text = "Hello, ${user.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text(text = user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "Internet Posts:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        // UI State Handling
        when (val response = apiResponse) {
            is ApiResponse.Loading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApiResponse.Success -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(response.data) { post ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = post.body, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            is ApiResponse.Error -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${response.message}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text(text = "Log Out")
        }
    }
}