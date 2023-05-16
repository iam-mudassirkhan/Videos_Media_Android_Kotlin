package com.mudassir.videosmedia

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.IntentSender
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    // Code for Getting Videos
    private val _videos = MutableLiveData<List<VideoModel>>()
    val videos: LiveData<List<VideoModel>> get() = _videos

    private var contentObserverforVideo: ContentObserver? = null

    private var pendingDeleteVideo: VideoModel? = null
    private val _permissionNeededForDeleteVideo = MutableLiveData<IntentSender?>()
    val permissionNeededForDeleteVideo: LiveData<IntentSender?> = _permissionNeededForDeleteVideo

    fun loadVideos() {
        viewModelScope.launch {
            val videoList = queryVidoes()
            _videos.postValue(videoList)

            if (contentObserverforVideo == null) {
                contentObserverforVideo = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadVideos()
                }
            }
        }
    }

    fun deleteVideo(video: VideoModel) {
        viewModelScope.launch {
            performDeleteVideo(video)

        }
    }

    fun deletePendingVideo() {
        pendingDeleteVideo?.let { video ->
            pendingDeleteVideo = null
            deleteVideo(video)
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private suspend fun queryVidoes(): List<VideoModel> {
        var videoList = mutableListOf<VideoModel>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATA
            )

           /* val selection = "${MediaStore.Video.Media.DATE_TAKEN} >= ?"

            val selectionArgs = arrayOf(
                dateToTimestamp(day = 1, month = 1, year = 2020).toString()
            )
*/
            val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"

            getApplication<Application>().contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
               /* selection,
                selectionArgs,
                sortOrder*/
            )?.use { cursor ->
                videoList = addVideosFromCursor(cursor)
            }
        }

        return videoList
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun addVideosFromCursor(cursor: Cursor): MutableList<VideoModel> {
        val videos = mutableListOf<VideoModel>()


        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
        val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val itemPathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

        while (cursor.moveToNext()) {

            val id = cursor.getLong(idColumn)
            val dateTaken = Date(cursor.getLong(dateTakenColumn))
            val displayName = cursor.getString(displayNameColumn)
            val itemPath = cursor.getString(itemPathColumn)

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val video = VideoModel(id, displayName, dateTaken, contentUri, itemPath)
            videos += video

        }
        return videos
    }

    private suspend fun performDeleteVideo(videoModel: VideoModel) {
        withContext(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.delete(
                    videoModel.contentUri,
                    "${MediaStore.Video.Media._ID} = ?",
                    arrayOf(videoModel.id.toString()
                    )
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException
                    pendingDeleteVideo = videoModel
                    _permissionNeededForDeleteVideo.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }

    // The Below Code is for Performing Deletion in VideoFilesFragment
    private var pendingDeleteFile: VideoDataClass? = null
    private val _permissionNeededForDeleteFile = MutableLiveData<IntentSender?>()
    val permissionNeededForDeleteFile: LiveData<IntentSender?> = _permissionNeededForDeleteFile

    fun deleteFile(videoDataClass: VideoDataClass) {
        viewModelScope.launch {
            performDeleteFile(videoDataClass)

        }
    }

    fun deletePendingFile() {
        pendingDeleteFile?.let { video ->
            pendingDeleteFile = null
            deleteFile(video)
        }
    }

    private suspend fun performDeleteFile(videoDataClass: VideoDataClass) {
        withContext(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.delete(
                    videoDataClass.contentUri,
                    "${MediaStore.Video.Media._ID} = ?",
                    arrayOf(videoDataClass.id.toString()
                    )
                )
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException
                            ?: throw securityException
                    pendingDeleteFile = videoDataClass
                    _permissionNeededForDeleteFile.postValue(
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    )
                } else {
                    throw securityException
                }
            }
        }
    }

}
/**
 * Extension method to register a [ContentObserver]
 */
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}