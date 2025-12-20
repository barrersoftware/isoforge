package com.barrersoftware.isotool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.github.mjdev.libaums.UsbMassStorageDevice

class UsbDetector(private val context: Context) {
    
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var onDeviceConnected: ((UsbMassStorageDevice) -> Unit)? = null
    private var onDeviceDisconnected: (() -> Unit)? = null
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    checkForStorageDevices()
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    onDeviceDisconnected?.invoke()
                }
            }
        }
    }
    
    fun startMonitoring(
        onConnected: (UsbMassStorageDevice) -> Unit,
        onDisconnected: () -> Unit
    ) {
        this.onDeviceConnected = onConnected
        this.onDeviceDisconnected = onDisconnected
        
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbReceiver, filter)
        
        // Check for already connected devices
        checkForStorageDevices()
    }
    
    fun stopMonitoring() {
        context.unregisterReceiver(usbReceiver)
    }
    
    private fun checkForStorageDevices() {
        val devices = UsbMassStorageDevice.getMassStorageDevices(context)
        if (devices.isNotEmpty()) {
            onDeviceConnected?.invoke(devices[0])
        }
    }
    
    fun getConnectedDevices(): List<UsbMassStorageDevice> {
        return UsbMassStorageDevice.getMassStorageDevices(context).toList()
    }
}
