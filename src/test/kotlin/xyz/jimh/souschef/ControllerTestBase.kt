/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.data.PreferenceDao

open class ControllerTestBase {

    protected lateinit var request: HttpServletRequest
    protected lateinit var context: SpringContext
    protected lateinit var applicationContext: ApplicationContext
    protected lateinit var preferenceDao: PreferenceDao

    protected fun setupContext() {
        preferenceDao = mockk(relaxed = true)
        Preferences.preferenceDao = preferenceDao    // always reset!

        request = mockk()
        every { request.remoteHost } returns "localhost"

        applicationContext = mockk()

        context = mockk()
        every { context.setApplicationContext(applicationContext) } answers { callOriginal() }
        context.setApplicationContext(applicationContext)
    }
}