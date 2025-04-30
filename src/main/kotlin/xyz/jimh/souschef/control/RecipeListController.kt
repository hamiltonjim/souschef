/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.Collections.singletonMap
import java.util.Optional
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.Preferences.languageStrings
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao
import xyz.jimh.souschef.display.HtmlBuilder

/**
 * Controller that displays the [Category] list. After clicking on
 * one of the categories, shows a list of [Recipe]s in that [Category].
 * @constructor Automagically built with data accessors [RecipeDao]
 * and [CategoryDao].
 */
@RestController
class RecipeListController(
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
) : Listener {

    private val kLogger = KotlinLogging.logger {}
    internal var lastMessage: Pair<String, Any>? = null
    internal var lastMessageTime: Instant? = null

    /**
     * On startup, binds this [Listener] to [Preferences] (as [Broadcaster])
     */
    @PostConstruct
    fun init() {
        Preferences.addListener(this)
    }

    /**
     * On shutdown, unbinds from [Preferences].
     */
    @PreDestroy
    fun destroy() {
        Preferences.removeListener(this)
    }

    /**
     * Listener for changes in [Preference] values.
     */
    override fun listen(name: String, value: Any) {
        lastMessage = Pair(name, value)
        lastMessageTime = Instant.now()
        kLogger.debug { "listen: $name=$value" }
    }

    /**
     * Build the [Category] list screen.
     */
    @Operation(summary = "Builds the default 'Categories' screen–same as /category-list")
    @GetMapping
    fun getDefault(request: HttpServletRequest): ResponseEntity<String> {
        return buildCategoryList(request)
    }

    /**
     * Build the [Category] list screen.
     */
    @Operation(summary = "Builds the default 'Categories' screen–same as the default")
    @GetMapping("/category-list")
    fun getCategoryList(request: HttpServletRequest): ResponseEntity<String> {
        return buildCategoryList(request)
    }

    private fun buildCategoryList(request: HttpServletRequest): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml()
        appendCategories(html).addBreak().addBreak()

        val catLabel = "catName"
        html.addBodyElement("form", singletonMap("action", "add-category"))
            .addBodyElement("label", singletonMap("for", catLabel))
            .addBodyText(languageStrings.get("New Category")).closeBodyElement()
            .addBodyElement(
                "input",
                mapOf(
                    "id" to catLabel, "name" to catLabel, "type" to "text",
                    "onkeyup" to "enableWhenNotEmpty(this, 'cat-submit')"
                ),
                true
            ).addWhitespace()

        html.addBodyElement(
            "input",
            mapOf(
                "type" to "submit",
                "value" to languageStrings.get("Create Category"),
                "id" to "cat-submit",
                "disabled" to "true"
            ),
            true
        )
            .addBreak()
            .closeBodyElement()

        return ResponseEntity.ok(html.get())
    }

    /**
     * Save a new [Category] and rebuild the Category list screen.
     */
    @Operation(summary = "Adds a new category to the recipe database")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Category added successfully"),
        ApiResponse(responseCode = "400", description = "Category is blank"),
        ApiResponse(responseCode = "409", description = "Category already exists"),
    ])
    @GetMapping("/add-category")
    fun addCategory(request: HttpServletRequest, @RequestParam catName: String): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        if (catName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, languageStrings.get("Please provide a category name"))
        }

        val category = Category(catName)
        try {
            categoryDao.save(category)
        } catch (e: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        return buildCategoryList(request)
    }

    /**
     * On click of a "delete" or "undelete" link, either delete or restore
     * the given [Recipe] by its [id] and [categoryId].
     */
    @Operation(
        summary = "Deletes a recipe from the recipe database",
        description = "'Deleted' recipes are not actually deleted, they are just marked as " +
                "deleted; they can be restored (when undelete is true). " +
                "If undelete is missing, it defaults to false.",
    )
    @GetMapping(value = ["/delete-recipe/{id}/{categoryId}","/delete-recipe/{id}/{categoryId}/{undelete}"])
    fun deleteRecipe(
        request: HttpServletRequest,
        @PathVariable("id") id: Long,
        @PathVariable("categoryId") categoryId: Long,
        @PathVariable("undelete") undeleteOptional: Optional<Boolean>
    ): ResponseEntity<String> {
        val undelete = when {
            undeleteOptional.isPresent -> undeleteOptional.get()
            else -> false
        }

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

    /**
     * Upon click of a [Category] name, rebuild the list, adding the list of [Recipe]s
     * in that Category (identified by the [categoryId]).
     */
    @Operation(summary = "Build the screen with the list of all recipes from the given category")
    @GetMapping("/recipe-list/{categoryId}")
    fun getRecipeList(request: HttpServletRequest, @PathVariable categoryId: Long): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml()
        appendCategories(html).addBreak().addBreak()

        val categoryOptional = categoryDao.findById(categoryId)
        if (categoryOptional.isEmpty) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, languageStrings.get("Category not found"))
        }
        html.addBodyElement("h2").addBodyText(categoryOptional.get().name).closeBodyElement()

        html.addBodyElement(
            "input",
            mapOf(
                "type" to "button",
                "class" to "tableHeader",
                "value" to languageStrings.get("New Recipe"),
                "onclick" to "openUrl('/souschef/new-recipe/$categoryId')"
            ),
            true
        ).addBreak().addBreak()

        val showDeleted = "true" == Preferences.getPreference(request.remoteHost, "showDeleted")
        val recipes = when {
            showDeleted -> recipeDao.findAllByCategoryId(categoryId)
            else -> recipeDao.findAllByCategoryIdAndDeletedIsFalse(categoryId)
        }.sortedBy { it.name }
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
                html.addBodyElement("a", singletonMap("href", "/souschef/show-recipe/${it.id}"))
                    .addBodyText(languageStrings.get("View")).closeBodyElement().addWhitespace()
                    .addBodyElement("a", singletonMap("href", "/souschef/edit-recipe/${it.id}"))
                    .addBodyText(languageStrings.get("Edit")).closeBodyElement().addWhitespace()

            html.addBodyElement("a", singletonMap("href", "/souschef/delete-recipe/${it.id}/$categoryId/$deleted"))
                .addBodyText(languageStrings.get(delText)).closeBodyElement().addWhitespace()
                .addBreak()
        }

        return ResponseEntity.ok(html.get())
    }

    private fun appendCategories(html: HtmlBuilder): HtmlBuilder {
        html.addBodyElement("h3").addBodyText(languageStrings.get("Categories")).closeBodyElement()
        categoryDao.findAll().sortedBy { it.name }
            .forEach {
                html.addBodyElement("a", singletonMap("href", "/souschef/recipe-list/${it.id}"))
                    .addBodyText(it.name).closeBodyElement().addWhitespace()
            }
        return html
    }
}