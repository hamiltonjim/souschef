package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.config.Preferences

class LocaleStringsTest {

    @Test
    fun `get succeeds`() {
        Preferences.locale = "en_US"
        Preferences.loadLanguageStrings(force = true)

        assertAll(
            Executable { assertEquals("English", Preferences.getLanguageString("language")) },
            Executable { assertEquals("en_US", Preferences.getLanguageString("locale")) },
            Executable { assertTrue("test" in Preferences.getLanguageArray("testList")) },
        )
    }

    @Test
    fun `get fails`() {
        Preferences.locale = "nothing"   // will fail
        Preferences.loadLanguageStrings(force = true)

        assertAll(
            Executable { assertThrows<IllegalStateException> { Preferences.getLanguageString("language") } },
            Executable { assertThrows<IllegalStateException> { Preferences.getLanguageString("locale") } },
            Executable { assertThrows<IllegalStateException> { Preferences.getLanguageArray("missing") } },
        )
    }
}