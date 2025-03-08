package xyz.jimh.souschef.config

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.InjectMocks
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao

@ExtendWith(MockKExtension::class)
class PreferencesTest {

    @MockK
    private lateinit var applicationContext: ApplicationContext

    @MockK
    private lateinit var context: SpringContext

    @MockK
    private lateinit var request: HttpServletRequest

    @MockK
    lateinit var preferenceDao: PreferenceDao

    @InjectMocks
    var preferences = Preferences

    @Test
    fun initHtml() {
        val html = Preferences.initHtml().get()
        // confirm that all parts are there
        val styleStart = html.indexOf("<style>")
        val styleEnd = html.indexOf("</style>")
        val scriptStart = html.indexOf("<script type=\"text/javascript\">")
        val scriptEnd = html.indexOf("</script>")

        Assertions.assertAll(
            Executable { Assertions.assertTrue(html.contains("<body onload=\"setSelects()\">"), "body") },
            Executable { Assertions.assertTrue(html.contains("<div id=\"preferences\">"), "footer") },
            Executable { Assertions.assertTrue(styleStart >= 0, "style start") },
            Executable { Assertions.assertTrue(styleEnd > styleStart, "style end after start") },
            Executable { Assertions.assertTrue(scriptStart >= 0, "script start") },
            Executable { Assertions.assertTrue(scriptEnd > scriptStart, "script end after start") },
            Executable { Assertions.assertTrue(html.contains("<head>"), "head exists") },
        )
    }

    @Test
    fun getPreferenceValues() {
        setupContext()
        every { preferenceDao.findAllByHost(any()) } returns
                listOf(
                    Preference("host", "foo", "foo"),
                    Preference("host", "bar", "bar"),
                    Preference("host", "baz", "baz"),
                    )

        every { request.remoteHost } returns "localhost"

        val prefs = preferences.getPreferenceValues(request)
        Assertions.assertNotNull(prefs)
        Assertions.assertAll(
            Executable { Assertions.assertNotNull(prefs.body) },
            Executable { Assertions.assertEquals("foo", prefs.body!!["foo"]) },
            Executable { Assertions.assertEquals("bar", prefs.body!!["bar"]) },
            Executable { Assertions.assertEquals("baz", prefs.body!!["baz"]) },
        )
    }

    @Test
    fun setPreferenceValue() {
        setupContext()
    }

    @Test
    fun getPreference() {
    }

    @Test
    fun getUnitTypes() {
    }

    @Test
    fun getUnitNames() {
    }

    @Test
    fun addScripts() {
    }

    private fun setupContext() {
        every { context.setApplicationContext(any()) } answers { callOriginal() }
        context.setApplicationContext(applicationContext)
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
        }
    }
}