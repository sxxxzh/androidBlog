package com.blog.myandroidblog.data.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.work.*
import kotlinx.coroutines.delay
import java.io.File

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

fun formatFileSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    
    return when {
        bytes >= gb -> String.format("%.2f GB", bytes.toDouble() / gb)
        bytes >= mb -> String.format("%.2f MB", bytes.toDouble() / mb)
        bytes >= kb -> String.format("%.2f KB", bytes.toDouble() / kb)
        else -> "$bytes B"
    }
}

class FileDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_FILE_URL = "file_url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_FILE_TYPE = "file_type"
        const val KEY_NOTIFICATION_TITLE = "notification_title"
        
        fun enqueueDownload(
            context: Context,
            fileUrl: String,
            fileName: String,
            fileType: String,
            notificationTitle: String = "Downloading file..."
        ) {
            val data = workDataOf(
                KEY_FILE_URL to fileUrl,
                KEY_FILE_NAME to fileName,
                KEY_FILE_TYPE to fileType,
                KEY_NOTIFICATION_TITLE to notificationTitle
            )
            
            val downloadRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
                .setInputData(data)
                .addTag("download_$fileName")
                .build()
            
            WorkManager.getInstance(context).enqueue(downloadRequest)
        }
    }

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_FILE_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val fileType = inputData.getString(KEY_FILE_TYPE) ?: "application/octet-stream"
        val notificationTitle = inputData.getString(KEY_NOTIFICATION_TITLE) ?: "Downloading file..."
        
        return try {
            val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(fileUrl.toUri()).apply {
                setTitle(notificationTitle)
                setDescription(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                
                // Set download location
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "PersonalBlog/$fileName"
                )
                
                // Allow scanning by MediaScanner
                allowScanningByMediaScanner()
                
                // Set MIME type
                setMimeType(fileType)
            }
            
            val downloadId = downloadManager.enqueue(request)
            
            // Monitor download progress
            monitorDownloadProgress(downloadManager, downloadId, fileName)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to e.message))
        }
    }
    
    private suspend fun monitorDownloadProgress(
        downloadManager: DownloadManager,
        downloadId: Long,
        fileName: String
    ) {
        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloading = false
                        // File downloaded successfully
                        val uri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        setProgress(workDataOf(
                            "status" to "completed",
                            "file_uri" to uri
                        ))
                    }
                    DownloadManager.STATUS_FAILED -> {
                        downloading = false
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        setProgress(workDataOf(
                            "status" to "failed",
                            "error" to "Download failed with reason: $reason"
                        ))
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val downloadedBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        
                        if (totalBytes > 0) {
                            val progress = (downloadedBytes * 100 / totalBytes).toInt()
                            setProgress(workDataOf(
                                "status" to "downloading",
                                "progress" to progress,
                                "file_name" to fileName
                            ))
                        }
                    }
                }
            }
            cursor.close()
            delay(500) // Check every 500ms
        }
    }
}

class DownloadManager(private val context: Context) {
    
    fun downloadFile(
        fileUrl: String,
        fileName: String,
        fileType: String,
        onProgress: (Int) -> Unit = {},
        onComplete: (File) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val workManager = WorkManager.getInstance(context)
        
        val data = workDataOf(
            FileDownloadWorker.KEY_FILE_URL to fileUrl,
            FileDownloadWorker.KEY_FILE_NAME to fileName,
            FileDownloadWorker.KEY_FILE_TYPE to fileType
        )
        
        val downloadRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
            .setInputData(data)
            .addTag("download_$fileName")
            .build()
        
        workManager.enqueue(downloadRequest)
        
        // Observe download progress
        workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getInt("progress", 0)
                    onProgress(progress)
                }
                WorkInfo.State.SUCCEEDED -> {
                    val fileUri = workInfo.outputData.getString("file_uri")
                    if (fileUri != null) {
                        val file = File(Uri.parse(fileUri).path ?: "")
                        onComplete(file)
                    }
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString("error") ?: "Download failed"
                    onError(error)
                }
                else -> {}
            }
        }
    }
    
    fun getDownloadedFiles(): List<File> {
        val downloadDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "PersonalBlog"
        )
        return downloadDir.listFiles()?.toList() ?: emptyList()
    }
    
    fun deleteDownloadedFile(fileName: String): Boolean {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "PersonalBlog/$fileName"
        )
        return file.delete()
    }
}