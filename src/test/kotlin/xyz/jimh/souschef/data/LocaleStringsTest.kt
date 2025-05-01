package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.config.Preferences

class LocaleStringsTest {

    @Test
    fun `get succeeds`() {
        Preferences.locale = "en_US"
        Preferences.loadLanguageStrings()

        assertAll(
            Executable { assertEquals("English", Preferences.languageStrings.get("language")) },
            Executable { assertEquals("en_US", Preferences.languageStrings.get("locale")) },
        )
    }

    @Test
    fun `get fails`() {
        Preferences.locale = "nothing"   // will fail
        Preferences.loadLanguageStrings()

        assertAll(
            Executable { assertThrows<IllegalStateException> { Preferences.languageStrings.get("language") } },
            Executable { assertThrows<IllegalStateException> { Preferences.languageStrings.get("locale") } },
        )
    }
}