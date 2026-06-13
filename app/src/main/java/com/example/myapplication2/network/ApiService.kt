package com.example.myapplication2.network

import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ApiService {
    private const val BASE_URL = "http://192.168.0.105:8000/api"

    // WAJIB: withContext(Dispatchers.IO) agar tidak NetworkOnMainThreadException
    suspend fun login(email: String, password: String): ApiResponse<Pair<User, String>> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/login")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            connection.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)

                if (jsonResponse.optBoolean("status", false)) {
                    val token = jsonResponse.optString("access_token", "")
                    val dataUser = jsonResponse.optJSONObject("user")

                    if (token.isNotEmpty() && dataUser != null) {

                        //url photo dari photo_path laravel
                        val photoPath = dataUser.optString("photo_path","")
                        val baseUrlDomain= BASE_URL.replace("/api","")// Hilangkan /api dengan "" yang kosong
                        val finalPhotoUrl= if(photoPath.isNotEmpty() && photoPath != "null") "$baseUrlDomain/storage/$photoPath" else null


                        val user = User(
                            id = dataUser.optInt("id", 0),
                            name = dataUser.optString("name", "Unknown"),
                            email = dataUser.optString("email", "Unknown"),
                            photoUrl = finalPhotoUrl
                        )
                        return@withContext ApiResponse.Success(Pair(user, token))
                    }
                }
                return@withContext ApiResponse.Error(jsonResponse.optString("message", "Login gagal dari server"))
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                return@withContext ApiResponse.Error("Error $responseCode: $errorText")
            }
        } catch (e: Exception) {
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/register")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("name", name)
                put("email", email)
                put("password", password)
            }.toString()

            connection.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                if (jsonResponse.optBoolean("status", false)) {
                    return@withContext ApiResponse.Success("Pendaftaran berhasil!")
                } else {
                    return@withContext ApiResponse.Error(jsonResponse.optString("message", "Gagal mendaftar"))
                }
            } else {
                return@withContext ApiResponse.Error("Error server: $responseCode")
            }
        } catch (e: Exception) {
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun getPosts(token: String): ApiResponse<List<Post>> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/posts")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)

                val posts = mutableListOf<Post>()
                if (jsonResponse.has("data")) {
                    val jsonArray = jsonResponse.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val jsonObj = jsonArray.getJSONObject(i)
                        posts.add(
                            Post(
                                id = jsonObj.optInt("id", 0),
                                title = jsonObj.optString("title", "No Title"),
                                body = jsonObj.optString("body", "No Content")
                            )
                        )
                    }
                }
                return@withContext ApiResponse.Success(posts.toList())
            } else {
                return@withContext ApiResponse.Error("Gagal mengambil data: $responseCode")
            }
        } catch (e: Exception) {
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun createPost(token: String, title: String, body: String): ApiResponse<String> = withContext(Dispatchers.IO){
        var connection: HttpURLConnection? = null

        try {
            val url = URL("$BASE_URL/posts")
            connection= url.openConnection() as HttpURLConnection
            connection.requestMethod="POST"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("title",title)
                put("body", body)
            }.toString()

            connection.outputStream.use{os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
            os.write(input,0,input.size)
            }
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                return@withContext ApiResponse.Success("Postingan berhasil ditambahkan!")
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                return@withContext ApiResponse.Error("Gagal menambahkan: $responseCode")
            }
        }catch (e: Exception){
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun deletePost(token: String, postId: Int): ApiResponse<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/posts/$postId")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            // HTTP_OK (200) atau HTTP_NO_CONTENT (204) biasanya digunakan jika berhasil dihapus
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return@withContext ApiResponse.Success("Berhasil dihapus")
            } else {
                return@withContext ApiResponse.Error("Gagal menghapus: $responseCode")
            }
        } catch (e: Exception) {
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun updatePost(token: String, postId:Int, title: String, body: String): ApiResponse<String> = withContext(Dispatchers.IO){
        var connection: HttpURLConnection? = null
        try {
            var url= URL("$BASE_URL/posts/$postId")
            connection= url.openConnection() as HttpURLConnection
            connection.requestMethod="PUT"// gunakan method put untuk update data
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept","application/json")
            connection.doOutput = true


            // ini yang biasa kalau kita di postman untuk mengisinya kita mengunakan Tap body
            val jsonBody = JSONObject().apply {
                put("title",title)
                put("body", body)
            }.toString()

            connection.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responcode= connection.responseCode
            if(responcode == HttpURLConnection.HTTP_OK){
                return@withContext ApiResponse.Success("Postingan berhasil diperbarui!")

            }else{
                val erroText= connection.errorStream?.bufferedReader()?.use {it.readText()} ?: "Unknown error"
                return@withContext ApiResponse.Error("Gagal Memperbarui: $responcode - $erroText")
            }



        }catch (e: Exception){
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        }finally {
            connection?.disconnect()
        }
    }

    suspend fun updateProfil(token: String, name: String,email: String,photoBytes: ByteArray?,photoFileName: String?): ApiResponse<User> = withContext(Dispatchers.IO){
        var connection: HttpURLConnection? = null
        try {
            val url = URL("${BASE_URL}/profile/update")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod="POST"
            connection.setRequestProperty("Authorization","Bearer $token")
            connection.setRequestProperty("Accept", "application/json")


            // membuat id uni sebagai pembatasan antara data
            val idUnik= "Boundary-"+ System.currentTimeMillis()
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$idUnik")
            connection.doOutput = true

            val outputStream= DataOutputStream(connection.outputStream)

            //fungsi bantuna menulis nama dan email
            fun addTextPart(fieldName: String, value: String){
                outputStream.writeBytes("--$idUnik\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"\r\n\r\n")
                outputStream.write(value.toByteArray(Charsets.UTF_8))
                outputStream.writeBytes("\r\n")
            }

            addTextPart("name", name)
            addTextPart("email", email)

            // Logika untuk menyisipkan File Foto jika ada
            if (photoBytes != null && photoFileName != null) {
                outputStream.writeBytes("--$idUnik\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"$photoFileName\"\r\n")
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                outputStream.write(photoBytes)
                outputStream.writeBytes("\r\n")
            }

            // Penutup file multipart
            outputStream.writeBytes("--$idUnik--\r\n")
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)

                // Ambil data User terbaru dari Laravel
                val dataObj = jsonResponse.optJSONObject("data")
                val userObj = dataObj?.optJSONObject("user")
                val photoUrl = dataObj?.optString("photo_url", "")

                val updatedUser = User(
                    id = userObj?.optInt("id", 0) ?: 0,
                    name = userObj?.optString("name", "") ?: name,
                    email = userObj?.optString("email", "") ?: email,
                    photoUrl = if (photoUrl.isNullOrEmpty() || photoUrl == "null") null else photoUrl
                )
                // Kembalikan data User baru!
                return@withContext ApiResponse.Success(updatedUser)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                return@withContext ApiResponse.Error("Gagal update profil: $responseCode - $errorText")
            }
        }catch (e: Exception){
            return@withContext ApiResponse.Error("Koneksi gagal: ${e.message}")
        }finally {
            connection?.disconnect()
        }
    }

}
