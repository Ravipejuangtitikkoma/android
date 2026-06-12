package com.example.myapplication2.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.network.ApiService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun TambahPostScreen(
    modifier : Modifier= Modifier,
    token: String,
    onNavigateBack: () -> Unit
){
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isloading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val corutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text="Buat Postingan Baru", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {title = it},
            label = {Text("Judul Postingan")},
            modifier = Modifier.fillMaxWidth(),
            enabled = !isloading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value= body,
            onValueChange = {body = it},
            label = {Text("Isi Postingan ")},
            modifier = Modifier.fillMaxWidth().height(150.dp),
            enabled = !isloading,
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(

            onClick = {
                if(title.isBlank() || body.isBlank()){
                    Toast.makeText(context,"Judul dan isi harus diisi", Toast.LENGTH_SHORT).show()
                }else{
                    isloading = true
                    corutineScope.launch{
                        when(val result= ApiService.createPost(token,title,body)){
                            is ApiResponse.Success -> {
                                Toast.makeText(context,"Postingan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                onNavigateBack()// Otomatis kembali ke dashboard setelah sukses

                            }
                            is ApiResponse.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                isloading = false
                            }else -> isloading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isloading
        ){
          if (isloading) CircularProgressIndicator(color= MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Simpangan Postingan ")


        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack, enabled = !isloading) {
            Text("Batal dan Kembali")
        }


    }
}