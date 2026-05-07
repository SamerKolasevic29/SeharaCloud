package com.devfamily.sehara.ui.navigation

sealed class Routes(val route: String) {
    object Landing : Routes("landing")
    object Home : Routes("home")
    object Music : Routes("music")
    object Photo : Routes("photo")
    object Video : Routes("video")
    object Docs : Routes("docs")
}