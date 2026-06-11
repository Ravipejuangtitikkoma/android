package com.example.myapplication2.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.R
import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.repository.PostRepository

@Composable
fun DashboardScreen(modifier: Modifier = Modifier, user: User, token: String, onLogout: () -> Unit) {
    val postRepo = remember { PostRepository() }
    var apiResponse by remember { mutableStateOf<ApiResponse<List<Post>>>(ApiResponse.Loading) }

    LaunchedEffect(token) {
        apiResponse = postRepo.getPosts(token)
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(id = R.drawable.ic_avatar), contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, Color.Gray, CircleShape))
        Text(text = "Halo, ${user.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text(text = user.email, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        Text(text = "Daftar Postingan dari Internet:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        when (val response = apiResponse) {
            is ApiResponse.Loading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Memuat data...")
                }
            }
            is ApiResponse.Success -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(response.data) { post ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = post.body, fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
            is ApiResponse.Error -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "Gagal memuat: ${response.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Keluar (Logout)") }
    }
}