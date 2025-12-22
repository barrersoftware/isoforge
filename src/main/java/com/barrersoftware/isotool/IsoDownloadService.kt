package com.barrersoftware.isotool

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class IsoDownloadService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var notificationManager: NotificationManager? = null
    
    companion object {
        private const val CHANNEL_ID = "iso_downloads"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_URL = "download_url"
        const val EXTRA_FILENAME = "filename"
        const val EXTRA_ISO_NAME = "iso_name"
        const val ACTION_DOWNLOAD_COMPLETE = "com.barrersoftware.isotool.DOWNLOAD_COMPLETE"
        const val ACTION_DOWNLOAD_FAILED = "com.barrersoftware.isotool.DOWNLOAD_FAILED"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ERROR = "error_message"
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val filename = intent.getStringExtra(EXTRA_FILENAME) ?: return START_NOT_STICKY
        val isoName = intent.getStringExtra(EXTRA_ISO_NAME) ?: filename
        
        val downloadDir = getExternalFilesDir(null)
        val destination = File(downloadDir, filename)
        
        startForeground(NOTIFICATION_ID, createNotification(isoName, 0, 0))
        
        serviceScope.launch {
            val downloader = IsoDownloader()
            downloader.downloadIso(url, destination) { downloaded, total ->
                updateNotification(isoName, downloaded, total)
            }.onSuccess { file ->
                notifyComplete(file, isoName)
                stopSelf()
            }.onFailure { error ->
                notifyFailure(error.message ?: "Unknown error", isoName)
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ISO Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of ISO file downloads"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(isoName: String, downloaded: Long, total: Long): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading $isoName")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
        
        if (total > 0) {
            val progress = ((downloaded * 100) / total).toInt()
            val downloadedMB = downloaded / 1024 / 1024
            val totalMB = total / 1024 / 1024
            builder.setContentText("${downloadedMB}MB / ${totalMB}MB")
                .setProgress(100, progress, false)
        } else {
            builder.setProgress(100, 0, true)
                .setContentText("Starting download...")
        }
        
        return builder.build()
    }
    
    private fun updateNotification(isoName: String, downloaded: Long, total: Long) {
        val notification = createNotification(isoName, downloaded, total)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
    
    private fun notifyComplete(file: File, isoName: String) {
        val intent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(EXTRA_FILE_PATH, file.absolutePath)
            putExtra(EXTRA_ISO_NAME, isoName)
        }
        sendBroadcast(intent)
        
        val completionNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("$isoName downloaded successfully")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        
        notificationManager?.notify(NOTIFICATION_ID + 1, completionNotification)
    }
    
    private fun notifyFailure(error: String, isoName: String) {
        val intent = Intent(ACTION_DOWNLOAD_FAILED).apply {
            putExtra(EXTRA_ERROR, error)
            putExtra(EXTRA_ISO_NAME, isoName)
        }
        sendBroadcast(intent)
        
        val errorNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Failed")
            .setContentText("$isoName: $error")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        
        notificationManager?.notify(NOTIFICATION_ID + 1, errorNotification)
    }
}
