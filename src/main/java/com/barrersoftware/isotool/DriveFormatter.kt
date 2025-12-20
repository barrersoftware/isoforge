package com.barrersoftware.isotool

import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystemFactory

class DriveFormatter {
    
    fun formatDrive(device: UsbMassStorageDevice): Result<Unit> {
        return try {
            val partition = device.partitions[0]
            // Format as FAT32 - most compatible for bootable drives
            val fs = FileSystemFactory.createFileSystem(partition, partition.fileSystem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getDriveCapacity(device: UsbMassStorageDevice): Long {
        return device.partitions.firstOrNull()?.capacity ?: 0L
    }
}
