package com.barrersoftware.isotool

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var selectedIso: Uri? = null
    private lateinit var selectedIsoText: TextView
    private lateinit var selectedUsbText: TextView
    private lateinit var statusText: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var selectUsbButton: MaterialButton
    private lateinit var formatAndWriteButton: MaterialButton
    private lateinit var formatBlankButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        setupClickListeners()
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
            // TODO: Open download dialog with CleanVM manifest
            statusText.text = "Download feature coming soon!"
        }
        
        findViewById<MaterialButton>(R.id.selectIsoButton).setOnClickListener {
            selectIsoFile()
        }
        
        selectUsbButton.setOnClickListener {
            // TODO: Show USB drive selection dialog
            statusText.text = "USB detection coming soon!"
        }
        
        formatAndWriteButton.setOnClickListener {
            // TODO: Confirm and start write operation
            statusText.text = "Write operation coming soon!"
        }
        
        formatBlankButton.setOnClickListener {
            // TODO: Confirm and format blank
            statusText.text = "Format operation coming soon!"
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
    
    companion object {
        const val REQUEST_ISO = 1
    }
}
