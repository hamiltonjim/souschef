// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.config

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContext : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        private var context: ApplicationContext? = null

        fun <T> getBean(clazz: Class<T>): T {
            val appContext = context
            check(appContext != null) {
                "Spring context has not been initialized; getting bean of type ${clazz.simpleName}"
            }
            return appContext.getBean(clazz)
        }
    }
}