package com.example.myapplication2.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // <-- TAMBAHAN IMPORT COIL
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

    // --- 1. DEKLARASI VARIABEL UNTUK EDIT POST ---
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    // =========================================================
    // --- TAMBAHAN: VARIABEL UNTUK EDIT PROFIL & REAL-TIME ---
    // =========================================================
    fun getLatestUserData(): Triple<String, String, String?> {
        val prefs = context.getSharedPreferences("SesiPengguna", android.content.Context.MODE_PRIVATE)
        return Triple(
            prefs.getString("NAMA_USER", user.name) ?: user.name,
            prefs.getString("EMAIL_USER", user.email) ?: user.email,
            prefs.getString("PHOTO_USER", user.photoUrl)
        )
    }

    var currentData by remember { mutableStateOf(getLatestUserData()) }
    LaunchedEffect(Unit) { currentData = getLatestUserData() }

    var showEditProfile by remember { mutableStateOf(false) }
    var editProfileName by remember { mutableStateOf(user.name) }
    var editProfileEmail by remember { mutableStateOf(user.email) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUpdatingProfile by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }
    // =========================================================

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

            // =========================================================
            // --- UBAHAN: FOTO DAN NAMA MENGGUNAKAN DATA REAL-TIME ---
            // =========================================================
            AsyncImage(
                model = selectedImageUri ?: currentData.third ?: R.drawable.ic_avatar,
                contentDescription = "Foto Profil",
                modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(text = "Halo, ${currentData.first}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(text = currentData.second, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedButton(onClick = {
                editProfileName = currentData.first
                editProfileEmail = currentData.second
                showEditProfile = true
            }) {
                Text("Edit Profil & Foto")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // =========================================================

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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = post.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = post.body, fontSize = 14.sp, color = Color.DarkGray)
                                    }

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

        // =========================================================
        // --- TAMBAHAN: POP-UP EDIT PROFIL ---
        // =========================================================
        if (showEditProfile) {
            AlertDialog(
                onDismissRequest = { if (!isUpdatingProfile) showEditProfile = false },
                title = { Text("Edit Profil") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = selectedImageUri ?: currentData.third ?: R.drawable.ic_avatar,
                            contentDescription = "Preview",
                            modifier = Modifier.size(80.dp).clip(CircleShape).border(1.dp, Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Text("Pilih Foto dari Galeri")
                        }

                        OutlinedTextField(
                            value = editProfileName,
                            onValueChange = { editProfileName = it },
                            label = { Text("Nama Baru") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdatingProfile
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editProfileEmail,
                            onValueChange = { editProfileEmail = it },
                            label = { Text("Email Baru") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdatingProfile
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isUpdatingProfile = true
                            coroutineScope.launch {
                                var photoBytes: ByteArray? = null
                                var photoName: String? = null

                                // Kompresi gambar agar tidak ditolak Laravel
                                if (selectedImageUri != null) {
                                    val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                    inputStream?.close()
                                    if(bitmap != null){
                                        val outputStream = java.io.ByteArrayOutputStream()
                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, outputStream)
                                        photoBytes = outputStream.toByteArray()
                                        photoName = "profile_${System.currentTimeMillis()}.jpg"
                                    }
                                }

                                val result = ApiService.updateProfil(token, editProfileName, editProfileEmail, photoBytes, photoName)

                                isUpdatingProfile = false
                                when (result) {
                                    is ApiResponse.Success -> {
                                        val dataBaru = result.data

                                        // Simpan ke SharedPreferences
                                        val sharedPref = context.getSharedPreferences("SesiPengguna", android.content.Context.MODE_PRIVATE)
                                        sharedPref.edit().apply {
                                            putString("NAMA_USER", dataBaru.name)
                                            putString("EMAIL_USER", dataBaru.email)
                                            if (dataBaru.photoUrl != null) {
                                                putString("PHOTO_USER", dataBaru.photoUrl)
                                            }
                                            apply()
                                        }

                                        // Refresh layar seketika
                                        currentData = getLatestUserData()
                                        selectedImageUri = null
                                        showEditProfile = false

                                        Toast.makeText(context, "Profil Diperbarui Secara Real-Time!", Toast.LENGTH_LONG).show()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }
                            }
                        },
                        enabled = !isUpdatingProfile
                    ) {
                        if (isUpdatingProfile) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Simpan Profil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfile = false }, enabled = !isUpdatingProfile) {
                        Text("Batal")
                    }
                }
            )
        }
        // =========================================================

    }
}