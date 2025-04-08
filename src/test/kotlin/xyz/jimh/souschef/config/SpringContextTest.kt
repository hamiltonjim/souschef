/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.BeansException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.data.CategoryDao

class SpringContextTest {

    private val springContext = SpringContext
    private lateinit var context: ApplicationContext

    @Test
    fun `getBean unavailable should throw exception`() {
        context = mockk()
        springContext.setApplicationContext(context)
        every { context.getBean(CategoryDao::class.java) } throws NoSuchBeanDefinitionException("not found")
        assertThrows<BeansException> { springContext.getBean(CategoryDao::class.java) }
        verify { springContext.getBean(CategoryDao::class.java) }
    }

    @Test
    fun `getBean succeeds`() {
        context = mockk()
        springContext.setApplicationContext(context)
        val categoryDao: CategoryDao = mockk()
        every { context.getBean(CategoryDao::class.java) } returns categoryDao
        assertNotNull(context.getBean(CategoryDao::class.java))
        verify { springContext.getBean(CategoryDao::class.java) }
    }

    @Test
    fun `context has not been set`() {
        resetLateInitField(SpringContext, "appContext")
        assertThrows<UninitializedPropertyAccessException> { springContext.getBean(CategoryDao::class.java) }
    }
}