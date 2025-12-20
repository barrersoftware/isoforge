package com.barrersoftware.isotool

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.github.mjdev.libaums.UsbMassStorageDevice

class UsbSelectionManager(private val context: Context) {
    
    fun selectDevice(
        devices: List<UsbMassStorageDevice>,
        onSelected: (UsbMassStorageDevice) -> Unit
    ) {
        when {
            devices.isEmpty() -> {
                // No devices
                showNoDeviceDialog()
            }
            devices.size == 1 -> {
                // Single device - auto-select!
                onSelected(devices[0])
            }
            else -> {
                // Multiple devices - show selection
                showSelectionDialog(devices, onSelected)
            }
        }
    }
    
    private fun showNoDeviceDialog() {
        AlertDialog.Builder(context)
            .setTitle("No USB Drive Detected")
            .setMessage("Please connect a USB drive to your phone")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSelectionDialog(
        devices: List<UsbMassStorageDevice>,
        onSelected: (UsbMassStorageDevice) -> Unit
    ) {
        val deviceNames = devices.map { device ->
            val partition = device.partitions.firstOrNull()
            val sizeGB = partition?.let { 
                it.fileSystem.capacity / (1024 * 1024 * 1024) 
            } ?: 0
            "${device.usbDevice.productName} (${sizeGB}GB)"
        }.toTypedArray()
        
        AlertDialog.Builder(context)
            .setTitle("Select USB Drive")
            .setItems(deviceNames) { _, which ->
                onSelected(devices[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    fun getDeviceInfo(device: UsbMassStorageDevice): String {
        val partition = device.partitions.firstOrNull()
        val sizeGB = partition?.let { it.fileSystem.capacity / (1024 * 1024 * 1024) } ?: 0
        return "${device.usbDevice.productName} (${sizeGB}GB)"
    }
}
