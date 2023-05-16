package com.mudassir.videoPlayer.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music")
data class Music(
    @PrimaryKey (autoGenerate = true)
    val id : Int?,
    val name: String,
    val path: String
                 )
