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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
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
        Preferences.preferenceDao = preferenceDao
        Preferences.locale = "en_US"

        resetLateInitField(Preferences, "languageOptions")
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
            { assertTrue(html.contains("<body onload=\"setSelects()\""), "body") },
            { assertTrue(html.contains("<div id=\"preferences\">"), "footer") },
            { assertTrue(styleStart >= 0, "style start") },
            { assertTrue(styleEnd > styleStart, "style end after start") },
            { assertTrue(scriptStart >= 0, "script start") },
            { assertTrue(scriptEnd > scriptStart, "script end after start") },
            { assertTrue(html.contains("<head>"), "head exists") },
        )

        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun getPreferenceValues() {
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        val prefs = Preferences.getPreferenceValues(request)
        assertNotNull(prefs)
        assertAll(
            { assertNotNull(prefs.body) },
            { assertEquals("foo", prefs.body!!["foo"]) },
            { assertEquals("bar", prefs.body!!["bar"]) },
            { assertEquals("baz", prefs.body!!["baz"]) },
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
        Preferences.setPreferenceValue(remoteRequest, "anything", "nothing")

        Preferences.setPreferenceValue(request, "answer", "42")
        // one save repeated, for coverage
        Preferences.setPreferenceValue(request, "answer", "42")
        Preferences.setPreferenceValue(request, "ford", "prefect")
        Preferences.setPreferenceValue(request, "arthur", "dent")
        Preferences.setPreferenceValue(request, "zaphod", "beeblebrox")
        Preferences.setPreferenceValue(request, "anything else", "")

        // language
        Preferences.setPreferenceValue(request, "language", "en_US")

        assertAll(
            { assertEquals("42", Preferences.getPreference("localhost", "answer")) },
            { assertEquals("beeblebrox", Preferences.getPreference("localhost", "zaphod")) },
            { assertEquals("prefect", Preferences.getPreference("localhost", "ford")) },
            { assertEquals("dent", Preferences.getPreference("localhost", "arthur")) },
            { assertEquals("not found", Preferences.getPreference("localhost", "anything else")) },
            { assertEquals("en_US", Preferences.getPreference("localhost", "language")) },
            { assertNull(Preferences.getPreference("remote", "anything")) },
        )

        verify(exactly = 1) { context.setApplicationContext(any()) }
        verify(exactly = 6) { request.remoteHost }
        verify(exactly = 7) { preferenceDao.save(allAny()) }
        verify(exactly = 12) { preferenceDao.findByHostAndKey("localhost", allAny()) }
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

        Preferences.setPreferenceValue(request, "answer", "42")
        Preferences.setPreferenceValue(request, "ford", "prefect")
        Preferences.setPreferenceValue(request, "zaphod", "beeblebrox")

        val success = Preferences.deletePreference(request, "zaphod")
        val failure = Preferences.deletePreference(request, "zaphod")

        assertAll(
            { assertEquals("42", Preferences.getPreference("localhost", "answer")) },
            { assertEquals("prefect", Preferences.getPreference("localhost", "ford")) },
            { assertNull(Preferences.getPreference("localhost", "zaphod")) },
            { assertEquals(HttpStatus.OK, success.statusCode) },
            { assertEquals(HttpStatus.NOT_FOUND, failure.statusCode) },
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
            { assertEquals("key_host", Preferences.getPreference("host", "key")) },
            { assertEquals("other_localhost", Preferences.getPreference("localhost", "other")) }
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
            { assertEquals(UnitPreference.ENGLISH, Preferences.getUnitTypes("host1")) },
            { assertEquals(UnitPreference.INTERNATIONAL, Preferences.getUnitTypes("host2")) },
            { assertEquals(UnitPreference.ANY, Preferences.getUnitTypes("host3")) },
            { assertEquals(UnitPreference.ANY, Preferences.getUnitTypes("host4")) },
            { assertEquals(UnitPreference.ANY, Preferences.getUnitTypes("host5")) }
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
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host1")) },
            { assertEquals(UnitAbbrev.ABBREVIATION, Preferences.getUnitNames("host2")) },
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host3")) },
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host4")) },
        )

        verify(exactly = 4) { preferenceDao.findByHostAndKey(any(), any()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun addScripts() {
        val html = Preferences.initHtml()
        Preferences.addScripts(html, "fauxAlert.js")

        assertTrue(html.get().trim().contains("alert('nothing');"))

        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `no preferences dao`() {
        preferenceDao = mockk()
        Preferences.preferenceDao = preferenceDao
        every { preferenceDao.findByHostAndKey(any(), any()) } returns Optional.empty()
        every { preferenceDao.findAllByHost(any()) } returns emptyList()
        assertAll(
            { assertNull(Preferences.getPreference("localhost", "foo")) },
            {
                val response = Preferences.getPreferenceValues(request)
                assertEquals(emptyMap(), response.body) }
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
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host1")) },
            { assertEquals(UnitAbbrev.ABBREVIATION, Preferences.getUnitNames("host2")) },
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host3")) },
            { assertEquals(UnitAbbrev.FULL_NAME, Preferences.getUnitNames("host4")) },
        )

        verify { applicationContext.getBean(PreferenceDao::class.java) }
        verify(exactly = 4) { preferenceDao.findByHostAndKey(allAny(), allAny()) }
        verify { context.setApplicationContext(any()) }
    }

    @Test
    fun `cover implicit null check on lateinit`() {
        resetLateInitField(Preferences, "preferenceDao")
        resetLateInitField(Preferences, "locale")
        resetLateInitField(Preferences, "languageStrings")
        assertThrows<UninitializedPropertyAccessException> { Preferences.preferenceDao.findAllByHost("remote") }
        assertThrows<UninitializedPropertyAccessException> { println(Preferences.locale) }
        assertThrows<UninitializedPropertyAccessException> { Preferences.languageStrings.get("locale") }
        verify { context.setApplicationContext(allAny()) }
    }

    @Test
    fun `cover implicit null check on lateinit when building HTML`() {
        Preferences.initHtml()

        resetLateInitField(Preferences, "locale")
        assertThrows<UninitializedPropertyAccessException>{ Preferences.initHtml() }
        verify { context.setApplicationContext(allAny()) }
    }

    @AfterEach
    fun cleanup() {
        confirmVerified(preferenceDao, applicationContext, request, context)
        clearAllMocks()
    }
}