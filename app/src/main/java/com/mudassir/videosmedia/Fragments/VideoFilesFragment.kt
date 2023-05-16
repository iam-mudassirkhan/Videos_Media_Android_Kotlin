package com.mudassir.videosmedia.Fragments

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_player.DB.MusicDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mudassir.videoPlayer.RoomDB.HistoryEntity
import com.mudassir.videosmedia.HistoryActivity
import com.mudassir.videosmedia.Interfaces.onVideoClickInterface
import com.mudassir.videosmedia.R
import com.mudassir.videosmedia.VideoAdapter
import com.mudassir.videosmedia.VideoDataClass
import com.mudassir.videosmedia.VideoModel
import com.mudassir.videosmedia.VideoViewModel
import com.mudassir.videosmedia.databinding.FragmentVideoFilesBinding
import com.mudassir.videosmedia.databinding.RenameDailogBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


private const val DELETE_PERMISSION_REQUEST = 221

class VideoFilesFragment : Fragment(), onVideoClickInterface {

    lateinit var binding: FragmentVideoFilesBinding
    lateinit var adapter: VideoAdapter
    lateinit var videoList : ArrayList<VideoDataClass>
    lateinit var searchList : ArrayList<VideoDataClass>
     var search : Boolean? = null
    private var popupMenu: PopupMenu? = null
    private val REQUEST_CODE_COPY_FILE = 130
    private val DELETE_REQUEST_CODE = 120
    private var launcher : ActivityResultLauncher<IntentSenderRequest>? = null

    private val viewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
val view = inflater.inflate(R.layout.fragment_video_files, container, false)
binding = FragmentVideoFilesBinding.bind(view)


        if (requestRuntimePermission()){


        }
        else{
            Toast.makeText(requireContext(), "Cancel", Toast.LENGTH_SHORT).show()
        }

      binding.videoRecyclerView.setHasFixedSize(true)
        binding.videoRecyclerView.setItemViewCacheSize(10)
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        search = false
        videoList = getAllVideos()
        adapter = VideoAdapter(requireContext(), videoList, this )
        binding.videoRecyclerView.adapter = adapter


