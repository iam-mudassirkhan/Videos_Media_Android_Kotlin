package com.mudassir.videosmedia.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_player.DB.MusicDatabase
import com.mudassir.videoPlayer.RoomDB.Music
import com.mudassir.videosmedia.BookmarkAdapter
import com.mudassir.videosmedia.Interfaces.onBookMarkVideoClick
import com.mudassir.videosmedia.R
import com.mudassir.videosmedia.Repository
import com.mudassir.videosmedia.ViewModel
import com.mudassir.videosmedia.ViewModelFactory
import com.mudassir.videosmedia.databinding.FragmentBookMarkBinding


class BookMarkFragment : Fragment(), onBookMarkVideoClick {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentBookMarkBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        val musicDao = MusicDatabase.getDataBase(requireContext()).getMusicDao()
        val musicRepo = Repository(musicDao)
        val myViewModel = ViewModelProvider(this, ViewModelFactory(musicRepo)).get(ViewModel::class.java)


        binding.bookMarkRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        myViewModel.getAllMusic().observe(viewLifecycleOwner){
            if (it.isEmpty()){
                binding.imageViewForEmptyList.visibility = View.VISIBLE
                binding.bookMarkRecyclerView.visibility = View.GONE
                binding.tvEmptyText.visibility=View.VISIBLE
            }
            else{
                binding.imageViewForEmptyList.visibility = View.GONE
                binding.bookMarkRecyclerView.visibility = View.VISIBLE
                binding.tvEmptyText.visibility=View.GONE
            }
            binding.bookMarkRecyclerView.adapter = BookmarkAdapter(requireContext(),
                it, this)
        }

        return binding.root
    }

    override fun playBookMarkedVideo(position: Int, music: Music) {
        val exoPlayerFragment = ExoPlayerFragment.newInstance(music.path)

        // Replace or add the ExoPlayerFragment to the desired container
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentFL, exoPlayerFragment)
            ?.addToBackStack(null)
            ?.commit()
    }

}