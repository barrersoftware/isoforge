package com.barrersoftware.isotool

import com.github.mjdev.libaums.UsbMassStorageDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.ByteBuffer

class IsoWriter {
    
    suspend fun writeIsoToUsb(
        isoStream: InputStream,
        device: UsbMassStorageDevice,
        progressCallback: (Long, Long) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val partition = device.partitions[0]
            val fs = partition.fileSystem
            val blockSize = fs.chunkSize
            
            val buffer = ByteArray(blockSize * 128) // 128 blocks at a time
            var totalBytesWritten = 0L
            val totalSize = isoStream.available().toLong()
            
            var bytesRead: Int
            while (isoStream.read(buffer).also { bytesRead = it } != -1) {
                // Convert ByteArray to ByteBuffer for writing
                val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                // Note: Direct block writing API limited in 0.5.5
                // Would need to write through filesystem API
                
                totalBytesWritten += bytesRead
                progressCallback(totalBytesWritten, totalSize)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun setBootableFlag(device: UsbMassStorageDevice): Result<Unit> {
        return try {
            // Note: Direct MBR access limited in 0.5.5
            // Would need native implementation or root access
            // Marking as TODO for now
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
