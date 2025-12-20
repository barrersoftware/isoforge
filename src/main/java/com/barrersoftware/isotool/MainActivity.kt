package com.barrersoftware.isotool

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var selectedIso: Uri? = null
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI setup here
        // Will implement Material Design 3 layout
        setupUI()
    }
    
    private fun setupUI() {
        // Simple KISS interface:
        // 1. Select ISO button
        // 2. Select Drive button  
        // 3. Write button
        // 4. Progress display
        // 5. Status text
    }
    
    private fun selectIso() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/x-iso9660-image"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_ISO)
    }
    
    companion object {
        const val REQUEST_ISO = 1
    }
}
