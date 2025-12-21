package com.barrersoftware.isotool

import org.json.JSONObject
import org.json.JSONArray
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class IsoTemplate(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val category: String,
    val categoryIcon: String,
    val notes: String
)

class IsoCatalog(private val context: Context) {
    
    private val catalogUrl = "https://raw.githubusercontent.com/barrersoftware/iso-catalog/main/catalog.json"
    
    suspend fun loadCatalog(): List<IsoTemplate> = withContext(Dispatchers.IO) {
        val templates = mutableListOf<IsoTemplate>()
        
        try {
            // Try to fetch from GitHub first
            val json = try {
                fetchCatalogFromGitHub()
            } catch (e: Exception) {
                // Fall back to bundled version
                context.resources.openRawResource(R.raw.iso_catalog).bufferedReader().use { it.readText() }
            }
            
            parseCatalog(json, templates)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext templates
    }
    
    private fun fetchCatalogFromGitHub(): String {
        val connection = java.net.URL(catalogUrl).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        return connection.getInputStream().bufferedReader().use { it.readText() }
    }
    
    private fun parseCatalog(json: String, templates: MutableList<IsoTemplate>) {
        try {
            val catalog = JSONObject(json)
            val categories = catalog.getJSONArray("categories")
            
            for (i in 0 until categories.length()) {
                val category = categories.getJSONObject(i)
                val categoryName = category.getString("name")
                val categoryIcon = category.optString("icon", "")
                val categoryTemplates = category.getJSONArray("templates")
                
                for (j in 0 until categoryTemplates.length()) {
                    val template = categoryTemplates.getJSONObject(j)
                    templates.add(
                        IsoTemplate(
                            id = template.getString("id"),
                            name = template.getString("name"),
                            description = template.getString("description"),
                            version = template.getString("version"),
                            downloadUrl = template.getString("downloadUrl"),
                            sizeBytes = template.getLong("sizeBytes"),
                            category = categoryName,
                            categoryIcon = categoryIcon,
                            notes = template.getString("notes")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
