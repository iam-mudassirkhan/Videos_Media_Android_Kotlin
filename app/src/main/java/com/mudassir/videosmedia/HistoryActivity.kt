package com.mudassir.videosmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_player.DB.MusicDatabase
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videosmedia.Adapters.HistoryAdapter
import com.mudassir.videosmedia.Fragments.ExoPlayerFragment
import com.mudassir.videosmedia.Interfaces.onHistoryClick
import com.mudassir.videosmedia.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity(), onHistoryClick {
lateinit   var binding : ActivityHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val musicDao = MusicDatabase.getDataBase(this).getMusicDao()
        val musicRepo = Repository(musicDao)
        val myViewModel = ViewModelProvider(this, ViewModelFactory(musicRepo)).get(ViewModel::class.java)

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)

        myViewModel.getAllHistory().observe(this, Observer {

            if (it.isEmpty()){
                binding.imageViewForEmptyList.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
                binding.tvEmptyText.visibility=View.VISIBLE
            }
            else{
                binding.imageViewForEmptyList.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
                binding.tvEmptyText.visibility=View.GONE
            }

            binding.historyRecyclerView.adapter = HistoryAdapter(this, it as ArrayList, this)
        })
    }

    override fun onHistoryVideoClick(position: Int, historyEntity: HistoryEntity) {
        val exoPlayerFragment = ExoPlayerFragment.newInstance(historyEntity.path)

        // Replace or add the ExoPlayerFragment to the desired container
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentFrameLayout, exoPlayerFragment)
            .addToBackStack(null)
            .commit()
    }


}