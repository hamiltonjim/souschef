package xyz.jimh.souschef.config

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Test
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain

class WebSecurityConfigTest {
    private var webSecurityConfig: WebSecurityConfig = WebSecurityConfig()

    private lateinit var httpSecurity: HttpSecurity

    @Test
    fun `getOriginPatterns$souschef`() {
        assertNull(webSecurityConfig.originPatterns)
        webSecurityConfig.originPatterns = listOf("*")
        assertNotNull(webSecurityConfig.originPatterns)
        val list = webSecurityConfig.originPatterns
        assertEquals("*", list?.get(0) ?: "")
    }

    @Test
    fun configure() {
        httpSecurity = mockk(relaxed = true)
        every { httpSecurity.cors { any() } } returns httpSecurity
        every { httpSecurity.csrf { any() } } returns httpSecurity
        every { httpSecurity.authorizeHttpRequests { any() } } returns httpSecurity
        every { httpSecurity.build() } returns mockk<DefaultSecurityFilterChain>()
        webSecurityConfig.configure(httpSecurity)
    }

    @Test
    fun `get cors configuration`() {
        val config = webSecurityConfig.corsConfiguration()
        assertNotNull(config)
    }
}