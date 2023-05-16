package com.mudassir.videosmedia.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mudassir.videosmedia.Interfaces.onVideoClickInterface
import com.mudassir.videosmedia.VideoModel

import com.mudassir.videosmedia.databinding.VideoViewBinding

class ScopeVideoAdapter(val context: Context,val videoModel:ArrayList<VideoModel>, val interfaceForClick: onVideoClickInterface): RecyclerView.Adapter<ScopeVideoAdapter.ScopeVideoViewHolder>() {



    class ScopeVideoViewHolder(val binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScopeVideoViewHolder {
        val binding = VideoViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ScopeVideoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return  videoModel.size
    }

    override fun onBindViewHolder(holder: ScopeVideoViewHolder, position: Int) {
        val myVideoList = videoModel[position]
        holder.binding.textView.text = myVideoList.displayName

        holder.itemView.setOnClickListener {
            interfaceForClick.onScopeVideoClick(position, myVideoList)
        }

        holder.binding.moreOptions.setOnClickListener {
//            interfaceForClick.onMoreOptionsClick(position, myVideoList, holder.binding.moreOptions)
        }

        Glide.with(holder.binding.imageView)
            .load(myVideoList.contentUri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.binding.imageView)
    }
}