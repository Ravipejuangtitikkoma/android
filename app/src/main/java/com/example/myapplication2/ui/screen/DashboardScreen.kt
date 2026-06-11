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
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.repository.PostRepository
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip

@Composable
fun DashboardScreen(
    user: com.example.myapplication2.model.User, // Terima User model
    token: String, // Terima Token
    onLogout: () -> Unit
) {
    val postRepo = remember { PostRepository() }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var apiResponse by remember { mutableStateOf<ApiResponse<List<Post>>>(ApiResponse.Loading) } // Gunakan ApiResponse untuk state
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(token) { // Ambil data saat token tersedia
        apiResponse = ApiResponse.Loading
        when (val result = postRepo.getPosts(token)) {
            is ApiResponse.Success -> {
                posts = result.data
                apiResponse = result
            }
            is ApiResponse.Error -> {
                apiResponse = result
            }
            is ApiResponse.Loading -> {
                // Ditangani oleh UI
            }
        }
    }

    Column(
        modifier = Modifier
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
        Text(
            text = "Hello, ${user.name}", // Gunakan nama dari User model
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = user.email, // Gunakan email dari User model
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Internet Posts:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (apiResponse) {
            is ApiResponse.Loading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApiResponse.Success -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = post.body, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
            is ApiResponse.Error -> {
                // Karena apiResponse adalah MutableState<ApiResponse>, dan kita berada dalam blok 'is ApiResponse.Error',
                // kita bisa casting nilai state tersebut ke ApiResponse.Error secara eksplisit.
                val errorMessage = (apiResponse as ApiResponse.Error).message
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Failed to load posts.")
                        Text(text = errorMessage, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            coroutineScope.launch {
                                when (val result = postRepo.getPosts(token)) {
                                    is ApiResponse.Success -> {
                                        posts = result.data
                                        apiResponse = result
                                    }
                                    is ApiResponse.Error -> {
                                        apiResponse = result
                                    }
                                    is ApiResponse.Loading -> {}
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Log Out")
        }
    }
}