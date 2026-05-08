package com.devfamily.sehara.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/music")
    suspend fun getMusic(): List<FileItem>

    @GET("api/music/recent")
    suspend fun getRecentMusic(): List<FileItem>

    @GET("api/music/artists")
    suspend fun getArtists(): List<ArtistItem>

    @GET("api/music/artists/{artist}")
    suspend fun getMusicByArtist(@Path("artist") artist: String): List<FileItem>
    @GET("api/music/genres")
    suspend fun getGenres(): List<GenreItem>

    @GET("api/music/genres/{genre}")
    suspend fun getMusicByGenre(@Path("genre") genre: String): List<FileItem>

    @GET("api/music/search")
    suspend fun searchMusic(@Query("q") term: String): List<FileItem>
    @GET("api/music/artists/search")
    suspend fun searchArtists(@Query("q") term: String): List<ArtistItem>

    @GET("api/videos")
    suspend fun getVideos(): List<FileItem>

    @GET("api/videos/recent")
    suspend fun getRecentVideos(): List<FileItem>

    @GET("api/videos/movies")
    suspend fun getMovies(): List<FileItem>

    @GET("api/videos/movies/search")
    suspend fun searchMovies(@Query("q") term: String): List<FileItem>

    @GET("api/videos/documentaries")
    suspend fun getDocumentaries(): List<FileItem>

    @GET("api/videos/documentaries/search")
    suspend fun searchDocumentaries(@Query("q") term: String): List<FileItem>

    @GET("api/videos/other")
    suspend fun getOtherVideos(): List<FileItem>

    @GET("api/videos/search")
    suspend fun searchVideos(@Query("q") term: String): List<FileItem>

    @GET("api/photos")
    suspend fun getImages(): List<FileItem>

    @GET("api/photos/{id}")
    suspend fun getImageById(@Path("id") id: String): FileItem

    @GET("api/docs")
    suspend fun getDocuments(): List<FileItem>

    @GET("api/docs/books")
    suspend fun getBooks(): List<FileItem>

    @GET("api/docs/books/search")
    suspend fun searchBooks(@Query("q") term: String): List<FileItem>

    @GET("api/docs/documents")
    suspend fun getStandardDocuments(): List<FileItem>

    @GET("api/docs/documents/search")
    suspend fun searchStandardDocuments(@Query("q") term: String): List<FileItem>

    @GET("api/docs/others")
    suspend fun getOtherDocuments(): List<FileItem>

    @GET("api/docs/search")
    suspend fun searchDocuments(@Query("q") term: String): List<FileItem>

    @GET("api/stream/{id}")
    suspend fun streamFile(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>

    @GET("api/thumbnails/{id}")
    suspend fun getThumbnail(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>
}