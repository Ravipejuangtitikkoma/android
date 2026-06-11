package com.example.myapplication2

import android.adservices.ondevicepersonalization.InferenceInput
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Outline
import android.os.Bundle
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.MyApplication2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        // Membuat aplikasi memenuhi layar penuh (sampai ke area jam/baterai di atas)
        enableEdgeToEdge()

    val sharedPrefe: SharedPreferences = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)

        val statusSudahLogin= sharedPrefe.getBoolean("SUDAH_LOGIN", false)

        val namaTersimpan = sharedPrefe.getString("NAMA_USER","")?: ""
        val emailTersimpan= sharedPrefe.getString("EMAIL_USER","")?: ""

        setContent {
            MyApplication2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    LayarLogin(modifier = Modifier.padding(innerPadding))

                    var layaraktif by remember {
                        mutableStateOf(if(statusSudahLogin)"dashboard" else "login")  }

                    //memori tambahan
                    var namaPengguna by remember {mutableStateOf(namaTersimpan)}
                    var emailPengguna by remember {mutableStateOf(emailTersimpan)}


                    if(layaraktif == "login"){
                        LayarLogin(
                            modifier = Modifier.padding(innerPadding),
                            pindahKeRegister = {layaraktif = "register"},

//                            pindahKeDashboard = { nama,email ->
//                                namaPengguna = nama
//                                emailPengguna= email
//                                layaraktif= "dashboard"
//                            }
                            pindahKeDashboard = { nama,email, token ->
                                val editor= sharedPrefe.edit()
                                editor.putBoolean("SUDAH_LOGIN", true)
                                editor.putString("NAMA_USER",nama)
                                editor.putString("EMAIL_USER",email)
                                editor.putString("TOKEN_LOGIN",token)
                                editor.apply()

                                namaPengguna= nama
                                emailPengguna= email
                                layaraktif = "dashboard"// berpindah ke dashboard
                            }
                        )
                    }else if(layaraktif == "register"){
                        layarRegister(
                            modifier = Modifier.padding(innerPadding),
                            pindahKeLogin = {layaraktif = "login"}
                        )
                    }else if(layaraktif == "dashboard"){
                        LayarDashboard(modifier = Modifier.padding(innerPadding),
                            nama = namaPengguna,
                            email = emailPengguna,
                            aksiLogout = {
                                // Bersihkan laci SharedPreferences seketika
                                val editor= sharedPrefe.edit()
                                editor.clear()//Menghapus seluruh key (Nama, Email, Token, Status)
                                editor.apply()

                                //kosong semula
                                namaPengguna=""
                                emailPengguna=""
                                layaraktif ="login"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LayarLogin(modifier: Modifier= Modifier, pindahKeRegister: () -> Unit, pindahKeDashboard: (String, String, String) -> Unit){


    // kita membuat  2 variabel yang akan di ketikan dengan username dan password
    // dan mutableStateOf nilai awalnya variabel adalah kosong jadi pas awal aplikasi di buka kedua variabel ini kosong
    var email by remember { mutableStateOf("")}
    var password by remember{mutableStateOf("")}

    // ini untuk Pop up Toast
    val context= LocalContext.current

    //3. COROUTINE SCOPE (Untuk Tugas Berat)
    // Ini seperti menyewa asisten. Saat kita menghubungi server Laravel,
    //kita suruh asisten ini yang menunggu, agar layar HP tidak nge-freeze (macet).
    val  asisten= rememberCoroutineScope()

    // dan ini merupakan kolong yang akan di susun dari atas sampe bawah
    Column (
        modifier = modifier
            .fillMaxSize()// ini penuhi semua layar
            .padding(16.dp),// berikan jarak pinggir  16 dp
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Selamat Datang",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // dan ini merupakan kolom input email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it},
            label = {Text("Email")},
            modifier =  Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Kolom input password
        OutlinedTextField(
            value = password,
            label = {Text("Password")},
            onValueChange = {password= it},
            visualTransformation = PasswordVisualTransformation(),
            modifier= Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        //Sekarang button

        Button(
            onClick = {
                if(email.isNotBlank() && password.isNotBlank()){
                    Toast.makeText(context, "Memproses...", Toast.LENGTH_SHORT).show()

                    asisten.launch{

                        val respon = nativeApiLogin(email,password)// dan ini merupakan function dari nativeApiLogin
                        try {
                            val jsonResponse= JSONObject(respon )
                            if(jsonResponse.optBoolean("status",false)){
                                //ambil token langsung
                                val tokenAkses= jsonResponse.getString("access_token")
                                //Buka kotak "user" lalu ambil nama dan email
                                val dataUser=jsonResponse.getJSONObject("user")
                                val namaUser= dataUser.getString("name")
                                val emailUser = dataUser.getString("email")




                                Toast.makeText(context,"Anda berhasil Login", Toast.LENGTH_SHORT).show()
                                // 3. Kirim ketiga data ini ke MainActivity untuk disimpan di brankas
                                pindahKeDashboard(namaUser, emailUser, tokenAkses)
                            }else{
                                val errorMesage= jsonResponse.optString("message","login gagal")
                                Toast.makeText(context, errorMesage, Toast.LENGTH_SHORT).show()
                            }

                        }catch (e: Exception){
                            Toast.makeText(context, "Error server: $respon", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else {
                    Toast.makeText(context, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier= Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(text = "Masuk")
        }

        TextButton(onClick = {pindahKeRegister()}) {
            Text("Belum punya akun? Daftar di sini")
        }
    }

}
    // suspend = Fungsi ini butuh waktu lama, jadi harus bisa dijeda
    suspend fun nativeApiLogin(emailInput: String, passwordInput: String): String{
        return withContext(Dispatchers.IO){
            var connection: HttpURLConnection? = null
            try {
                //tentukan tujuan api yang kita buat
                val url = URL("http://10.0.2.2:8000/api/login")
                connection = url.openConnection() as HttpURLConnection

                // 2. Atur cara kirim (POST) dan bahasanya (JSON)
                connection.requestMethod= "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                // 3. siapkan paket json
                val JsonBody= JSONObject().apply {
                    put("email",emailInput)
                    put("password", passwordInput)
                }.toString()

                // 4. Kirim paketnya
                connection.outputStream.use { os ->
                    val input = JsonBody.toByteArray(Charsets.UTF_8)
                    os.write(input,0,input.size)
                }
                //5. tunggu balasan dari laravel
                val responCode=  connection.responseCode
                if(responCode == HttpURLConnection.HTTP_OK || responCode == 200){
                    connection.inputStream.bufferedReader().use { it.readText() }
                }else{
                    // jika errornya(contoh 401/500) baca pesan errornya
                    "Error: $responCode - "+ connection.errorStream.bufferedReader().use { it.readText() }
                }

            }catch (e: Exception){
                // Jika internet mati atau IP salah
                "Exception: ${e.message}"
            } finally {
                // Selalu tutup koneksi agar RAM HP tidak bocor
                connection?.disconnect()
            }
            }
    }

@Composable
fun layarRegister(modifier: Modifier= Modifier, pindahKeLogin: () -> Unit){

    // variabel
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context=LocalContext.current
    val asisten = rememberCoroutineScope ()

    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Daftar akun baru", fontSize = 28.sp, modifier= Modifier.fillMaxWidth())

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it},
            label = {Text("Nama Lengkap")},
            modifier= Modifier.fillMaxWidth()
        )

        Spacer(modifier= Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("Email")},
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = {Text("Password")},
            visualTransformation = PasswordVisualTransformation(),
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(32.dp))

        Button(
            onClick = {
                if(nama.isNotBlank() && email.isNotBlank() && password.isNotBlank()){
                    Toast.makeText(context,"Proses....", Toast.LENGTH_SHORT).show()

                        asisten.launch {
                            val respon = nativeApiRegister(nama, email, password)

                            try {
                                val jsonResponse = JSONObject(respon)
                                if(jsonResponse.optBoolean("status",false)){
                                    Toast.makeText(context," Daftar berhasil di buat " , Toast.LENGTH_SHORT).show()
                                    pindahKeLogin() // Otomatis pindah ke layar login jika sukses
                                }else{
                                    val errorMesage = jsonResponse.optString("message", "Gagal mendaftar")
                                    Toast.makeText(context, errorMesage, Toast.LENGTH_LONG).show()
                                }
                            }catch (x: Exception){
                                Toast.makeText(context, "Error server: $respon", Toast.LENGTH_SHORT).show()
                            }
                        }


                }else{
                    Toast.makeText(context,"Harap isi semua field", Toast.LENGTH_SHORT).show()
                }

            },
            modifier= Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Daftar sekarang ")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 5. Tombol Kembali ke Login
        TextButton(onClick = { pindahKeLogin() }) {
            Text("Sudah punya akun? Kembali ke Login")
        }
    }
}

suspend fun nativeApiRegister(namaInput: String, emailInput: String, passwordInput: String): String{
    return withContext(Dispatchers.IO){
        var connection: HttpURLConnection? = null
        try {
            // 1. URL mengarah ke /register mengunakan method Post
            val url= URL("http://10.0.2.2:8000/api/register")
            connection= url.openConnection() as HttpURLConnection
            connection.requestMethod="POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("accept", "application/json")
            connection.doOutput= true

            // 2. Masukkan namaInput ke dalam pake Body JSOn
            val jsonBody= JSONObject().apply {
                put("name", namaInput)
                put("email", emailInput)
                put("password", passwordInput)
            }.toString()

            connection.outputStream.use{
                os->val input= jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // 3. Tambahkan pengecekan responCode 201 (Created)
            val responCode= connection.responseCode
            if(responCode == HttpURLConnection.HTTP_OK || responCode == 200 || responCode == 201){
                connection.inputStream.bufferedReader().use { it.readText() }

            }else{
                "Error: $responCode - " + connection.errorStream.bufferedReader().use { it.readText() }
            }
        }catch (e: Exception){
            "Exception: ${e.message}"
        }finally {
            connection?.disconnect()
        }

    }
}


@Composable
fun LayarDashboard(modifier: Modifier= Modifier, nama: String, email: String, aksiLogout: () -> Unit){

    // --- STATE UNTUK RECYCLERVIEW (LAZYCOLUMN) ---
    var daftarPost by remember { mutableStateOf(emptyList<PostDemo>()) }
    var sedangLoading by remember { mutableStateOf(true) }

    // LaunchedEffect akan langsung berjalan 1 kali saat LayarDashboard terbuka
    LaunchedEffect(Unit) {
        daftarPost = dataGetBrowser() // Tembak API
        sedangLoading = false // Matikan tulisan loading
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1. BAGIAN PROFIL ATAS ---
        Image(
            painter = painterResource(id= R.drawable.ic_avatar),
            contentDescription = "Foto profil user",
            modifier= Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )
        Text(text = "Halo, $nama", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text(text = email, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        // --- 2. BAGIAN LAZYCOLUMN (PENGGANTI RECYCLERVIEW) ---
        Text(text = "Daftar Postingan dari Internet:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        if (sedangLoading) {
            // Tampilkan ini saat sedang menunggu data dari internet
            Text("Memuat data...", modifier = Modifier.weight(1f))
        } else {
            // Ini adalah bentuk modern dari RecyclerView di Jetpack Compose
            LazyColumn(
                modifier = Modifier.weight(1f) // .weight(1f) artinya kolom ini akan memakan sisa ruang kosong di tengah
            ) {
                items(daftarPost) { post ->
                    // Card (Kartu) untuk setiap item
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = post.body, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. BAGIAN TOMBOL LOGOUT ---
        Button(
            onClick = { aksiLogout() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Keluar (Logout)")
        }
    }
}

    // 1. Ini adalah kerangka data (Model) dari JSON Placeholder
    data class PostDemo(
        val id: Int,
        val title: String,
        val body: String
    )

    suspend fun dataGetBrowser(): List<PostDemo>{
        return withContext(Dispatchers.IO){
            val listHasil=  mutableListOf<PostDemo>()
            var connection: HttpURLConnection? =null
            try {
                var url= URL("https://jsonplaceholder.typicode.com/posts")
                connection= url.openConnection() as HttpURLConnection
                connection.requestMethod="GET"// kita menggunakan get untuk mengambil data

                if(connection.responseCode == 200 || connection.responseCode == HttpURLConnection.HTTP_OK){
                    val responBody= connection.inputStream.bufferedReader().use { it.readText() }

                    val jsonArray= JSONArray(responBody)

                    val batasData= if (jsonArray.length() > 10) 10 else jsonArray.length()

                    for (i in 0 until batasData){
                        val jsonObj= jsonArray.getJSONObject(i)

                        listHasil.add(
                            PostDemo(
                                id= jsonObj.getInt("id"),
                                title= jsonObj.getString("title"),
                                body= jsonObj.getString("body")
                            )
                        )
                    }


                }
            }catch (e: Exception){

            }finally {
                connection?.disconnect()
            }
            listHasil
        }
    }







