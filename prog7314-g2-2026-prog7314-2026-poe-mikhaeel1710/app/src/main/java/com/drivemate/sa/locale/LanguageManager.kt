package com.drivemate.sa.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/** A language the app can be switched to, plus its own display name (in its own language). */
data class AppLanguage(val tag: String, val displayName: String)

/**
 * Thin wrapper around AppCompatDelegate's per-app language storage.
 *
 * AppCompatDelegate.setApplicationLocales() persists the choice itself
 * (via the framework LocaleManager on API 33+, and via AppCompat's own
 * storage below that) - no SharedPreferences/DataStore needed here.
 */
object LanguageManager {

    // Add a tag here once you've added a matching values-<tag>/strings.xml.
    val supportedLanguages = listOf(
        AppLanguage(tag = "en-GB", displayName = "English (UK)"),
        AppLanguage(tag = "af", displayName = "Afrikaans"),
        AppLanguage(tag = "zu", displayName = "isiZulu")
    )

    fun currentLanguageTag(): String {
        val applied = AppCompatDelegate.getApplicationLocales()
        if (!applied.isEmpty) return applied[0]?.toLanguageTag() ?: "en-GB"
        return "en-GB" // no explicit choice yet -> following the system locale
    }

    fun currentLanguageDisplayName(): String {
        val tag = currentLanguageTag()
        return supportedLanguages.firstOrNull { it.tag == tag }?.displayName
            ?: Locale.forLanguageTag(tag).getDisplayName(Locale.forLanguageTag(tag))
    }

    fun setLanguage(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}