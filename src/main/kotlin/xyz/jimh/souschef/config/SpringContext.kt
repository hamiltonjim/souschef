/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Object that allows a non-bean to access a bean.
 */
@Component
object SpringContext : ApplicationContextAware {

    private lateinit var appContext: ApplicationContext

    /**
     * Called automatically at app startup to supply the [ApplicationContext]
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        appContext = applicationContext
    }

    /**
     * Returns a bean by its class [clazz].
     *
     * @throws UninitializedPropertyAccessException if applicationContext is not
     * initialized (we could check that, and throw a different exception, but
     * that does not really buy anything)
     */
    fun <T> getBean(clazz: Class<T>): T {
        return appContext.getBean(clazz)
    }
}