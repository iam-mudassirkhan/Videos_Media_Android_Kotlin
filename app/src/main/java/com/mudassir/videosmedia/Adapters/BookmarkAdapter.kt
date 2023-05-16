package com.mudassir.videosmedia
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.music_player.DB.MusicDatabase
import com.mudassir.videoPlayer.RoomDB.Music
import com.mudassir.videosmedia.Interfaces.onBookMarkVideoClick
import com.mudassir.videosmedia.databinding.VideoViewForBookmarkBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkAdapter(private val context: Context, private var bookMarkList: List<Music>, var onBookMarkVideoClick: onBookMarkVideoClick)
   :RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>()  {

   class BookmarkViewHolder(var binding : VideoViewForBookmarkBinding): RecyclerView.ViewHolder(binding.root)

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
     val binding = VideoViewForBookmarkBinding.inflate(LayoutInflater.from(context), parent, false)
      return BookmarkViewHolder(binding)
   }

   override fun getItemCount(): Int {
   return bookMarkList.size
   }

   @OptIn(DelicateCoroutinesApi::class)
   override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
   var myBookMarkList = bookMarkList[position]
      holder.binding.textView.text = myBookMarkList.name

      Glide.with(context).asBitmap().load(myBookMarkList.path).apply(RequestOptions().placeholder(R.drawable.vplaceholder))
         .centerCrop().into(holder.binding.imageView)

      holder.binding.removeFromBookMark.setOnClickListener {
         GlobalScope.launch (Dispatchers.IO){
         val deleteMusic = Music(myBookMarkList.id, myBookMarkList.name, myBookMarkList.path)
         MusicDatabase.getDataBase(context).getMusicDao().deletemusic(deleteMusic)

      }
         Toast.makeText(context, "Video Removed", Toast.LENGTH_SHORT).show()
      }

      holder.itemView.setOnClickListener {
         onBookMarkVideoClick.playBookMarkedVideo(position, myBookMarkList)
      }

   }

   }