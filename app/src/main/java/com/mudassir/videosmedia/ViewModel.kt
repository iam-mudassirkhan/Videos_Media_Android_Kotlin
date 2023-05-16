package com.mudassir.videosmedia

import android.content.ContentUris
import android.content.IntentSender
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videoPlayer.RoomDB.Music
import java.io.File

open class ViewModel(val repository: Repository) : ViewModel() {

    private val _videos = MutableLiveData<List<VideoDataClass>>()
    val videos: LiveData<List<VideoDataClass>> get() = _videos

    private var contentObserver: ContentObserver? = null

    private var pendingDeleteImage: VideoDataClass? = null
    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete


    fun getAllMusic(): LiveData<List<Music>>{
        return repository.getAllMusic()
    }

    fun getAllHistory(): LiveData<List<HistoryEntity>>{
        return repository.getAllHistory()
    }


}