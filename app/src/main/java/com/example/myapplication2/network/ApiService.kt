package com.example.myapplication2.network

import android.util.Log
import com.example.myapplication2.model.Post
import com.example.myapplication2.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object ApiService {
    private const val BASE_URL = "http://10.0.2.2:8000/api"

    // Login
    suspend fun login(email: String, password: String): ApiResponse<Pair<User, String>> { // Mengembalikan User dan Token
        return try {
            val url = URL("$BASE_URL/login")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                connection.outputStream.use { os ->
                    val input = jsonBody.toByteArray(StandardCharsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.optBoolean("status", false)) {
                        val token = jsonResponse.getString("access_token")
                        val userDataJson = jsonResponse.getJSONObject("user")
                        val user = User(
                            id = userDataJson.getInt("id"),
                            name = userDataJson.getString("name"),
                            email = userDataJson.getString("email")
                        )
                        ApiResponse.Success(Pair(user, token))
                    } else {
                        val errorMessage = jsonResponse.optString("message", "Login failed")
                        ApiResponse.Error(errorMessage)
                    }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText()
                    Log.e("API_SERVICE", "Login Error: $responseCode - $errorBody")
                    ApiResponse.Error("Server Error: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            Log.e("API_SERVICE", "Network Error during login: ${e.message}", e)
            ApiResponse.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("API_SERVICE", "Unexpected Error during login: ${e.message}", e)
            ApiResponse.Error("Unexpected Error: ${e.message}")
        }
    }

    // Register
    suspend fun register(name: String, email: String, password: String): ApiResponse<String> {
        return try {
            val url = URL("$BASE_URL/register")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("password", password)
                }.toString()

                connection.outputStream.use { os ->
                    val input = jsonBody.toByteArray(StandardCharsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode in listOf(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED)) {
                    val responseBody = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(responseBody)

                    if (jsonResponse.optBoolean("status", false)) {
                        ApiResponse.Success("Registration successful")
                    } else {
                        val errorMessage = jsonResponse.optString("message", "Registration failed")
                        ApiResponse.Error(errorMessage)
                    }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText()
                    Log.e("API_SERVICE", "Register Error: $responseCode - $errorBody")
                    ApiResponse.Error("Server Error: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            Log.e("API_SERVICE", "Network Error during registration: ${e.message}", e)
            ApiResponse.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("API_SERVICE", "Unexpected Error during registration: ${e.message}", e)
            ApiResponse.Error("Unexpected Error: ${e.message}")
        }
    }

    // Get Posts
    suspend fun getPosts(token: String): ApiResponse<List<Post>> {
        return try {
            val url = URL("$BASE_URL/posts")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(responseBody)

                    val posts = mutableListOf<Post>()
                    if (jsonResponse.has("data")) {
                        val jsonArray = jsonResponse.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            posts.add(
                                Post(
                                    id = jsonObj.getInt("id"),
                                    title = jsonObj.getString("title"),
                                    body = jsonObj.getString("body")
                                )
                            )
                        }
                    }
                    ApiResponse.Success(posts.toList())
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText()
                    Log.e("API_SERVICE", "Get Posts Error: $responseCode - $errorBody")
                    ApiResponse.Error("Server Error: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            Log.e("API_SERVICE", "Network Error fetching posts: ${e.message}", e)
            ApiResponse.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("API_SERVICE", "Unexpected Error fetching posts: ${e.message}", e)
            ApiResponse.Error("Unexpected Error: ${e.message}")
        }
    }
}