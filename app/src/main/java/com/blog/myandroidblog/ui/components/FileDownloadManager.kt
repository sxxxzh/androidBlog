package com.blog.myandroidblog.ui.components

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.getSystemService
import android.util.Log

object FileDownloadManager {
    
    fun downloadFile(
        context: Context,
        fileUrl: String,
        fileName: String,
        mimeType: String? = null
    ): Long {
        return try {
            val downloadManager = context.getSystemService<DownloadManager>() ?: return -1
            
            // Determine MIME type if not provided
            val finalMimeType = mimeType ?: getMimeTypeFromFileName(fileName)
            
            val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
                setTitle(fileName)
                setDescription("Downloading from Personal Blog")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PersonalBlog/$fileName")
                setMimeType(finalMimeType)
                allowScanningByMediaScanner()
            }
            
            val downloadId = downloadManager.enqueue(request)
            Log.d("FileDownloadManager", "Started download for $fileName with ID: $downloadId")
            downloadId
            
        } catch (e: Exception) {
            Log.e("FileDownloadManager", "Failed to download file: ${e.message}", e)
            -1
        }
    }
    
    private fun getMimeTypeFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) 
            ?: "application/octet-stream"
    }
    
    fun getDownloadStatus(context: Context, downloadId: Long): Int {
        val downloadManager = context.getSystemService<DownloadManager>() ?: return -1
        val query = DownloadManager.Query().setFilterById(downloadId)
        
        downloadManager.query(query)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            }
        }
        return -1
    }
}