package com.barrersoftware.isotool
import android.content.Context
import android.hardware.usb.UsbManager
import com.github.mjdev.libaums.UsbMassStorageDevice
class DriveDetector(private val context: Context) {
    fun detectUsbDrives(): List<UsbMassStorageDevice> {
        return UsbMassStorageDevice.getMassStorageDevices(context).toList()
    }
    fun getDriveInfo(device: UsbMassStorageDevice): String {
        val partition = device.partitions.firstOrNull()
        val sizeGB = partition?.let { it.capacity / (1024 * 1024 * 1024) } ?: 0
        return "${device.usbDevice.productName} (${sizeGB}GB)"
    }
}
