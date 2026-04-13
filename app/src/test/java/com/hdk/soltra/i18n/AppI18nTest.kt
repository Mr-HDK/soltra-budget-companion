package com.hdk.soltra.i18n

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppI18nTest {
    @Test
    fun `resolveAppLanguage prioritizes supported forced language`() {
        val language = resolveAppLanguage(forcedLanguageTag = "en-US", systemLocale = Locale.FRENCH)

        assertEquals(AppLanguage.EN, language)
    }

    @Test
    fun `resolveAppLanguage falls back to system locale when forced tag unsupported`() {
        val language = resolveAppLanguage(
            forcedLanguageTag = "es-ES",
            systemLocale = Locale.forLanguageTag("en-US"),
        )

        assertEquals(AppLanguage.EN, language)
    }

    @Test
    fun `resolveAppLanguage uses app fallback when system locale unsupported`() {
        val language = resolveAppLanguage(
            forcedLanguageTag = null,
            systemLocale = Locale.forLanguageTag("es-ES"),
        )

        assertEquals(AppLanguage.EN, language)
    }

    @Test
    fun `AppStrings returns translated value for known key`() {
        val english = AppStrings(AppLanguage.EN)

        assertEquals("Settings", english.get(AppTextKey.ROOT_TAB_SETTINGS))
    }

    @Test
    fun `AppLanguagePreference fromStorage falls back to system`() {
        assertEquals(AppLanguagePreference.SYSTEM, AppLanguagePreference.fromStorage("unknown"))
    }

    @Test
    fun `resolveLocale uses forced locale when preference is explicit`() {
        val resolved = resolveLocale(
            preference = AppLanguagePreference.EN,
            systemLocale = Locale.FRENCH,
        )

        assertEquals("en", resolved.language)
    }
}
