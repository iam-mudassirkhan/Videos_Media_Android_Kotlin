package com.mudassir.videoPlayer.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey (autoGenerate = true)
    val id : Int?,
    val name: String,
    val path: String
)
