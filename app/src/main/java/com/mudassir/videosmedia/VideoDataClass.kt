package com.mudassir.videosmedia

import android.net.Uri

data class VideoDataClass (val id: String, var title : String, val duration: Long = 0, val size: String, var path: String
                           , val artUri: Uri, val contentUri: Uri)
