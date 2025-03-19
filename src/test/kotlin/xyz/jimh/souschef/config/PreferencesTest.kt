package xyz.jimh.souschef.config

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import java.util.*
import org.hibernate.resource.beans.container.internal.NoSuchBeanException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
        preferenceDao = mockk()
        applicationContext = mockk()
        request = mockk()
        context = mockk()

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
    }

    @Test
    fun initHtml() {
        val html = Preferences.initHtml().get()
        // confirm that all parts are there
        val styleStart = html.indexOf("<style>")
        val styleEnd = html.indexOf("</style>")
        val scriptStart = html.indexOf("<script type=\"text/javascript\">")
        val scriptEnd = html.indexOf("</script>")

        assertAll(
            Executable { assertTrue(html.contains("<body onload=\"setSelects()\">"), "body") },
            Executable { assertTrue(html.contains("<div id=\"preferences\">"), "footer") },
            Executable { assertTrue(styleStart >= 0, "style start") },
            Executable { assertTrue(styleEnd > styleStart, "style end after start") },
            Executable { assertTrue(scriptStart >= 0, "script start") },
            Executable { assertTrue(scriptEnd > scriptStart, "script end after start") },
            Executable { assertTrue(html.contains("<head>"), "head exists") },
        )

        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun getPreferenceValues() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        val prefs = preferences.getPreferenceValues(request)
        assertNotNull(prefs)
        assertAll(
            Executable { assertNotNull(prefs.body) },
            Executable { assertEquals("foo", prefs.body!!["foo"]) },
            Executable { assertEquals("bar", prefs.body!!["bar"]) },
            Executable { assertEquals("baz", prefs.body!!["baz"]) },
        )

        verify(exactly = 1) { preferenceDao.findAllByHost("localhost") }
        verify { request.getRemoteHost() }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `set preference values test`() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        val map = mutableMapOf<String, String>()
        val preferenceSlot = slot<Preference>()
        every { preferenceDao.save(capture(preferenceSlot))} answers {
            val preference = preferenceSlot.captured
            map[preference.key] = preference.value
            preference
        }
        val stringSlot = slot<String>()
        every { preferenceDao.findByHostAndKey("remote", "anything") } returns Optional.empty()
        every { preferenceDao.findByHostAndKey("localhost", capture(stringSlot)) } answers {
            val key = stringSlot.captured
            val preference = Preference("localhost", key, map[key] ?: "not found")
            Optional.of(preference)
        }

        preferences.setPreferenceValue(request, "answer", "42")
        // one save repeated, for coverage
        preferences.setPreferenceValue(request, "answer", "42")
        preferences.setPreferenceValue(request, "ford", "prefect")
        preferences.setPreferenceValue(request, "arthur", "dent")
        preferences.setPreferenceValue(request, "zaphod", "beeblebrox")
        preferences.setPreferenceValue(request, "anything else", null)

        assertAll(
            Executable { assertEquals("42", preferences.getPreference("localhost", "answer")) },
            Executable { assertEquals("beeblebrox", preferences.getPreference("localhost", "zaphod")) },
            Executable { assertEquals("prefect", preferences.getPreference("localhost", "ford")) },
            Executable { assertEquals("dent", preferences.getPreference("localhost", "arthur")) },
            Executable { assertEquals("not found", preferences.getPreference("localhost", "anything else")) },
            Executable { assertNull(preferences.getPreference("remote", "anything")) },
        )

        verify(exactly = 1) { context.setApplicationContext(any()) }
        verify(exactly = 5) { request.remoteHost }
        verify(exactly = 5) { preferenceDao.save(allAny()) }
        verify(exactly = 10) { preferenceDao.findByHostAndKey("localhost", allAny()) }
        verify(exactly = 1) { preferenceDao.findByHostAndKey("remote", allAny()) }
    }

    @Test
    fun getPreference() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { preferenceDao.findByHostAndKey("host", "key") } returns
                Optional.of(Preference("host", "key", "key_host"))
        every { preferenceDao.findByHostAndKey("localhost", "other") } returns
                Optional.of(Preference("localhost", "other", "other_localhost"))

        assertAll(
            Executable { assertEquals(
                "key_host",
                preferences.getPreference("host", "key")
            ) },
            Executable { assertEquals(
                "other_localhost",
                preferences.getPreference("localhost", "other")
            ) }
        )

        verify(exactly = 2) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun getUnitTypes() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { preferenceDao.findByHostAndKey("host1", any()) } returns
                Optional.of(Preference("host1", "unit", "english"))
        every { preferenceDao.findByHostAndKey("host2", any()) } returns
                Optional.of(Preference("host2", "unit", "international"))
        every { preferenceDao.findByHostAndKey("host3", any()) } returns
                Optional.of(Preference("host3", "unit", "any"))
        every { preferenceDao.findByHostAndKey("host4", any()) } returns Optional.empty()
        every { preferenceDao.findByHostAndKey("host5", any()) } returns
                Optional.of(Preference("host5", "unit", "unknown value"))

        assertAll(
            Executable { assertEquals(UnitPreference.ENGLISH, preferences.getUnitTypes("host1")) },
            Executable { assertEquals(UnitPreference.INTERNATIONAL, preferences.getUnitTypes("host2")) },
            Executable { assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host3")) },
            Executable { assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host4")) },
            Executable { assertEquals(UnitPreference.ANY, preferences.getUnitTypes("host5")) }
        )

        verify(exactly = 5) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun getUnitNames() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { preferenceDao.findByHostAndKey("host1", any()) } returns
                Optional.of(Preference("host1", "unit", "full"))
        every { preferenceDao.findByHostAndKey("host2", any()) } returns
                Optional.of(Preference("host2", "unit", "abbreviation"))
        every { preferenceDao.findByHostAndKey("host3", any()) } returns
                Optional.of(Preference("host3", "unit", "unknown value"))
        every { preferenceDao.findByHostAndKey("host4", any()) } returns Optional.empty()

        assertAll(
            Executable { assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host1")) },
            Executable { assertEquals(UnitAbbrev.ABBREVIATION, preferences.getUnitNames("host2")) },
            Executable { assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host3")) },
            Executable { assertEquals(UnitAbbrev.FULL_NAME, preferences.getUnitNames("host4")) },
        )

        verify(exactly = 4) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun addScripts() {
        val html = preferences.initHtml()
        preferences.addScripts(html, "fauxAlert.js")

        assertTrue(html.get().trim().contains("alert('nothing');"))

        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `no preferences dao`() {
        preferences.preferenceDao = null
        every { applicationContext.getBean(PreferenceDao::class.java) } throws NoSuchBeanException(RuntimeException())
        assertAll(
            Executable { assertNull(preferences.getPreference("localhost", "foo")) },
            Executable {
                val response = preferences.getPreferenceValues(request)
                assertEquals(emptyMap<String, Any?>(), response.body) }
        )
        verify { applicationContext.getBean(PreferenceDao::class.java) }
        verify { context.setApplicationContext(any()) }
    }

    @AfterEach
    fun cleanup() {
        confirmVerified(preferenceDao, applicationContext, request, context)
        clearAllMocks()
    }
}