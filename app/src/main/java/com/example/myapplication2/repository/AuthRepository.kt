package com.example.myapplication2.repository

import com.example.myapplication2.model.User
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.network.ApiService

class AuthRepository {
    suspend fun login(email: String, password: String): ApiResponse<Pair<User, String>> {
        return ApiService.login(email, password)
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse<String> {
        return ApiService.register(name, email, password)
    }
}