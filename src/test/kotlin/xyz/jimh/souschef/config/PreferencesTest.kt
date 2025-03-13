package xyz.jimh.souschef.config

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao

@ExtendWith(MockKExtension::class)
class PreferencesTest {

    private lateinit var applicationContext: ApplicationContext
    private lateinit var context: SpringContext
    private lateinit var request: HttpServletRequest
    private lateinit var preferenceDao: PreferenceDao
    private var preferences = Preferences

    @BeforeEach
    fun setup() {
        preferenceDao = mockk(relaxed = true)
        applicationContext = mockk(relaxed = true)
        request = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { preferenceDao.findAllByHost(any()) } returns
                listOf(
                    Preference("host", "foo", "foo"),
                    Preference("host", "bar", "bar"),
                    Preference("host", "baz", "baz"),
                )

        every { request.remoteHost } returns "localhost"

        every { context.setApplicationContext(any()) } answers { callOriginal() }
        context.setApplicationContext(applicationContext)
        preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
    }

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
        val prefs = preferences.getPreferenceValues(request)
        Assertions.assertNotNull(prefs)
        Assertions.assertAll(
            Executable { Assertions.assertNotNull(prefs.body) },
            Executable { Assertions.assertEquals("foo", prefs.body!!["foo"]) },
            Executable { Assertions.assertEquals("bar", prefs.body!!["bar"]) },
            Executable { Assertions.assertEquals("baz", prefs.body!!["baz"]) },
        )

        verify(exactly = 1) { preferenceDao.findAllByHost("localhost") }
        verify { request.getRemoteHost() }
        verify { context.setApplicationContext(any()) }
        confirmVerified(preferenceDao, applicationContext, request, context)
    }

    @Test
    fun getPreference() {
        every { preferenceDao.findByHostAndKey("host", "key") } returns
                Optional.of(Preference("host", "key", "key_host"))
        every { preferenceDao.findByHostAndKey("localhost", "other") } returns
                Optional.of(Preference("localhost", "other", "other_localhost"))

        Assertions.assertAll(
            Executable { Assertions.assertEquals(
                "key_host",
                preferences.getPreference("host", "key")
            ) },
            Executable { Assertions.assertEquals(
                "other_localhost",
                preferences.getPreference("localhost", "other")
            ) }
        )

        verify(exactly = 2) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
        confirmVerified(preferenceDao, applicationContext, request, context)
    }

    @Test
    fun getUnitTypes() {
        every { preferenceDao.findByHostAndKey("host1", any()) } returns
                Optional.of(Preference("host1", "unit", "english"))
        every { preferenceDao.findByHostAndKey("host2", any()) } returns
                Optional.of(Preference("host2", "unit", "international"))
        every { preferenceDao.findByHostAndKey("host3", any()) } returns
                Optional.of(Preference("host3", "unit", "any"))
        every { preferenceDao.findByHostAndKey("host4", any()) } returns Optional.empty()
        every { preferenceDao.findByHostAndKey("host5", any()) } returns
                Optional.of(Preference("host5", "unit", "unknown value"))

        Assertions.assertAll(
            Executable { Assertions.assertEquals(UnitPreference.ENGLISH, preferences.getUnitTypes("host1")) },
            Executable { Assertions.assertEquals(UnitPreference.INTERNATIONAL, preferences.getUnitTypes("host2")) },
            Executable { Assertions.assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host3")) },
            Executable { Assertions.assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host4")) },
            Executable { Assertions.assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host5")) }
        )

        verify(exactly = 5) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
        confirmVerified(preferenceDao, applicationContext, request, context)
    }

    @Test
    fun getUnitNames() {
        every { preferenceDao.findByHostAndKey("host1", any()) } returns
                Optional.of(Preference("host1", "unit", "full"))
        every { preferenceDao.findByHostAndKey("host2", any()) } returns
                Optional.of(Preference("host2", "unit", "abbreviation"))
        every { preferenceDao.findByHostAndKey("host3", any()) } returns
                Optional.of(Preference("host3", "unit", "unknown value"))
        every { preferenceDao.findByHostAndKey("host4", any()) } returns Optional.empty()

        Assertions.assertAll(
            Executable { Assertions.assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host1")) },
            Executable { Assertions.assertEquals(UnitAbbrev.ABBREVIATION, preferences.getUnitNames("host2")) },
            Executable { Assertions.assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host3")) },
            Executable { Assertions.assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host4")) },
        )

        verify(exactly = 4) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
        confirmVerified(preferenceDao, applicationContext, request, context)
    }

    @Test
    fun addScripts() {
        val html = preferences.initHtml()
        preferences.addScripts(html, "fauxAlert.js")

        Assertions.assertTrue(html.get().trim().contains("alert('nothing');"))

        verify { context.setApplicationContext(any()) }
        confirmVerified(preferenceDao, applicationContext, request, context)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }
}