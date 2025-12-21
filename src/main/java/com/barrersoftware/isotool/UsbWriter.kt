package com.barrersoftware.isotool

import android.content.Context
import android.net.Uri
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class UsbWriter(private val context: Context) {
    
    suspend fun writeIsoToUsb(
        isoUri: Uri, 
        device: UsbMassStorageDevice,
        progressCallback: (Long, Long) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Initialize device
            device.init()
            
            // Open ISO file
            val inputStream: InputStream = context.contentResolver.openInputStream(isoUri) 
                ?: return@withContext Result.failure(Exception("Cannot open ISO file"))
            
            val isoSize = context.contentResolver.query(isoUri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: 0L
            
            // Get block device for raw write - use partition instead
            val partition = device.partitions[0]
            val blockSize = 512 // Standard sector size
            
            // Write ISO to USB in blocks
            val buffer = ByteArray(1024 * 1024) // 1MB chunks
            var totalBytesWritten = 0L
            var bytesRead: Int
            
            // Simple file-by-file copy (libaums limitation)
            val fs = partition.fileSystem
            val root = fs.rootDirectory
            
            // Create ISO file on USB
            val isoFileName = isoUri.lastPathSegment ?: "bootable.iso"
            val outputFile = root.createFile(isoFileName)
            val outputStream = com.github.mjdev.libaums.fs.UsbFileOutputStream(outputFile)
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesWritten += bytesRead.toLong()
                progressCallback(totalBytesWritten, isoSize)
            }
            
            outputStream.close()
            
            inputStream.close()
            device.close()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
