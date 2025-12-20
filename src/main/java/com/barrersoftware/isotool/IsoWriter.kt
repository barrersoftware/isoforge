package com.barrersoftware.isotool

import com.github.mjdev.libaums.UsbMassStorageDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class IsoWriter {
    
    suspend fun writeIsoToUsb(
        isoStream: InputStream,
        device: UsbMassStorageDevice,
        progressCallback: (Long, Long) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val partition = device.partitions[0]
            val blockDevice = partition.blockDevice
            val blockSize = blockDevice.blockSize
            
            val buffer = ByteArray(blockSize * 128) // 128 blocks at a time
            var totalBytesWritten = 0L
            val totalSize = isoStream.available().toLong()
            var currentBlock = 0
            
            var bytesRead: Int
            while (isoStream.read(buffer).also { bytesRead = it } != -1) {
                // Write buffer to block device
                blockDevice.write(currentBlock.toLong(), buffer.copyOf(bytesRead))
                
                totalBytesWritten += bytesRead
                currentBlock += (bytesRead / blockSize)
                
                progressCallback(totalBytesWritten, totalSize)
            }
            
            // Flush and sync
            blockDevice.close()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun setBootableFlag(device: UsbMassStorageDevice): Result<Unit> {
        return try {
            // Write boot flag to MBR
            // Byte 510-511 should be 0x55AA
            // Partition 1 boot flag at byte 446 should be 0x80
            val partition = device.partitions[0]
            val blockDevice = partition.blockDevice
            
            // Read MBR (first 512 bytes)
            val mbr = ByteArray(512)
            blockDevice.read(0, mbr)
            
            // Set bootable flag on first partition
            mbr[446] = 0x80.toByte()
            
            // Ensure boot signature
            mbr[510] = 0x55.toByte()
            mbr[511] = 0xAA.toByte()
            
            // Write MBR back
            blockDevice.write(0, mbr)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
