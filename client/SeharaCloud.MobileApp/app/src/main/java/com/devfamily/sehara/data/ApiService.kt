package com.devfamily.sehara.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/music")
    suspend fun getMusic(): List<SongItem>

    @GET("api/music/recent")
    suspend fun getRecentMusic(): List<SongItem>

    @GET("api/music/artists")
    suspend fun getArtists(): List<ArtistItem>

    @GET("api/music/artists/{artist}")
    suspend fun getMusicByArtist(@Path("artist") artist: String): List<SongItem>
    @GET("api/music/genres")
    suspend fun getGenres(): List<GenreItem>

    @GET("api/music/genres/{genre}")
    suspend fun getMusicByGenre(@Path("genre") genre: String): List<SongItem>

    @GET("api/music/search")
    suspend fun searchMusic(@Query("q") term: String): List<SongItem>
    @GET("api/music/artists/search")
    suspend fun searchArtists(@Query("q") term: String): List<ArtistItem>

    @GET("api/videos")
    suspend fun getVideos(): List<VideoItem>

    @GET("api/videos/recent")
    suspend fun getRecentVideos(): List<VideoItem>

    @GET("api/videos/movies")
    suspend fun getMovies(): List<VideoItem>

    @GET("api/videos/movies/search")
    suspend fun searchMovies(@Query("q") term: String): List<VideoItem>

    @GET("api/videos/documentaries")
    suspend fun getDocumentaries(): List<VideoItem>

    @GET("api/videos/documentaries/search")
    suspend fun searchDocumentaries(@Query("q") term: String): List<VideoItem>

    @GET("api/videos/other")
    suspend fun getOtherVideos(): List<VideoItem>

    @GET("api/videos/search")
    suspend fun searchVideos(@Query("q") term: String): List<VideoItem>

    @GET("api/photos")
    suspend fun getImages(): List<SongItem>

    @GET("api/photos/{id}")
    suspend fun getImageById(@Path("id") id: String): SongItem

    @GET("api/docs")
    suspend fun getDocuments(): List<DocumentItem>

    @GET("api/docs/books")
    suspend fun getBooks(): List<DocumentItem>

    @GET("api/docs/books/search")
    suspend fun searchBooks(@Query("q") term: String): List<DocumentItem>

    @GET("api/docs/documents")
    suspend fun getStandardDocuments(): List<DocumentItem>

    @GET("api/docs/documents/search")
    suspend fun searchStandardDocuments(@Query("q") term: String): List<DocumentItem>

    @GET("api/docs/others")
    suspend fun getOtherDocuments(): List<DocumentItem>

    @GET("api/docs/search")
    suspend fun searchDocuments(@Query("q") term: String): List<DocumentItem>

    @GET("api/stream/{id}")
    suspend fun streamFile(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>

    @GET("api/thumbnails/{id}")
    suspend fun getThumbnail(@Path("id") id: String): retrofit2.Response<okhttp3.ResponseBody>
}