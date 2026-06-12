package com.example.myapplication2.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.R
import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(modifier: Modifier = Modifier, user: User, token: String, onLogout: () -> Unit, onTambah:() -> Unit) {
    var apiResponse by remember { mutableStateOf<ApiResponse<List<Post>>>(ApiResponse.Loading) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- 1. DEKLARASI VARIABEL UNTUK EDIT ---
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    // Fungsi untuk mengambil data dari server (bisa dipanggil berulang kali)
    fun loadData() {
        coroutineScope.launch {
            apiResponse = ApiResponse.Loading
            apiResponse = ApiService.getPosts(token)
        }
    }

    // Panggil loadData saat layar pertama kali dibuka
    LaunchedEffect(token) {
        loadData()
    }
    Box(modifier= Modifier.fillMaxSize()){
        Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_avatar), contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, Color.Gray, CircleShape))
            Text(text = "Halo, ${user.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(text = user.email, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

            Text(text = "Daftar Postingan dari Internet:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            when (val response = apiResponse) {
                is ApiResponse.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()// Tampilan loading berputar
                    }
                }
                is ApiResponse.Success -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(response.data) { post ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {

                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    // --- BAGIAN KIRI: TEKS ---
                                    // Modifier.weight(1f) di sini berfungsi untuk mendorong tombol hapus ke pojok kanan
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = post.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = post.body, fontSize = 14.sp, color = Color.DarkGray)
                                    } // <-- PERHATIKAN: Kurung tutup Column ada di sini

                                    //tombol edit
                                    IconButton(
                                        onClick = {
                                            postToEdit= post
                                            editTitle= post.title
                                            editBody= post.body
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Post", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // --- BAGIAN KANAN: TOMBOL HAPUS ---
                                    // IconButton berada DI LUAR Column, tapi DI DALAM Row
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val deleteResult = ApiService.deletePost(token, post.id)

                                                when(deleteResult){
                                                    is ApiResponse.Success -> {
                                                        Toast.makeText(context, "Postingan dihapus", Toast.LENGTH_SHORT).show()
                                                        loadData()
                                                    }
                                                    is ApiResponse.Error -> {
                                                        Toast.makeText(context, deleteResult.message, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Post",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
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

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick =  onTambah,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {Text("Tambah Postingan") }

        }

        if(postToEdit != null){
            AlertDialog(
                onDismissRequest = {if (!isUpdating) postToEdit = null},
                title = {Text("Edit Postingan")},
                text = {
                    Column {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = {editTitle = it},
                            label ={ Text("Judul Postingan")},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdating

                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editBody,
                            onValueChange = { editBody = it },
                            label = { Text("Isi Postingan") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            enabled = !isUpdating,
                            maxLines = 5
                        )
                    }
                },

                confirmButton = {
                    Button(
                        onClick = {
                            if(editTitle.isBlank() || editBody.isBlank()){
                                Toast.makeText(context, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isUpdating = true
                            coroutineScope.launch{
                                //panggil update ApiService
                                val updateResult = ApiService.updatePost(token, postToEdit!!.id, editTitle, editBody)
                                isUpdating = false
                                when(updateResult){
                                    is ApiResponse.Success -> {
                                        Toast.makeText(context, "Postingan diperbarui", Toast.LENGTH_SHORT).show()
                                        postToEdit = null // Tutup pop up
                                        loadData() // Refresh layar
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, updateResult.message, Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }

                            }
                        },
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Simpan")
                    }

                },
                dismissButton = {
                    TextButton(onClick = {postToEdit = null }, enabled= !isUpdating) {
                        Text("Batal")
                    }
                }

            )
        }


    }

}