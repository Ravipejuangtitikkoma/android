package com.example.myapplication2.repository

import com.example.myapplication2.model.Post
import com.example.myapplication2.network.ApiResponse
import com.example.myapplication2.network.ApiService

class PostRepository {
    suspend fun getPosts(token: String): ApiResponse<List<Post>> {
        return ApiService.getPosts(token)
    }
}