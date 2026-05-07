package com.devfamily.sehara.data

import retrofit2.http.GET
import retrofit2.http.Path



interface ApiService {

    @GET("api/files/videos")
    suspend fun getVideos(): List<FileItem>

    @GET("api/music")
    suspend fun getMusic(): List<FileItem>

    @GET("api/music/recent")
    suspend fun getRecentMusic(): List<FileItem>

    @GET("api/files/images")
    suspend fun getImages(): List<FileItem>

    @GET("api/files/documents")
    suspend fun getDocuments(): List<FileItem>

    @GET("api/files/recent")
    suspend fun getRecentFiles(): List<FileItem>

    @GET("api/stream/{id}")
    suspend fun streamFile(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>

    @GET("api/thumbnails/{id}")
    suspend fun getThumbnail(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>

    @GET("api/files/{file_type}/{term}")
    suspend fun searchFiles(
        @Path("file_type") fileType: String,
        @Path("term") term: String
    ): List<FileItem>
}