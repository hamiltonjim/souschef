package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.config.Preferences

class LocaleStringsTest {

    @Test
    fun `get succeeds`() {
        Preferences.locale = "en_US"
        Preferences.loadLanguageStrings(force = true)

        assertAll(
            { assertEquals("English", Preferences.getLanguageString("language")) },
            { assertEquals("en_US", Preferences.getLanguageString("locale")) },
            { assertEquals("valid", Preferences.getLanguageString("valid")) },
            { assertThrows<IllegalStateException> { Preferences.getLanguageString("invalid")  }},
            { assertTrue("test" in Preferences.getLanguageArray("testList")) },
        )
    }

    @Test
    fun `get fails`() {
        Preferences.locale = "nothing"   // will fail
        Preferences.loadLanguageStrings(force = true)

        assertAll(
            { assertThrows<IllegalStateException> { Preferences.getLanguageString("language") } },
            { assertThrows<IllegalStateException> { Preferences.getLanguageString("locale") } },
            { assertThrows<IllegalStateException> { Preferences.getLanguageArray("missing") } },
        )
    }
}