/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.OListener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
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
) : OListener() {

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
     * Build the [Category] list screen.
     */
    @Operation(summary = "Builds the default 'Categories' screen")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The category list",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping(value = ["/", "/category-list"], produces = [MediaType.TEXT_HTML_VALUE])
    fun getCategoryList(request: HttpServletRequest): ResponseEntity<String> {
        return buildCategoryList(request)
    }

    private fun buildCategoryList(request: HttpServletRequest): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml(mapOf("class" to "rendered"))
        appendCategories(html).addBreak().addBreak()

        val catLabel = "catName"
        html.addBodyElement(tag = "form", attributes = mapOf("action" to "add-category"))
            .addBodyElement(tag = "label", attributes = mapOf("for" to catLabel))
            .addBodyText(Preferences.getLanguageString("New Category")).closeBodyElement()
            .addBodyElement(
                tag = "input",
                attributes = mapOf(
                    "id" to catLabel,
                    "name" to catLabel,
                    "type" to "text",
                    "onkeyup" to "enableWhenNotEmpty(this, 'cat-submit')"
                ),
                closing = true
            ).addWhitespace()

        html.addBodyElement(
            tag = "input",
            attributes = mapOf(
                "type" to "submit",
                "value" to Preferences.getLanguageString("Create Category"),
                "id" to "cat-submit",
                "disabled" to "true"
            ),
            closing = true
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
        ApiResponse(
            responseCode = "200",
            description = "Category added successfully",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
        ApiResponse(responseCode = "400", description = "Category is blank"),
        ApiResponse(responseCode = "409", description = "Category already exists"),
    ])
    @GetMapping("/add-category", produces = [MediaType.TEXT_HTML_VALUE])
    fun addCategory(request: HttpServletRequest, @RequestParam catName: String): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        if (catName.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                Preferences.getLanguageString("Please provide a category name")
            )
        }

        val category = Category(catName)
        try {
            categoryDao.save(category)
        } catch (_: DataIntegrityViolationException) {
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
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The resulting recipe list for the category",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping(
        value = [
            "/delete-recipe/{id}/{categoryId}",
            "/delete-recipe/{id}/{categoryId}/{undelete}"
                ],
        produces = [MediaType.TEXT_HTML_VALUE],
    )
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
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The recipe list for the category",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/recipe-list/{categoryId}", produces = [MediaType.TEXT_HTML_VALUE])
    fun getRecipeList(request: HttpServletRequest, @PathVariable categoryId: Long): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml(mapOf("class" to "rendered"))
        appendCategories(html).addBreak().addBreak()

        val categoryOptional = categoryDao.findById(categoryId)
        if (categoryOptional.isEmpty) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                Preferences.getLanguageString("Category not found")
            )
        }
        html.addBodyElement("h2").addBodyText(categoryOptional.get().name).closeBodyElement()

        html.addBodyElement(
            tag = "input",
            attributes = mapOf(
                "type" to "button",
                "class" to "tableHeader",
                "value" to Preferences.getLanguageString("New Recipe"),
                "onclick" to "openUrl('/souschef/new-recipe/$categoryId')"
            ),
            closing = true
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
                html.addBodyElement(tag = "a", attributes = mapOf("href" to "/souschef/show-recipe/${it.id}"))
                    .addBodyText(Preferences.getLanguageString("View")).closeBodyElement().addWhitespace()
                    .addBodyElement(tag = "a", attributes = mapOf("href" to "/souschef/edit-recipe/${it.id}"))
                    .addBodyText(Preferences.getLanguageString("Edit")).closeBodyElement().addWhitespace()

            html.addBodyElement(
                tag = "a",
                attributes = mapOf("href" to "/souschef/delete-recipe/${it.id}/$categoryId/$deleted")
            )
                .addBodyText(Preferences.getLanguageString(delText)).closeBodyElement().addWhitespace()
                .addBreak()
        }

        return ResponseEntity.ok(html.get())
    }

    private fun appendCategories(html: HtmlBuilder): HtmlBuilder {
        html.addBodyElement("h3")
            .addBodyText(Preferences.getLanguageString("Categories")).closeBodyElement()
        categoryDao.findAll().sortedBy { it.name }
            .forEach {
                html.addBodyElement(tag = "a", attributes = mapOf("href" to "/souschef/recipe-list/${it.id}"))
                    .addBodyText(it.name).closeBodyElement().addWhitespace()
            }
        return html
    }
}