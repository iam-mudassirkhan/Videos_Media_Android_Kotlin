package com.mudassir.videosmedia.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.mudassir.videosmedia.R


class ExoPlayerFragment : Fragment() {

    private lateinit var playerView: PlayerView
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private var videoPath: String? = null // Video file path

    companion object {
        private const val ARG_VIDEO_PATH = "video_path"

        fun newInstance(videoPath: String): ExoPlayerFragment {
            val fragment = ExoPlayerFragment()
            val args = Bundle()
            args.putString(ARG_VIDEO_PATH, videoPath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPath = arguments?.getString(ARG_VIDEO_PATH)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_exo_player, container, false)
        playerView = view.findViewById(R.id.exoPlayer)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
    }

    private fun initializePlayer() {


//        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
//            requireContext(),
//            DefaultTrackSelector(),
//            DefaultLoadControl()
//        )
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        playerView.player = simpleExoPlayer

        val mediaItem = videoPath?.let { MediaItem.fromUri(it) }
        if (mediaItem != null) {
            simpleExoPlayer.setMediaItem(mediaItem)
        }
        simpleExoPlayer.prepare()
        simpleExoPlayer.play()

//
//        val dataSourceFactory = DefaultDataSourceFactory(
//            requireContext(),
//            DefaultHttpDataSourceFactory("exoplayer")
//        )
//
//        mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(Uri.parse(videoPath))
//
//        simpleExoPlayer.prepare(mediaSource)
//        simpleExoPlayer.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simpleExoPlayer.stop()
        simpleExoPlayer.release()
    }


}