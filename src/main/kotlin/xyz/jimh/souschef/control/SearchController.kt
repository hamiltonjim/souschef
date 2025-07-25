/*
 * Copyright (c) 2025 Jim Hamilton. All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.OListener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.display.HtmlBuilder

/**
 * Controller for searching for key phrases in recipes.
 */
@RestController
class SearchController(private val recipeController: RecipeController) : OListener() {

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
     * Loads the search page.
     */
    @Operation(summary = "Builds the search screen")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Builds the Search screen",
            content = [Content(mediaType = "text/html")]
        )
    )
    @GetMapping("/search")
    fun search(request: HttpServletRequest): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml(mapOf("class" to "rendered"))

        html
            .addBodyElement("h1")
            .addBodyText(Preferences.getLanguageString("searchTitle"))
            .closeBodyElement().addBreak().addBreak()

            .addBodyElement(tag = "div", attributes = mapOf("class" to "centered"))
            .addBodyElement(
                tag = "label",
                attributes = mapOf("class" to "search-size", "for" to "search-text")
            ).addBodyText(Preferences.getLanguageString("searchFor")).closeBodyElement().addWhitespace()

            .addBodyElement(
                tag = "input",
                attributes = mapOf(
                    "type" to "text",
                    "class" to "search-text search-size",
                    "id" to "search-text",
                    "placeholder" to Preferences.getLanguageString("search for this")
                ),
                closing = true
            )
            .addBreak().addBreak()
            .addBodyElement(tag = "div", attributes = mapOf("class" to "centered"))
            .addBodyElement(tag = "input", attributes = mapOf(
                "type" to "button",
                "value" to Preferences.getLanguageString("byTitle"),
                "onclick" to "search('titles')"
            )).closeBodyElement().addWhitespace()
            .addBodyElement(tag = "input", attributes = mapOf(
                "type" to "button",
                "value" to Preferences.getLanguageString("byIngredients"),
                "onclick" to "search('ingredients')"
            )).closeBodyElement().addWhitespace()
            .addBodyElement(tag = "input", attributes = mapOf(
                "type" to "button",
                "value" to Preferences.getLanguageString("byDirections"),
                "onclick" to "search('directions')"
            )).closeBodyElement()
            .closeBodyElement() // inner div
            .closeBodyElement() // outer div

            .addBodyElement(
                tag = "div",
                attributes = mapOf("class" to "centered", "id" to "search-results"),
                closing = true
            )
        return ResponseEntity.ok(html.get())
    }

    /**
     * Searches [Recipe] titles for the given [phrase].
     */
    @Operation(summary = "Searches recipe titles for the given phrase")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of links to recipes",
            content = [Content(mediaType = "text/html")]
        )
    )
    @PostMapping("/search/titles/{phrase}")
    fun searchTitles(@PathVariable phrase: String): ResponseEntity<String> {
        val recipes = recipeController.searchRecipeTitles(phrase)
        return ResponseEntity.ok(listRecipes(recipes))
    }

    /**
     * Searches [Recipe] ingredients for the given [phrase].
     */
    @Operation(summary = "Searches recipe ingredients for the given phrase")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of links to recipes",
            content = [Content(mediaType = "text/html")]
        )
    )
    @PostMapping("/search/ingredients/{phrase}")
    fun searchIngredients(@PathVariable phrase: String): ResponseEntity<String> {
        val recipes = recipeController.searchRecipeIngredients(phrase)
        return ResponseEntity.ok(listRecipes(recipes))
    }

    /**
     * Searches [Recipe] directions for the given [phrase].
     */
    @Operation(summary = "Searches recipe directions for the given phrase")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of links to recipes",
            content = [Content(mediaType = "text/html")]
        )
    )
    @PostMapping("/search/directions/{phrase}")
    fun searchDirections(@PathVariable phrase: String): ResponseEntity<String> {
        val recipes = recipeController.searchRecipeDirections(phrase)
        return ResponseEntity.ok(listRecipes(recipes))
    }

    private fun listRecipes(recipes: List<Recipe>): String {
        // We're going to use an HtmlBuilder, but only use the body, and ignore the header.
        val html = HtmlBuilder()

        html.addBodyElement(tag = "hr", closing = true).addBreak()

        if (recipes.isEmpty()) {
            html.addBodyText(Preferences.getLanguageString("noRecipesFound"))
            return html.body.toString()
        }

        html.addBodyElement(tag = "ul")

        recipes.forEach { recipe ->
            html.addBodyElement(tag = "li")
                .addBodyElement(
                    tag = "a",
                    attributes = mapOf("href" to "/souschef/show-recipe/${recipe.id}")
                )
                .addBodyText(recipe.name).closeBodyElement().closeBodyElement()
        }

        html.closeBodyElement()
        return html.body.toString()
    }

}