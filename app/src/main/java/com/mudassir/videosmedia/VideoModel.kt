package com.mudassir.videosmedia

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.Date

data class VideoModel(val id: Long, val displayName: String, val dateTaken: Date, val contentUri: Uri, var path: String) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel) = oldItem == newItem
        }
    }
}