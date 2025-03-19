/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.BeansException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import xyz.jimh.souschef.data.CategoryDao

class SpringContextTest {

    private val springContext = SpringContext
    private lateinit var context: ApplicationContext

    @BeforeEach
    fun setUp() {
        context = mockk()
        springContext.setApplicationContext(context)
        every { context.getBean(CategoryDao::class.java) } throws NoSuchBeanDefinitionException("not found")
    }

    @Test
    fun `getBean unavailable should throw exception`() {
        assertThrows<BeansException> { (springContext.getBean(CategoryDao::class.java)) }
    }
}