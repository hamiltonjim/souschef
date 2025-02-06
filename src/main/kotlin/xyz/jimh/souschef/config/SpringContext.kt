package xyz.jimh.souschef.config

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContext : ApplicationContextAware {

    companion object {
        private var context: ApplicationContext? = null

        fun <T> getBean(clazz: Class<T>): T {
            val appContext = context
                ?: throw IllegalStateException("Spring context has not been initialized bean of type ${clazz.simpleName}")
            return appContext.getBean(clazz)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}