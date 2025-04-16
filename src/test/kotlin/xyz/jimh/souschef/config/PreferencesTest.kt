package xyz.jimh.souschef.config

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import java.util.Optional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
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
        verify { request.remoteHost }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `set preference values test`() {
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

        // cover case where no preference exists
        val remoteRequest: HttpServletRequest = mockk()
        every { remoteRequest.remoteHost } returns "remote"
        preferences.setPreferenceValue(remoteRequest, "anything", "nothing")

        preferences.setPreferenceValue(request, "answer", "42")
        // one save repeated, for coverage
        preferences.setPreferenceValue(request, "answer", "42")
        preferences.setPreferenceValue(request, "ford", "prefect")
        preferences.setPreferenceValue(request, "arthur", "dent")
        preferences.setPreferenceValue(request, "zaphod", "beeblebrox")
        preferences.setPreferenceValue(request, "anything else", "")

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
        verify(exactly = 6) { preferenceDao.save(allAny()) }
        verify(exactly = 10) { preferenceDao.findByHostAndKey("localhost", allAny()) }
        verify(exactly = 2) { preferenceDao.findByHostAndKey("remote", allAny()) }
    }

    @Test
    fun `delete preference test`() {
        val map = mutableMapOf<String, String>()
        val stringSlot = slot<String>()
        every { preferenceDao.findByHostAndKey("localhost", capture(stringSlot)) } answers {
            val key = stringSlot.captured
            val preference = when (map[key]) {
                null -> null
                else -> Preference("localhost", key, map[key] ?: "not found")
            }
            Optional.ofNullable(preference)
        }
        val preferenceSlot = slot<Preference>()
        every { preferenceDao.save(capture(preferenceSlot))} answers {
            val preference = preferenceSlot.captured
            map[preference.key] = preference.value
            preference
        }
        every { preferenceDao.delete(capture(preferenceSlot)) } answers {
            val preference = preferenceSlot.captured
            val key = preference.key
            when {
                map[key] == null -> preference.value = "not found"
                else -> {
                    preference.value = map[key] as String
                }
            }
            map -= key
        }

        preferences.setPreferenceValue(request, "answer", "42")
        preferences.setPreferenceValue(request, "ford", "prefect")
        preferences.setPreferenceValue(request, "zaphod", "beeblebrox")

        val success = preferences.deletePreference(request, "zaphod")
        val failure = preferences.deletePreference(request, "zaphod")

        assertAll(
            Executable { assertEquals("42", preferences.getPreference("localhost", "answer")) },
            Executable { assertEquals("prefect", preferences.getPreference("localhost", "ford")) },
            Executable { assertNull(preferences.getPreference("localhost", "zaphod")) },
            Executable { assertEquals(HttpStatus.OK, success.statusCode) },
            Executable { assertEquals(HttpStatus.NOT_FOUND, failure.statusCode) },
        )

        verify {
            preferenceDao.findByHostAndKey("localhost", allAny())
            preferenceDao.save(allAny())
            preferenceDao.delete(allAny())
        }
        verify { request.remoteHost }
        verify { context.setApplicationContext(allAny()) }
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
        preferenceDao = mockk()
        preferences.preferenceDao = preferenceDao
        every { preferenceDao.findByHostAndKey(any(), any()) } returns Optional.empty()
        every { preferenceDao.findAllByHost(any()) } returns emptyList()
        assertAll(
            Executable { assertNull(preferences.getPreference("localhost", "foo")) },
            Executable {
                val response = preferences.getPreferenceValues(request)
                assertEquals(emptyMap<String, Any?>(), response.body) }
        )
        verify { context.setApplicationContext(any()) }
        verify { request.remoteHost }
        verify {
            preferenceDao.findByHostAndKey(allAny(), allAny())
            preferenceDao.findAllByHost(allAny())
        }
    }

    @Test
    fun `preferencesDao is uninitialized`() {
        resetLateInitField(Preferences, "preferenceDao")

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

        verify { applicationContext.getBean(PreferenceDao::class.java) }
        verify(exactly = 4) { preferenceDao.findByHostAndKey(allAny(), allAny()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `cover implicit null check on lateinit`() {
        resetLateInitField(Preferences, "preferenceDao")
        assertThrows<UninitializedPropertyAccessException> { preferences.preferenceDao.findAllByHost("remote") }
        verify { context.setApplicationContext(allAny()) }
    }

    @AfterEach
    fun cleanup() {
        confirmVerified(preferenceDao, applicationContext, request, context)
        clearAllMocks()
    }
}