        viewModel.permissionNeededForDeleteFile.observe(requireActivity(), Observer { intentSender ->
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


        return binding.root
    }


    //for requesting permission
    private fun requestRuntimePermission (): Boolean{
        if (ActivityCompat.checkSelfPermission (  requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (  requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()

            else  ActivityCompat.requestPermissions (  requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)

        }
    }

    private fun getAllVideos(): ArrayList<VideoDataClass>{
        val tempList = ArrayList<VideoDataClass>()
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            /* MediaStore.Video.Media.BUCKET_DISPLAY_NAME,*/ MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.DURATION)
        val cursor = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            MediaStore.Video.Media.DATE_ADDED +" DESC")
        if(cursor != null)
            if(cursor.moveToNext())
                do {
                    //checking null safety with ?: operator
                    val videoTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))?: "Unknown"
                    val videoID = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
//                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
//                    val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val videoSize = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    //just add null checking in end, this 0L is alternative value if below function returns a null value
                    val videoDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))?.toLong()?:0L

                    try {

                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            videoID.toLong()
                        )

                        val file = File(videoPath)
                        val artUri = Uri.fromFile(file)
                        val video = VideoDataClass(videoID,videoTitle, duration = videoDuration, videoSize, videoPath, artUri, contentUri)
                        if(file.exists()) tempList.add(video)


                    }catch (e:Exception){}
                }while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }

    override fun onVideoClick(positon: Int, videoDataClass: VideoDataClass) {

        val addHistory = HistoryEntity(null, videoDataClass.title, videoDataClass.path)

        GlobalScope.launch {
            MusicDatabase.getDataBase(requireContext()).getMusicDao().insertHistory(addHistory)
        }


        val exoPlayerFragment = ExoPlayerFragment.newInstance(videoDataClass.path)

        // Replace or add the ExoPlayerFragment to the desired container
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentFL, exoPlayerFragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onMoreOptionsClick(positon: Int, videoDataClass: VideoDataClass, passAnyView: View) {
        popUpMenu(passAnyView,videoDataClass)
    }



    override fun onScopeVideoClick(positon: Int, videoModel: VideoModel) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_menu, menu)

        val searchView = menu.findItem(R.id.search_view)?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                   searchList = ArrayList()
                    for (video in videoList) {
                        if (video.title.lowercase().contains(newText.lowercase()))
                           searchList.add(video)
                    }
                   search = true
                    adapter.updateList(searchList = searchList)
                }
                return true
            }
        })

        // Find the toggle button in the menu
        val toggleButton = menu.findItem(R.id.gridLayout)

        // Set the button's icon based on the current layout type
        val currentLayoutType = binding.videoRecyclerView.layoutManager?.javaClass?.simpleName
        toggleButton.icon = if (currentLayoutType == "LinearLayoutManager") {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.gridLayout -> {
                // Toggle between LinearLayoutManager and GridLayoutManager
                val currentLayoutType = binding.videoRecyclerView.layoutManager?.javaClass?.simpleName
                binding.videoRecyclerView.layoutManager = if (currentLayoutType == "LinearLayoutManager") {
                    GridLayoutManager(requireContext(), 2)
                } else {
                    LinearLayoutManager(requireContext())
                }

                // Here I am Saving the layout type to SharedPreferences
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("layout_type", binding.videoRecyclerView.layoutManager?.javaClass?.simpleName)
                    apply()
                }

                //I am Updating the button's icon
                item.icon = if (currentLayoutType == "LinearLayoutManager") {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid)
                }
                return true
            }

            R.id.bookMarkHistoryID-> {
                Toast.makeText(requireContext(), "History", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, HistoryActivity::class.java)
                startActivity(intent)

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun popUpMenu(imageView: View, videoLists: VideoDataClass) {
        popupMenu = PopupMenu(imageView.context, imageView)
        popupMenu!!.inflate(R.menu.video_item_popup_menu)
        popupMenu!!.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.mn_CopyFile -> {
                    // Create a file picker intent to select the destination file path
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "video/*"
                        putExtra(Intent.EXTRA_TITLE, videoLists.title)
//                        putExtra(Intent.EXTRA_TITLE, "new_file.pdf")
                    }

                    // Start the file picker activity
                    (context as Activity).startActivityForResult(intent, REQUEST_CODE_COPY_FILE)
                    true
                }

                R.id.mn_Share -> {
                    try {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "video/*"
//                        val path = imagesUri?.let { it1 -> File(it1).absolutePath }
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoLists.path))
//                        startActivity(Intent.createChooser(share, "Share via"))
                        context?.startActivity(Intent.createChooser(share, "Share By"))
                        //                       Toast.makeText(applicationContext, "Share is not implemented", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }

                R.id.mn_Rename -> {

                    requestPermission()

                    val customDialog = LayoutInflater.from(context).inflate(R.layout.rename_dailog, null)
                    val bindingRF = RenameDailogBinding.bind(customDialog)
                    val dialog = MaterialAlertDialogBuilder(requireContext()).setView(customDialog)
                        .setCancelable(false)
                        .setPositiveButton("Rename") { self, _ ->
                            self.dismiss()



                            val currenFile = File(videoLists.path)
                            val newName = bindingRF.renameField.text
//                            rename(videoLists.path.toUri(), newName.toString())

                            if (newName != null && currenFile.exists() && newName.toString()
                                    .isNotEmpty()
                            ) {

                                val newFile = File(
                                    currenFile.parentFile,
                                    newName.toString() + "." + currenFile.extension
                                )
                                Log.d("IsFileRename", "popUpMenu: $newName")

                                if (currenFile.renameTo(newFile)) {
                                    Toast.makeText(context, newName.toString(), Toast.LENGTH_SHORT)
                                        .show()
                                    MediaScannerConnection.scanFile(
                                        context,
                                        arrayOf(newFile.toString()),
                                        arrayOf("video/*"),
                                        null
                                    )
                                    videoLists.title = newFile.name
                                    videoLists.path = newFile.path
                                    binding.videoRecyclerView.adapter?.notifyDataSetChanged()
//                                    notifyItemChanged()
                                } else {
                                    Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            } else {
                                Toast.makeText(context, "Access Denied", Toast.LENGTH_SHORT).show()
                            }

                        }
                        .setNegativeButton("Cancel") { self, _ ->
                            self.dismiss()
                        }
                        .create()
                    dialog.show()
                    bindingRF.renameField.text = SpannableStringBuilder(videoLists.title)





                    true
                }


                R.id.mn_Delete -> {

//                    val filePath = videoLists.path
//                    val isDeleted = deleteFileFromStorage(filePath)
//                    if (isDeleted) {
//                        Toast.makeText(context, "${videoLists.title} is Deleted", Toast.LENGTH_SHORT).show()
//                        notifyDataSetChanged()
//                    } else {
//                        Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
//                    }
//                    requestPermission()
                    deleteFile(videoLists)


                    true
                }

                else -> {
                    false
                }
            }
        }
        popupMenu!!.show()
    }

    // for Requesting 11 or Higher Storage Permission
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent =
                    Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${context?.applicationContext?.packageName}")
                ContextCompat.startActivity(requireContext(), intent, null)

            }
        }

    }

