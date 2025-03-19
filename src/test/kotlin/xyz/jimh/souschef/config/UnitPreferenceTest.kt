package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.junit.jupiter.api.Test

class UnitPreferenceTest {

    @Test
    fun getLabel() {
        UnitPreference.values().forEach {
            assertEquals(it.name.toTitleCase(), it.getLabel())
        }
    }
}

fun String.toTitleCase(): String =
    this.toLowerCaseAsciiOnly().capitalizeAsciiOnly()