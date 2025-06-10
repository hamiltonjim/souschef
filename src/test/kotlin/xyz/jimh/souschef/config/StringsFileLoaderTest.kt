/*
 * Copyright (c) 2025 Jim Hamilton. All rights reserved.
 */

package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import xyz.jimh.souschef.data.LocaleStrings

class StringsFileLoaderTest {

    private val resolver = PathMatchingResourcePatternResolver()
    private val localeEn = "en_US"
    private val localeEs = "es_US"

    @Test
    fun `test load from resource`() {
        val stringsResource = resolver.getResource("classpath:/static/$localeEs/strings")
        val languageStrings = LocaleStrings.from(stringsResource)

        assertAll(
            { assertEquals("es_US", languageStrings.get("locale")) },
            { assertEquals("español", languageStrings.get("language")) },
        )
    }

    @Test
    fun `test limited load from resource`() {
        val stringsResource = resolver.getResource("classpath:/static/$localeEn/strings")
        val languageStrings = StringsFileLoader().load(stringsResource, 2)

        assertAll(
            { assertEquals("en_US", languageStrings.strings["locale"]) },
            { assertEquals("English", languageStrings.strings["language"]) },
            { assertNull(languageStrings.strings["Categories"]) }
        )
    }

}