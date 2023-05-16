package com.mudassir.videosmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mudassir.videosmedia.Adapters.ViewPagerAdapter
import com.mudassir.videosmedia.Fragments.BookMarkFragment
import com.mudassir.videosmedia.Fragments.ImagesFragment
import com.mudassir.videosmedia.Fragments.ScopeVideoFragment
import com.mudassir.videosmedia.Fragments.VideoFilesFragment
import com.mudassir.videosmedia.Fragments.VideoScopeImplentFragment
import com.mudassir.videosmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter= ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(VideoFilesFragment(),"Video Files")
        adapter.addFragment(BookMarkFragment(),"Book Mark")
        adapter.addFragment(ImagesFragment(),"Images")
        adapter.addFragment(ScopeVideoFragment(), "ScopeVideo")
        binding.viewPager.adapter=adapter
        binding.tbLayout.setupWithViewPager(binding.viewPager)

    }
}