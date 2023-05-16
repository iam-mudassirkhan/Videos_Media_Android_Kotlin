package com.example.music_player.DB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videoPlayer.RoomDB.Music

@Dao
interface MusicDao {

    @Insert
  suspend  fun insert(music: Music)

    @Query("SELECT * FROM music" )
   fun getAllMusic():LiveData<List<Music>>

    @Delete
    fun deletemusic(music: Music)

    @Insert
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Delete
    fun deleteHistory(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history")
    fun getAllHistory(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM music WHERE name LIKE :name ")
    fun search(name:String):LiveData<List<Music>>
}