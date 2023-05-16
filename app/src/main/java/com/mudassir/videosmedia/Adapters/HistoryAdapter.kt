package com.mudassir.videosmedia.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.music_player.DB.MusicDatabase
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videoPlayer.RoomDB.Music
import com.mudassir.videosmedia.Interfaces.onHistoryClick
import com.mudassir.videosmedia.R
import com.mudassir.videosmedia.databinding.VideoViewForHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryAdapter(val context: Context, val historyList: ArrayList<HistoryEntity>,val onHistoryClick: onHistoryClick) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: VideoViewForHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            VideoViewForHistoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val myHistoryList = historyList[position]
        holder.binding.textView.text = myHistoryList.name

        Glide.with(context).asBitmap().load(myHistoryList.path).apply(
            RequestOptions().placeholder(
                R.drawable.vplaceholder
            )
        )
            .centerCrop().into(holder.binding.imageView)

        holder.itemView.setOnClickListener {
            onHistoryClick.onHistoryVideoClick(position, myHistoryList)
        }

        holder.binding.removeFromHistory.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val deleteHistory = HistoryEntity(myHistoryList.id, myHistoryList.name, myHistoryList.path)
                MusicDatabase.getDataBase(context).getMusicDao().deleteHistory(deleteHistory)
            }

            Toast.makeText(context, "Video Removed from History", Toast.LENGTH_SHORT).show()

        }
    }
}