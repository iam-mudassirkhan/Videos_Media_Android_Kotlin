package com.mudassir.videosmedia

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.music_player.DB.MusicDao
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videoPlayer.RoomDB.Music

class Repository(val musicDao: MusicDao) {

    fun getAllMusic() : LiveData<List<Music>> {
        return musicDao.getAllMusic()

    }

    fun getAllHistory() : LiveData<List<HistoryEntity>>{
        return musicDao.getAllHistory()
    }


}