package com.mudassir.videosmedia.Interfaces

import com.mudassir.videoPlayer.RoomDB.HistoryEntity

interface onHistoryClick {
    fun onHistoryVideoClick(position: Int, historyEntity: HistoryEntity)
}