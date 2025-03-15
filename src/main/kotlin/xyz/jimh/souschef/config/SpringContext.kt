/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object SpringContext : ApplicationContextAware {

    private lateinit var appContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        appContext = applicationContext
    }

    fun <T> getBean(clazz: Class<T>): T {
        check(this::appContext.isInitialized) {
            "Spring context has not been initialized; getting bean of type ${clazz.simpleName}"
        }
        return appContext.getBean(clazz)
    }
}