package com.barrersoftware.isotool

import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystem

class VentoyDetector {
    
    /**
     * Check if a USB drive is formatted with Ventoy
     * Ventoy creates specific directories and files:
     * - /ventoy/ directory
     * - /grub/ directory  
     * - /EFI/BOOT/ directory
     */
    fun isVentoyDrive(device: UsbMassStorageDevice): Boolean {
        return try {
            device.init()
            val partition = device.partitions.firstOrNull() ?: return false
            val fs = partition.fileSystem
            val root = fs.rootDirectory
            
            // Check for Ventoy signature directories
            val hasVentoyDir = root.listFiles().any { it.name.equals("ventoy", ignoreCase = true) && it.isDirectory }
            val hasGrubDir = root.listFiles().any { it.name.equals("grub", ignoreCase = true) && it.isDirectory }
            
            hasVentoyDir || hasGrubDir
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the path where ISOs should be copied on a Ventoy drive
     * Ventoy looks for ISOs in the root or /iso/ directory
     */
    fun getVentoyIsoPath(fs: FileSystem): String {
        val root = fs.rootDirectory
        
        // Check if /iso/ directory exists
        val isoDir = root.listFiles().firstOrNull { 
            it.name.equals("iso", ignoreCase = true) && it.isDirectory 
        }
        
        return if (isoDir != null) {
            "/iso/"
        } else {
            "/" // Root directory
        }
    }
    
    /**
     * Calculate available space on Ventoy drive for new ISOs
     */
    fun getAvailableSpace(device: UsbMassStorageDevice): Long {
        return try {
            val partition = device.partitions.firstOrNull() ?: return 0
            val fs = partition.fileSystem
            fs.capacity - fs.occupiedSpace
        } catch (e: Exception) {
            0
        }
    }
}
