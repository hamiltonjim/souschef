// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.control

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.Collections.singletonMap
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.RecipeDao
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.UrlBaser

@RestController
class RecipeListController(
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
) : Listener {

    private var baseUrl: String = ""

    val kLogger = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        Preferences.addListener(this)
    }

    @PreDestroy
    fun destroy() {
        Preferences.removeListener(this)
    }

    override fun listen(name: String, value: String) {
        kLogger.debug { "listen: $name=$value" }
    }

    @GetMapping
    fun getDefault(request: HttpServletRequest): ResponseEntity<String> {
        baseUrl = request.requestURL.toString()
        return buildCategoryList()
    }

    @GetMapping("/category-list")
    fun getCategoryList(request: HttpServletRequest): ResponseEntity<String> {
        baseUrl = UrlBaser.baseUrl("/category-list", request.requestURL)
        return buildCategoryList()
    }

    @GetMapping("/add-category")
    fun addCategory(request: HttpServletRequest, @RequestParam catName: String?): ResponseEntity<String> {
        if (catName.isNullOrEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide a category name")
        }

        baseUrl = UrlBaser.baseUrl("/add-category", request.requestURL)
        val category = Category(catName)
        try {
            categoryDao.save(category)
        } catch (e: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        return buildCategoryList()
    }

    private fun buildCategoryList(): ResponseEntity<String> {
        val html = Preferences.initHtml(baseUrl)
        appendCategories(html).addBreak().addBreak()

        val catLabel = "catName"
        html.addBodyElement("form", singletonMap("action", "add-category"))
            .addBodyElement("label", singletonMap("for", catLabel))
            .addBodyText("New Category:").closeBodyElement()
            .addBodyElement(
                "input", mapOf(
                    "id" to catLabel, "name" to catLabel, "type" to "text",
                    "onkeyup" to "enableWhenNotEmpty(this, 'cat-submit')"
                ), true
            ).addWhitespace()

        html.addBodyElement(
            "input",
            mapOf("type" to "submit", "value" to "Create Category", "id" to "cat-submit", "disabled" to "true"),
            true
        ).addBreak()
            .closeBodyElement()

        return ResponseEntity.ok(html.get())
    }

    @GetMapping("/delete-recipe/{id}/{categoryId}/{undelete}")
    fun deleteRecipe(
        request: HttpServletRequest,
        @PathVariable("id") id: Long,
        @PathVariable("categoryId") categoryId: Long,
        @PathVariable("undelete") undelete: Boolean
    ): ResponseEntity<String> {
        val recipeOptional = recipeDao.findById(id)
        if (recipeOptional.isEmpty) return getRecipeList(request, categoryId)

        val recipe = recipeOptional.get()
        if (recipe.deleted != undelete) return getRecipeList(request, categoryId)

        recipe.deleted = !undelete
        recipe.deletedOn = when {
            undelete -> null
            else -> Instant.now()
        }
        recipeDao.save(recipe)
        return getRecipeList(request, categoryId)
    }

    @GetMapping("/recipe-list/{categoryId}")
    fun getRecipeList(request: HttpServletRequest, @PathVariable categoryId: Long): ResponseEntity<String> {
        baseUrl = UrlBaser.baseUrl("/recipe-list", request.requestURL)
        val html = Preferences.initHtml(baseUrl)
        appendCategories(html).addBreak().addBreak()

        val categoryOptional = categoryDao.findById(categoryId)
        if (categoryOptional.isEmpty) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
        }
        html.addBodyElement("h2").addBodyText(categoryOptional.get().name).closeBodyElement()

        html.addBodyElement(
            "input",
            mapOf(
                "type" to "button",
                "class" to "tableHeader",
                "value" to "New Recipe",
                "onclick" to "openUrl('$baseUrl/new-recipe/$categoryId')"
            ),
            true
        ).addBreak().addBreak()

        val showDeleted = "true" == Preferences.getPreference(request.remoteHost, "showDeleted")
        val recipes = when {
            showDeleted -> recipeDao.findAllByCategoryId(categoryId)
            else -> recipeDao.findAllByCategoryIdAndDeletedIsFalse(categoryId)
        }
        recipes.forEach {
            val deleted = it.deleted
            val titleTag = when {
                deleted -> "del"
                else -> "strong"
            }
            val delText = when {
                deleted -> "Restore"
                else -> "Delete"
            }
            html.addBodyElement(titleTag).addBodyText(it.name).closeBodyElement().addWhitespace()
            if (!deleted)
                html.addBodyElement("a", singletonMap("href", "$baseUrl/show-recipe/${it.id}"))
                    .addBodyText("View").closeBodyElement().addWhitespace()
                    .addBodyElement("a", singletonMap("href", "$baseUrl/edit-recipe/${it.id}"))
                    .addBodyText("Edit").closeBodyElement().addWhitespace()

            html.addBodyElement("a", singletonMap("href", "$baseUrl/delete-recipe/${it.id}/$categoryId/$deleted"))
                .addBodyText(delText).closeBodyElement().addWhitespace()
                .addBreak()
        }

        return ResponseEntity.ok(html.get())
    }

    private fun appendCategories(html: HtmlBuilder): HtmlBuilder {
        html.addBodyElement("h3").addBodyText("Categories").closeBodyElement()
        categoryDao.findAll().sortedBy { it.name }
            .forEach {
                html.addBodyElement("a", singletonMap("href", "$baseUrl/recipe-list/${it.id}"))
                    .addBodyText(it.name).closeBodyElement().addWhitespace()
            }
        return html
    }
}