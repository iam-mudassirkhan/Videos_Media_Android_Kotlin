package com.mudassir.videosmedia.Fragments

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mudassir.videosmedia.Adapters.ScopeVideoAdapter
import com.mudassir.videosmedia.Interfaces.onVideoClickInterface
import com.mudassir.videosmedia.R
import com.mudassir.videosmedia.VideoDataClass
import com.mudassir.videosmedia.VideoModel
import com.mudassir.videosmedia.VideoViewModel
import com.mudassir.videosmedia.databinding.FragmentVideoScopeImplentBinding


private const val READ_EXTERNAL_STORAGE_REQUEST = 110
private const val DELETE_PERMISSION_REQUEST = 210

class VideoScopeImplentFragment : Fragment(), onVideoClickInterface {
    private var binding: FragmentVideoScopeImplentBinding? = null
    private val viewModel: VideoViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      binding = FragmentVideoScopeImplentBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )

        binding?.videoScopeRecycler?.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.videos.observe(requireActivity(), Observer {
        binding?.videoScopeRecycler?.adapter = ScopeVideoAdapter(requireContext(),
            it as ArrayList<VideoModel>, this)
            Toast.makeText(requireContext(), it.size, Toast.LENGTH_SHORT).show()
        })


        viewModel.permissionNeededForDeleteVideo.observe(requireActivity(), Observer { intentSender ->
            intentSender?.let {
                startIntentSenderForResult(
                    intentSender,
                    DELETE_PERMISSION_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        })

        binding?.openAlbumButton?.setOnClickListener { openMediaStore() }
        binding?.grantPermissionButton?.setOnClickListener { openMediaStore() }

        if (!haveStoragePermission()) {
            binding?.albumContainer?.visibility = View.VISIBLE
        } else {
            showVideos()
        }



        return binding?.root
    }



    override fun onVideoClick(positon: Int, videoDataClass: VideoDataClass) {

    }

    override fun onMoreOptionsClick(
        positon: Int,
        videoDataClass: VideoDataClass,
        passAnyView: View
    ) {
        TODO("Not yet implemented")
    }


    override fun onScopeVideoClick(positon: Int, videoModel: VideoModel) {
        deleteVideo(videoModel)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showVideos()
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    if (showRationale) {
                        showNoAccess()
                    } else {
                        goToSettings()
                    }
                }
                return
            }
        }
    }



    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            viewModel.deletePendingVideo()
        }
    }

    private fun showVideos() {
        viewModel.loadVideos()
        binding?.albumContainer?.visibility = View.GONE
        binding?.permissionContainer?.visibility = View.GONE
    }

    private fun showNoAccess() {
        binding?.albumContainer?.visibility = View.GONE
        binding?.permissionContainer?.visibility = View.VISIBLE
    }

    private fun openMediaStore() {
        if (haveStoragePermission()) {
            showVideos()
        } else {
            requestPermission()
        }
    }

    private fun goToSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }


    private fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


    private fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                READ_EXTERNAL_STORAGE_REQUEST
            )
        }
    }

    private fun deleteVideo(video: VideoModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, video.displayName))
            .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
                viewModel.deleteVideo(video)
            }
            .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }


}