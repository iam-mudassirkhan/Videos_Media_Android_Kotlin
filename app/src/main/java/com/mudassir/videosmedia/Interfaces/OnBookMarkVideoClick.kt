package com.mudassir.videosmedia.Interfaces

import com.mudassir.videoPlayer.RoomDB.Music


interface onBookMarkVideoClick {
    fun playBookMarkedVideo(position: Int, music: Music)
}