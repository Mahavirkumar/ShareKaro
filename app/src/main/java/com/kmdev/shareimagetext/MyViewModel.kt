package com.kmdev.shareimagetext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class MyViewModel : ViewModel() {

    private val _imageSaveStatus = MutableLiveData<Boolean?>(null)
    val imageSaveStatus = _imageSaveStatus

    fun saveImageInGallery(urlAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = downloadImageFromUrl(urlAddress)
            if (bitmap == null) {
                withContext(Dispatchers.Main) {
                    _imageSaveStatus.value = false
                }
            } else {
                val imageSaved = saveImageToDownloadFolder(bitmap)
                withContext(Dispatchers.Main) {
                    _imageSaveStatus.value = imageSaved
                }
            }
        }
    }

    private fun downloadImageFromUrl(urlAddress: String): Bitmap? {
        return try {
            val url = URL(urlAddress)
            val inputStream: InputStream = url.openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToDownloadFolder(bitmap: Bitmap): Boolean {
        return try {
            val filePath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/profile.png"
            )
            val outputStream: OutputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}