//    private fun deleteFile(inputPath: String, inputFile: String) {
//        try {
//            // delete the original file
//            File(inputPath + inputFile).delete()
//        } catch (e: java.lang.Exception) {
//            e.message?.let { Log.e("tag", it) }
//        }
//    }

    fun deleteFileFromStorage(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) { // Check if file exists
            return file.delete() // Delete the file
        } else {
            // Log an error message for debugging
            println("File not found at path: $filePath")
        }
        return false // Return false if file does not exist or deletion fails
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            viewModel.deletePendingFile()
        }
    }

    private fun deleteFile(video: VideoDataClass) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, video.title))
            .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
                viewModel.deleteFile(video)
            }
            .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Rename file.
     *
     * @param uri    - filepath
     * @param rename - the name you want to replace with original.
     */
    fun rename(uri: Uri?, rename: String?) {

        //create content values with new name and update
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.TITLE, rename)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().contentResolver.update(uri!!, contentValues, null)
        }
    }

    fun renameFile(context: Context, file: File, newFileName: String): Boolean {
        val resolver = context.contentResolver

        // Create the update content values
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
        }

        // Prepare the selection and selection arguments
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)

        // Perform the update using MediaStore
        val updatedRows = resolver.update(MediaStore.Files.getContentUri("external"), values, selection, selectionArgs)

        // Check if the update was successful
        return updatedRows > 0
    }

    /**
     * Delete file.
     *
     *
     * If [ContentResolver] failed to delete the file, use trick,
     * SDK version is >= 29(Q)? use [SecurityException] and again request for delete.
     * SDK version is >= 30(R)? use [MediaStore.createDeleteRequest].
     */
    fun delete(launcher: ActivityResultLauncher<IntentSenderRequest?>, uri: Uri) {
        val contentResolver = requireContext().contentResolver
        try {

            //delete object using resolver
            contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            var pendingIntent: PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val collection = ArrayList<Uri>()
                collection.add(uri)
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e is RecoverableSecurityException) {
                    pendingIntent = e.userAction.actionIntent
                }
            }
            if (pendingIntent != null) {
                val sender = pendingIntent.intentSender
                val request = IntentSenderRequest.Builder(sender).build()
                launcher.launch(request)
            }
        }
    }

    fun renameFile(launcher: ActivityResultLauncher<IntentSenderRequest?>, uri: Uri, newFileName: String) {
        val contentResolver = requireContext().contentResolver
        try {

            //delete object using resolver
            contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            var pendingIntent: PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val collection = ArrayList<Uri>()
                collection.add(uri)
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e is RecoverableSecurityException) {
                    pendingIntent = e.userAction.actionIntent
                }
            }
            if (pendingIntent != null) {
                val sender = pendingIntent.intentSender
                val request = IntentSenderRequest.Builder(sender).build()
                launcher.launch(request)
            }
        }
    }

}