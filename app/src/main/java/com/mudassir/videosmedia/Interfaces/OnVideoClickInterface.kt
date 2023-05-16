package com.mudassir.videosmedia.Interfaces

import android.view.View
import com.mudassir.videosmedia.VideoDataClass
import com.mudassir.videosmedia.VideoModel


interface onVideoClickInterface {

    fun onVideoClick(positon: Int, videoDataClass: VideoDataClass)
    fun onMoreOptionsClick (positon: Int, videoDataClass: VideoDataClass, passAnyView: View)
    fun onScopeVideoClick(positon: Int, videoModel: VideoModel)
}