/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.springframework.beans.BeansException
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
     * @throws IllegalStateException if applicationContext is not initialized
     * @throws BeansException if bean cannot be loaded
     */
    @Throws(BeansException::class, IllegalStateException::class)
    fun <T> getBean(clazz: Class<T>): T {
        check(this::appContext.isInitialized) {
            "Spring context has not been initialized; getting bean of type ${clazz.simpleName}"
        }
        return appContext.getBean(clazz)
    }
}