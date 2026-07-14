/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class SpringContext(val context: ApplicationContext) {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: SpringContext

        /**
         * Returns a bean by its class [T].
         *
         * @throws IllegalStateException if applicationContext is not initialized
         * @throws BeansException if bean cannot be loaded
         */
        inline fun <reified T : Any> getBean(): T = instance.context.getBean(T::class.java)
    }
}