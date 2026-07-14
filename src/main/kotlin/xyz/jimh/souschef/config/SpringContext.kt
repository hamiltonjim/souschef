/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class SpringContext(val context: ApplicationContext) {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: SpringContext
        inline fun <reified T : Any> getBean(): T = instance.context.getBean()
    }
}