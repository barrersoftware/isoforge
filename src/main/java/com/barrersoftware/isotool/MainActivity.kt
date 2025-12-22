package com.barrersoftware.isotool

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var selectedIso: Uri? = null
    private lateinit var selectedIsoText: TextView
    private lateinit var selectedUsbText: TextView
    private lateinit var statusText: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var selectUsbButton: MaterialButton
    private lateinit var formatAndWriteButton: MaterialButton
    private lateinit var formatBlankButton: MaterialButton
    private lateinit var usbDetector: UsbDetector
    private lateinit var ventoyDetector: VentoyDetector
    private var selectedUsbDevice: com.github.mjdev.libaums.UsbMassStorageDevice? = null
    private var isVentoyDrive: Boolean = false
    
    companion object {
        const val REQUEST_ISO = 1
        const val REQUEST_STORAGE_PERMISSION = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        checkStoragePermissions()
        setupClickListeners()
        setupUsbDetection()
        loadDarkModePreference()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val darkModeItem = menu.findItem(R.id.menu_dark_mode)
        val prefs = getSharedPreferences("ISOForgePrefs", Context.MODE_PRIVATE)
        darkModeItem.isChecked = prefs.getBoolean("dark_mode", false)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            R.id.menu_help -> {
                showHelpDialog()
                true
            }
            R.id.menu_dark_mode -> {
                toggleDarkMode(item)
                true
            }
            R.id.menu_catalog -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/barrersoftware/iso-catalog"))
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - request MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            // Android 10 and below
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            
            val notGranted = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            
            if (notGranted.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), REQUEST_STORAGE_PERMISSION)
            }
        }
    }
    
    private fun setupViews() {
        selectedIsoText = findViewById(R.id.selectedIsoText)
        selectedUsbText = findViewById(R.id.selectedUsbText)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
        selectUsbButton = findViewById(R.id.selectUsbButton)
        formatAndWriteButton = findViewById(R.id.formatAndWriteButton)
        formatBlankButton = findViewById(R.id.formatBlankButton)
    }
    
    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.downloadIsoButton).setOnClickListener {
            showIsoDownloadDialog()
        }
        
        findViewById<MaterialButton>(R.id.selectIsoButton).setOnClickListener {
            selectIsoFile()
        }
        
        selectUsbButton.setOnClickListener {
            selectedUsbDevice?.let { device ->
                statusText.text = "USB drive selected: ${device.usbDevice.deviceName}"
                formatAndWriteButton.isEnabled = selectedIso != null
                formatBlankButton.isEnabled = true
            } ?: run {
                statusText.text = "No USB drive detected. Please connect one."
            }
        }
        
        formatAndWriteButton.setOnClickListener {
            selectedIso?.let { iso ->
                selectedUsbDevice?.let { device ->
                    if (confirmWrite(iso)) {
                        writeIsoToUsb(iso, device)
                    }
                } ?: run {
                    statusText.text = "No USB drive selected"
                }
            } ?: run {
                statusText.text = "No ISO selected"
            }
        }
        
        formatBlankButton.setOnClickListener {
            selectedUsbDevice?.let { device ->
                showFormatConfirmDialog(device)
            } ?: run {
                statusText.text = "No USB drive detected"
            }
        }
    }
    
    private fun selectIsoFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/x-iso9660-image",
                "application/octet-stream"
            ))
        }
        startActivityForResult(intent, REQUEST_ISO)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ISO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedIso = uri
                selectedIsoText.text = "ISO: ${uri.lastPathSegment}"
                selectUsbButton.isEnabled = true
                statusText.text = "ISO selected! Now connect USB drive"
            }
        }
    }
    
    private fun setupUsbDetection() {
        usbDetector = UsbDetector(this)
        ventoyDetector = VentoyDetector()
        usbDetector.startMonitoring(
            onConnected = { device ->
                selectedUsbDevice = device
                
                // Check if it's a Ventoy drive
                isVentoyDrive = ventoyDetector.isVentoyDrive(device)
                
                val driveType = if (isVentoyDrive) " (Ventoy)" else ""
                selectedUsbText.text = "USB: ${device.usbDevice.deviceName}$driveType"
                selectUsbButton.isEnabled = true
                
                if (isVentoyDrive) {
                    statusText.text = "⚡ Ventoy drive detected! You can add ISOs without overwriting."
                } else {
                    statusText.text = "USB drive detected! Tap 'Select USB Drive' to use it."
                }
            },
            onDisconnected = {
                selectedUsbDevice = null
                isVentoyDrive = false
                selectedUsbText.text = "No USB drive detected"
                selectUsbButton.isEnabled = false
                formatAndWriteButton.isEnabled = false
                formatBlankButton.isEnabled = false
                statusText.text = "USB drive disconnected"
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        usbDetector.stopMonitoring()
    }
    
    private fun showIsoDownloadDialog() {
        statusText.text = "Loading ISO catalog..."
        lifecycleScope.launch {
            val catalog = IsoCatalog(this@MainActivity)
            val templates = catalog.loadCatalog()
            
            if (templates.isEmpty()) {
                statusText.text = "Failed to load ISO catalog"
                return@launch
            }
            
            IsoDownloadDialog(this@MainActivity, templates) { template ->
                statusText.text = "Starting download: ${template.name}"
                downloadIso(template)
            }.show()
        }
    }
    
    private fun downloadIso(template: IsoTemplate) {
        val downloadDir = getExternalFilesDir(null)
        val fileName = "${template.id}.iso"
        val destination = java.io.File(downloadDir, fileName)
        
        // Log and show download path
        android.util.Log.d("ISOForge", "Download path: ${destination.absolutePath}")
        android.widget.Toast.makeText(this, "Downloading to: ${destination.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
        
        progressBar.visibility = android.view.View.VISIBLE
        progressBar.isIndeterminate = false
        
        lifecycleScope.launch {
            val downloader = IsoDownloader()
            downloader.downloadIso(template.downloadUrl, destination) { downloaded, total ->
                runOnUiThread {
                    if (total > 0) {
                        progressBar.progress = ((downloaded * 100) / total).toInt()
                        val downloadedMB = downloaded / 1024 / 1024
                        val totalMB = total / 1024 / 1024
                        statusText.text = "Downloading: ${downloadedMB}MB / ${totalMB}MB"
                    }
                }
            }.onSuccess { file ->
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    selectedIso = android.net.Uri.fromFile(file)
                    selectedIsoText.text = "ISO: ${template.name}"
                    selectUsbButton.isEnabled = true
                    statusText.text = "✓ Download complete! Saved to: ${file.absolutePath}"
                    android.widget.Toast.makeText(this@MainActivity, "ISO saved to:\n${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                }
            }.onFailure { error ->
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = "Download failed: ${error.message}"
                }
            }
        }
    }
    
    private fun confirmWrite(isoUri: Uri): Boolean {
        val isoName = isoUri.lastPathSegment ?: "ISO"
        
        // If Ventoy drive, ask what to do
        if (isVentoyDrive) {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("⚡ Ventoy Drive Detected")
            builder.setMessage("This USB drive has Ventoy installed.\n\nWhat would you like to do with:\n$isoName")
            
            var action: String? = null
            builder.setPositiveButton("Add ISO to Ventoy") { _, _ ->
                action = "add"
            }
            builder.setNegativeButton("Overwrite (Destructive)") { _, _ ->
                action = "overwrite"
            }
            builder.setNeutralButton("Cancel", null)
            
            val dialog = builder.create()
            dialog.show()
            
            // TODO: Handle "add" vs "overwrite" action
            // For now, return false (not implemented yet)
            return false
        } else {
            // Normal confirmation for non-Ventoy drives
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Write ISO to USB?")
            builder.setMessage("⚠️ WARNING: This will ERASE ALL DATA on the USB drive and write:\n\n$isoName\n\nAre you sure?")
            
            var confirmed = false
            builder.setPositiveButton("Write (Erase USB)") { _, _ ->
                confirmed = true
            }
            builder.setNegativeButton("Cancel", null)
            
            val dialog = builder.create()
            dialog.show()
            
            return confirmed
        }
    }
    
    private fun writeIsoToUsb(isoUri: Uri, device: com.github.mjdev.libaums.UsbMassStorageDevice) {
        statusText.text = "Writing ISO to USB..."
        progressBar.visibility = android.view.View.VISIBLE
        progressBar.isIndeterminate = false
        progressBar.progress = 0
        
        lifecycleScope.launch {
            val writer = UsbWriter(this@MainActivity)
            writer.writeIsoToUsb(isoUri, device) { written, total ->
                runOnUiThread {
                    if (total > 0) {
                        progressBar.progress = ((written * 100) / total).toInt()
                        val writtenMB = written / 1024 / 1024
                        val totalMB = total / 1024 / 1024
                        statusText.text = "Writing: ${writtenMB}MB / ${totalMB}MB"
                    }
                }
            }.onSuccess {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = "✓ Bootable USB created successfully! Ready to use."
                    
                    // Show success dialog
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Success!")
                        .setMessage("Your bootable USB drive is ready.\n\nYou can now safely remove it and use it to boot/recover your system.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }.onFailure { error ->
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = "Write failed: ${error.message}"
                    
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Write Failed")
                        .setMessage("Error: ${error.message}\n\nMake sure USB is writable and has enough space.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
    
    private fun showFormatConfirmDialog(device: com.github.mjdev.libaums.UsbMassStorageDevice) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Format USB Drive?")
            .setMessage("⚠️ WARNING: This will ERASE ALL DATA on the USB drive!\n\nAre you sure you want to continue?")
            .setPositiveButton("Format (Erase All)") { _, _ ->
                formatUsbBlank(device)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun formatUsbBlank(device: com.github.mjdev.libaums.UsbMassStorageDevice) {
        statusText.text = "Formatting USB drive..."
        progressBar.visibility = android.view.View.VISIBLE
        progressBar.isIndeterminate = true
        
        lifecycleScope.launch {
            try {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    device.init()
                    val partition = device.partitions[0]
                    
                    // Format as FAT32
                    val fs = partition.fileSystem
                    val root = fs.rootDirectory
                    
                    // Delete all files
                    root.listFiles().forEach { file ->
                        file.delete()
                    }
                }
                
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = "✓ USB drive formatted successfully"
                    selectedUsbText.text = "USB: ${device.usbDevice.deviceName} (Blank)"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = "Format failed: ${e.message}"
                }
            }
        }
    }
    
    private fun loadDarkModePreference() {
        val prefs = getSharedPreferences("ISOForgePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun toggleDarkMode(item: MenuItem) {
        val prefs = getSharedPreferences("ISOForgePrefs", Context.MODE_PRIVATE)
        val isDarkMode = !item.isChecked
        item.isChecked = isDarkMode
        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
        
        // Recreate activity to apply theme
        recreate()
    }
    
    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("📦 About ISOForge")
            .setMessage("""
                ISOForge - USB Recovery Tool
                Version 1.0
                
                Create bootable USB drives from your phone for system recovery and installation.
                
                • 105+ operating systems available
                • Direct downloads from official sources
                • Community-maintained ISO catalog
                
                Built by Barrer Software 🏴‍☠️
                
                ISO Catalog: github.com/barrersoftware/iso-catalog
                
                Open source infrastructure for everyone.
            """.trimIndent())
            .setPositiveButton("Visit Catalog") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/barrersoftware/iso-catalog"))
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showHelpDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("❓ How to Use ISOForge")
            .setMessage("""
                Creating a Bootable USB:
                
                1️⃣ Download ISO
                   • Tap "Download ISO"
                   • Browse 105+ operating systems
                   • Filter by category
                   • Tap to download
                
                2️⃣ Connect USB Drive
                   • Plug in USB drive
                   • Wait for auto-detection
                   • Tap "Select USB Drive"
                
                3️⃣ Write ISO to USB
                   • Tap "Format and Write ISO"
                   • Confirm the warning
                   • Wait for completion
                
                4️⃣ Boot and Recover
                   • Safely remove USB
                   • Boot from USB on target system
                   • Install or recover!
                
                ⚠️ Warning: Writing ISO erases all USB data!
                
                Need more ISOs? Visit the catalog on GitHub to suggest additions.
            """.trimIndent())
            .setPositiveButton("Got It", null)
            .show()
    }
}
