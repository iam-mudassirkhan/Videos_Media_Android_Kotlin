package com.mudassir.videosmedia

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.music_player.DB.MusicDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mudassir.videoPlayer.RoomDB.Music
import com.mudassir.videosmedia.Interfaces.onVideoClickInterface
import com.mudassir.videosmedia.databinding.RenameDailogBinding
import com.mudassir.videosmedia.databinding.VideoViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoAdapter(private val context: Context, private var videoList: ArrayList<VideoDataClass>,var onVideoClickInterface: onVideoClickInterface): RecyclerView.Adapter<VideoAdapter.VideoHolder>() {

    private var popupMenu: PopupMenu? = null
    private val REQUEST_CODE_COPY_FILE = 1
    private val DELETE_REQUEST_CODE = 120

    class VideoHolder(var binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root){
        var titel = binding.textView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
       val binding = VideoViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return VideoHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {

        holder.titel.text = videoList[position].title

        holder.itemView.setOnClickListener {
            onVideoClickInterface.onVideoClick(position, videoList[position])
        }
        holder.binding.moreOptions.setOnClickListener {
//            popUpMenu(holder.binding.moreOptions, videoList[position])
            onVideoClickInterface.onMoreOptionsClick(position, videoList[position], holder.binding.moreOptions)
        }

        holder.binding.addToBookMark.setOnClickListener {
            val music = Music(null,videoList[position].title, videoList[position].path)

            GlobalScope.launch {
                MusicDatabase.getDataBase(context).getMusicDao().insert(music)
            }

            Toast.makeText(context, "added to BookMark", Toast.LENGTH_SHORT).show()
            holder.binding.addToBookMark.isVisible = false
            holder.binding.alreadyAddedToBookMark.isVisible = true
        }
        holder.binding.alreadyAddedToBookMark.setOnClickListener {
            Toast.makeText(context, "Already Added", Toast.LENGTH_SHORT).show()
        }

        Glide.with(context)
            .asBitmap()
            .load(videoList[position].path).apply(RequestOptions().placeholder(R.drawable.vplaceholder))
            .centerCrop().into(holder.binding.imageView)

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
                        context.startActivity(Intent.createChooser(share, "Share By"))
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
                    val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                        .setCancelable(false)
                        .setPositiveButton("Rename") { self, _ ->
                            self.dismiss()

                            val currenFile = File(videoLists.path)
                            val newName = bindingRF.renameField.text
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
                                    notifyDataSetChanged()
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
                    performDeleteVideo(videoLists, context)


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
                intent.data = Uri.parse("package:${context.applicationContext.packageName}")
                ContextCompat.startActivity(context, intent, null)

            }
        }

    }

    private fun deleteFile(inputPath: String, inputFile: String) {
        try {
            // delete the original file
            File(inputPath + inputFile).delete()
        } catch (e: java.lang.Exception) {
            e.message?.let { Log.e("tag", it) }
        }
    }

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

   /* suspend fun performDeleteVideo(video: VideoDataClass) {
       withContext(Dispatchers.IO) {
        try {
            context.applicationContext.contentResolver.delete(
                video.artUri,
                "${MediaStore.Video.Media._ID} = ?",
                arrayOf(video.id.toString()
                )
            )
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException
                        ?: throw securityException
                   _permissionNeededForDelete.postValue(
                recoverableSecurityException.userAction.actionIntent.intentSender
                   )
            } else {
                throw securityException
            }
        }
        }
    }*/

    fun performDeleteVideo(video: VideoDataClass, context: Context) {
        try {
            val contentResolver = context.applicationContext.contentResolver

            val selection = "${MediaStore.Video.Media._ID} = ?"
            val selectionArgs = arrayOf(video.id.toString())

            val rowsDeleted = contentResolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs)

            if (rowsDeleted > 0) {
                // Video deleted successfully
                // Perform any additional actions or UI updates if needed
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            } else {
                // Failed to delete video
                // Handle the error or show an appropriate message
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException = securityException as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    // Handle the recoverable security exception
                    // You can request permission to delete the video using the user action intent
                } ?: throw securityException
            } else {
                throw securityException
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(searchList: ArrayList<VideoDataClass>){
        videoList = ArrayList()
        videoList.addAll(searchList)
        notifyDataSetChanged()
    }

   /* fun deleteFileWithPermission(fileUri: Uri, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver

//            val deleteRequest = DeleteRequest.Builder(fileUri).build()
            val deleteRequest = DeleteRequest.Builder(fileUri).build()
            val deleteIntent = contentResolver.createDeleteRequest(deleteRequest)

            try {
                val activity = context as? Activity
                activity?.startIntentSenderForResult(
                    deleteIntent.intentSender,
                    DELETE_REQUEST_CODE,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: IntentSender.SendIntentException) {
                // Handle any errors that occur during the intent sender process
            }
        } else {
            // For versions prior to Android 10, directly delete the file without permission
            deleteFileWithoutPermission(fileUri, context)
        }
    }*/

    private fun deleteFileWithoutPermission(fileUri: Uri, context: Context) {
        val file = File(fileUri.path)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                // File deleted successfully
            } else {
                // Failed to delete file
            }
        } else {
            // File does not exist
        }
    }

    fun deleteFileWithPermissionTwo(fileUri: Uri, context: Context) {
        val contentResolver: ContentResolver = context.contentResolver

        try {
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(fileUri, DocumentsContract.getTreeDocumentId(fileUri))
            contentResolver.delete(documentUri, null, null)
            // File deleted successfully
        } catch (e: SecurityException) {
            // Handle the SecurityException when user permission is required
            // Request necessary permissions and perform the deletion in the callback
        } catch (e: Exception) {
            // Handle any other exceptions that may occur during the deletion process
        }
    }

    private fun deleteFileWithoutPermissionTwo(fileUri: Uri, context: Context) {
        val contentResolver: ContentResolver = context.contentResolver

        try {
            contentResolver.delete(fileUri, null, null)
            // File deleted successfully
        } catch (e: Exception) {
            // Handle any exceptions that may occur during the deletion process
        }
    }


}