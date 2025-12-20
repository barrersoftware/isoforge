package com.barrersoftware.isotool

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

data class IsoManifestEntry(
    val name: String,
    val description: String,
    val url: String,
    val size: Long,
    val category: String,
    val version: String?
)

class ManifestFetcher {
    companion object {
        private const val CLEANVM_MANIFEST_URL = "https://cleanvm.barrersoftware.com/manifest.json"
    }
    
    suspend fun fetchManifest(): List<IsoManifestEntry> = withContext(Dispatchers.IO) {
        try {
            val json = URL(CLEANVM_MANIFEST_URL).readText()
            val jsonArray = JSONArray(json)
            
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                IsoManifestEntry(
                    name = obj.getString("name"),
                    description = obj.getString("description"),
                    url = obj.getString("url"),
                    size = obj.getLong("size"),
                    category = obj.getString("category"),
                    version = obj.optString("version", null)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getCategorizedManifest(entries: List<IsoManifestEntry>): Map<String, List<IsoManifestEntry>> {
        return entries.groupBy { it.category }
    }
}
