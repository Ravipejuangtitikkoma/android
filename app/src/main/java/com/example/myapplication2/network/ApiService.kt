package com.example.myapplication2.network

import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiService {
    private const val BASE_URL = "http://10.0.2.2:8000/api"

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
                        val user = User(
                            id = dataUser.optInt("id", 0),
                            name = dataUser.optString("name", "Unknown"),
                            email = dataUser.optString("email", "Unknown")
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
}