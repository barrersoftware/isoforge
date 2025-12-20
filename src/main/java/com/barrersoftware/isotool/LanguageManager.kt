package com.barrersoftware.isotool

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

class LanguageManager(private val context: Context) {
    
    companion object {
        const val PREF_LANGUAGE = "app_language"
    }
    
    data class Language(
        val code: String,
        val displayName: String
    )
    
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English"),
            Language("es", "Español"),
            Language("fr", "Français"),
            Language("de", "Deutsch"),
            Language("pt", "Português")
        )
    }
    
    fun getCurrentLanguage(): String {
        val prefs = context.getSharedPreferences("ISOForge", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, "en") ?: "en"
    }
    
    fun setLanguage(languageCode: String) {
        val prefs = context.getSharedPreferences("ISOForge", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, languageCode).apply()
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getLanguageDisplayName(code: String): String {
        return when(code) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "pt" -> "Português"
            else -> "English"
        }
    }
}
