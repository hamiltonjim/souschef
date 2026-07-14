/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.BeansException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.data.CategoryDao

class SpringContextTest {

    private lateinit var context: ApplicationContext

    @Test
    fun `getBean unavailable should throw exception`() {
        context = mockk(relaxed = true)
        SpringContext(context)
        every { context.getBean(CategoryDao::class.java) } throws NoSuchBeanDefinitionException("not found")
        assertThrows<BeansException> { SpringContext.getBean<CategoryDao>() }
    }

    @Test
    fun `getBean succeeds`() {
        context = mockk(relaxed = true)
        SpringContext(context)
        val categoryDao: CategoryDao = mockk()
        every { context.getBean(CategoryDao::class.java) } returns categoryDao
        assertNotNull(SpringContext.getBean<CategoryDao>())
    }

    @Test
    fun `context has not been set`() {
        resetLateInitField(SpringContext, "instance")
        assertThrows<UninitializedPropertyAccessException> { SpringContext.getBean<CategoryDao>() }
    }
}