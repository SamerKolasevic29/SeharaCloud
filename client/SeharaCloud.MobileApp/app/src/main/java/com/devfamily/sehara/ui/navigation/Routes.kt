package com.devfamily.sehara.ui.navigation
import android.net.Uri

sealed class Routes(val route: String) {
    object Landing : Routes("landing")
    object Home : Routes("home")
    object Music : Routes("music")
    object Artist : Routes("artist")
    object Genre : Routes("genre")
    object MusicList : Routes("music_list/{type}/{id}/{name}") {
        fun createRoute(type: String, id: String, name: String) =
            "music_list/$type/$id/${Uri.encode(name)}"
    }
    object Photo : Routes("photo")
    object Video : Routes("video")
    object Docs : Routes("docs")
}