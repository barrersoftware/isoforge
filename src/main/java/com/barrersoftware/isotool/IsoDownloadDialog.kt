package com.barrersoftware.isotool

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding

class IsoDownloadDialog(
    context: Context,
    private val templates: List<IsoTemplate>,
    private val onIsoSelected: (IsoTemplate) -> Unit
) : Dialog(context) {
    
    private var selectedCategory: String? = null
    private lateinit var isoListContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Title
        val title = TextView(context).apply {
            text = "📦 Download Recovery ISO"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)
        
        val subtitle = TextView(context).apply {
            text = "${templates.size} operating systems available"
            textSize = 14f
            setPadding(0, 0, 0, 24)
            alpha = 0.7f
        }
        layout.addView(subtitle)
        
        // Category filter
        val categoriesLabel = TextView(context).apply {
            text = "Filter by category:"
            textSize = 12f
            alpha = 0.7f
            setPadding(0, 0, 0, 12)
        }
        layout.addView(categoriesLabel)
        
        val categoryChips = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val chipScrollView = HorizontalScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(categoryChips)
        }
        layout.addView(chipScrollView)
        
        // Get unique categories with icons
        val categoryMap = mutableMapOf<String, String>()
        templates.forEach { template ->
            if (!categoryMap.containsKey(template.category)) {
                categoryMap[template.category] = template.categoryIcon
            }
        }
        val categories = categoryMap.keys.sorted()
        
        // Add "All" chip
        val allChip = Button(context).apply {
            text = "🌐 All (${templates.size})"
            textSize = 12f
            setPadding(24, 12, 24, 12)
            setOnClickListener {
                selectedCategory = null
                renderIsoList()
            }
        }
        categoryChips.addView(allChip)
        
        // Add category chips
        categories.forEach { category ->
            val count = templates.count { it.category == category }
            val icon = categoryMap[category] ?: ""
            val chip = Button(context).apply {
                text = "$icon $category ($count)"
                textSize = 12f
                setPadding(24, 12, 24, 12)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 0, 0)
                }
                setOnClickListener {
                    selectedCategory = category
                    renderIsoList()
                }
            }
            categoryChips.addView(chip)
        }
        
        // ISO list
        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                800
            )
        }
        
        isoListContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        scrollView.addView(isoListContainer)
        layout.addView(scrollView)
        
        // Initial render - show all
        renderIsoList()
        
        // Cancel button
        val cancelButton = Button(context).apply {
            text = "Cancel"
            setOnClickListener { dismiss() }
        }
        layout.addView(cancelButton)
        
        setContentView(layout)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun renderIsoList() {
        isoListContainer.removeAllViews()
        
        val filteredTemplates = if (selectedCategory == null) {
            templates
        } else {
            templates.filter { it.category == selectedCategory }
        }
        
        var currentCategory = ""
        
        filteredTemplates.forEach { template ->
            // Category header
            if (selectedCategory == null && template.category != currentCategory) {
                currentCategory = template.category
                val categoryHeader = TextView(context).apply {
                    text = template.category
                    textSize = 16f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 32, 0, 16)
                    setTextColor(context.getColor(android.R.color.holo_blue_dark))
                }
                isoListContainer.addView(categoryHeader)
            }
            
            // ISO item
            val isoButton = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32)
                background = context.getDrawable(android.R.drawable.dialog_holo_light_frame)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
                
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onIsoSelected(template)
                    dismiss()
                }
            }
            
            val nameText = TextView(context).apply {
                text = template.name
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(context.getColor(android.R.color.black))
            }
            isoButton.addView(nameText)
            
            val versionText = TextView(context).apply {
                text = "Version ${template.version}"
                textSize = 12f
                setPadding(0, 4, 0, 0)
                alpha = 0.7f
            }
            isoButton.addView(versionText)
            
            val sizeText = TextView(context).apply {
                val sizeGB = template.sizeBytes.toDouble() / 1024.0 / 1024.0 / 1024.0
                text = if (sizeGB >= 1.0) {
                    "Size: %.1f GB".format(sizeGB)
                } else {
                    val sizeMB = template.sizeBytes / 1024 / 1024
                    "Size: ${sizeMB} MB"
                }
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }
            isoButton.addView(sizeText)
            
            val notesText = TextView(context).apply {
                text = template.notes
                textSize = 12f
                setPadding(0, 4, 0, 0)
            }
            isoButton.addView(notesText)
            
            isoListContainer.addView(isoButton)
        }
    }
}
