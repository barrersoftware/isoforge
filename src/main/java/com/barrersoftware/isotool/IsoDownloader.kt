package com.barrersoftware.isotool

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class IsoDownloader {
    
    suspend fun downloadIso(
        url: String,
        destination: File,
        progressCallback: (Long, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            val totalSize = connection.contentLengthLong
            
            connection.getInputStream().use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        progressCallback(totalBytesRead, totalSize)
                    }
                }
            }
            
            Result.success(destination)
        } catch (e: Exception) {
            destination.delete()
            Result.failure(e)
        }
    }
}
