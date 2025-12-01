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
import java.util.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.OListener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.VolumeDao
import xyz.jimh.souschef.data.WeightDao
import xyz.jimh.souschef.display.IngredientFormatter
import xyz.jimh.souschef.utility.MathUtils

/**
 * Controller that builds the screen that displays a [Recipe].
 * @constructor Automagically built with various other controllers and
 * DAOs, as shown.
 */
@RestController
class ShowRecipeController(
    private val foodController: FoodController,
    private val ingredientController: IngredientController,
    private val recipeController: RecipeController,
    private val unitDao: UnitDao,
    private val volumeDao: VolumeDao,
    private val weightDao: WeightDao,
    private val ingredientFormatter: IngredientFormatter,
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
     * Retrieves the given [Recipe] by its [recipeId] and builds the screen to display it.
     */
    @Operation(summary = "Build the screen that shows a recipe as originally written.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The recipe",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/show-recipe/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    fun showRecipe(
        request: HttpServletRequest,
        @PathVariable("id") recipeId: Long
    ): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request, recipe, recipe.servings.toDouble())
        return ResponseEntity.ok(html)
    }

    /**
     * Retrieves the given [Recipe] by its [recipeId] and builds the screen to display it with
     * [Ingredient]s adjusted for the given number of [servings].
     */
    @Operation(
        summary = "Build the screen that shows a recipe with amounts adjusted for the desired number of servings."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The recipe",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/show-recipe/{id}/{servings}", produces = [MediaType.TEXT_HTML_VALUE])
    fun showRecipe(
        request: HttpServletRequest,
        @PathVariable("id") recipeId: Long,
        @PathVariable("servings") servings: Double
    ): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request, recipe, servings)
        return ResponseEntity.ok(html)
    }

    /**
     * Retrieves the given [Recipe] by its [recipeId] and builds the screen to display it with
     * [Ingredient]s adjusted for the given number of [servings]. Hides controls, so that the
     * page is suitable for printing. Meant to be opened in a new tab.
     */
    @Operation(
        summary = "Build the screen that shows a recipe with amounts adjusted for the desired " +
                "number of servings, suitably for printing."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "The recipe, pretty for printing",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/print-recipe/{id}/{servings}", produces = [MediaType.TEXT_HTML_VALUE])
    fun printRecipe(
        request: HttpServletRequest,
        @PathVariable("id") recipeId: Long,
        @PathVariable("servings") servings: Double
    ): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request, recipe, servings, prettyPrint = true)
        return ResponseEntity.ok(html)
    }

    private fun showRecipeAdjusted(
        request: HttpServletRequest,
        recipe: Recipe,
        servings: Double,
        prettyPrint: Boolean = false
    ): String {
        Preferences.loadPreferenceValues(request)
        val remoteHost = request.remoteHost
        val html = Preferences.initHtml(mapOf("class" to "rendered"), prettyPrint)
        val recipeId = recipe.id
        check(recipeId != null) { Preferences.getLanguageString("Null recipe id") }
        val ingredients = ingredientController.getIngredientInventory(recipeId)

        val multiplier = servings / recipe.servings

        // for edit button

        html.addBodyElement("h1")
            .addBodyText(recipe.name)
            .addWhitespace()
        if (!prettyPrint) {
            html
                .addBodyElement(
                    "input",
                    mapOf(
                        "type" to "button",
                        "value" to Preferences.getLanguageString("Edit"),
                        "onclick" to "openUrl('/souschef/edit-recipe/${recipe.id}')",
                        "class" to "title",
                    ),
                    true
                )
                .addWhitespace()
                .addBodyElement("div", mapOf("class" to "tooltip"))
                .addBodyElement(
                    "input",
                    mapOf(
                        "type" to "button",
                        "value" to Preferences.getLanguageString("Print"),
                        "onclick" to "window.open('/souschef/print-recipe/${recipe.id}/$servings')",
                        "class" to "title",
                    ),
                    true
                ).addBodyElement("span", mapOf("class" to "tooltiptext"))
                .addBodyText(Preferences.getLanguageString("printTooltip"))
                .closeBodyElement() // close span
                .closeBodyElement() // close div
        }
        html.closeBodyElement()
            .addBreak()

        val servingsStr = "servings"
        val writeNumber = ingredientFormatter.writeNumber(servings)

        // servings?
        if (prettyPrint) {
            html.addBodyText("${Preferences.getLanguageString("Servings")} " +
                    ingredientFormatter.writeNumber(servings)
            )
        } else {
            html.addBodyElement("form")
                .addBodyElement("label", Collections.singletonMap("for", "servings"))
                .addBodyText(Preferences.getLanguageString("Servings")).closeBodyElement()
                .addBodyElement(
                    "input",
                    mapOf(
                        "id" to servingsStr,
                        "name" to servingsStr,
                        "type" to "number",
                        "value" to writeNumber,
                    ),
                    true
                )
        }

        html.addBreak()

        // buttons
        if (!prettyPrint) {
            val setStr = "Set"
            html.addBodyElement(
                "input",
                mapOf(
                    "id" to setStr,
                    "name" to setStr,
                    "type" to "button",
                    "value" to Preferences.getLanguageString(setStr),
                    "onclick" to "openUrl('/souschef/show-recipe', $recipeId, 'servings')",
                ),
                true
            )

            val resetStr = "Reset"
            html
                .addBodyElement(
                    "input",
                    mapOf(
                        "id" to resetStr,
                        "name" to resetStr,
                        "type" to "button",
                        "value" to Preferences.getLanguageString(resetStr),
                        "onclick" to "openUrl('/souschef/show-recipe', $recipeId, null)",
                    ),
                    true
                )
        }

        // close form
        html.closeBodyElement()

        html.startTable()
        ingredients.forEach {
            val copy = it.copy()
            copy.amount *= multiplier
            val ingred = bestUnit(remoteHost, copy)
            html.startRow()
                .startCell().addBodyText(ingredientFormatter.writeNumber(ingred.amount)).closeBodyElement()
            html.startCell()
            val iUnit = ingred.unit
            if (iUnit != null) {
                html.addBodyText(ingredientFormatter.writeUnit(remoteHost, iUnit))
            }
            html.closeBodyElement()
            val food = foodController.getFood(ingred.itemId)
            val name: String = if (food.isPresent) food.get().name else Preferences.getLanguageString("unknown")
            html.startCell().addBodyText(name).closeBodyElement()
                .closeBodyElement()
        }
        html.closeBodyElement()
            .addBreak()
            .addBodyElement("p")
            .addBodyText(recipe.directions)
            .addBreak().addBreak()

        return html.get()
    }

    private fun unitsByPreference(type: UnitType, preference: UnitPreference): List<AUnit> {
        return when (preference) {
            UnitPreference.ANY -> unitDao.findAllByType(type)
            UnitPreference.ENGLISH -> unitDao.findAllByTypeAndIntlFalse(type)
            UnitPreference.INTERNATIONAL -> unitDao.findAllByTypeAndIntlTrue(type)
        }
    }

    private fun bestUnit(remoteHost: String, ingr: Ingredient): Ingredient {
        val unit = ingr.unit ?: return ingr     // if there is no unit, there's nothing to do

        val unitPreference = Preferences.getUnitTypes(remoteHost)
        /* One special case: we don't mind a fraction of a cup -- measuring cups come in 1/8 cup
         * to 3 cups. Values up to 6 cups should be rendered as "cups" if possible.
         */
        if (unitPreference != UnitPreference.INTERNATIONAL && unit == "cup"
            && MathUtils.geEpsilon(ingr.amount, 0.25)
            && MathUtils.leEpsilon(ingr.amount, 6.0)
        ) {
            return ingr
        }

        // Try to find the right unit in the DB, by either name or abbreviation. If none,
        // give up and use existing unit.
        val unitType = findTypeOf(unit) ?: return ingr
        val record = unitDao.findByAnyNameAndType(unit, unitType) ?: return ingr

        val baseAmount = ingr.amount * record.inBase
        val type = record.type
        val units = unitsByPreference(type, unitPreference)

        var bestUnit: AUnit? = null
        var leastAmount: Double = Double.MAX_VALUE
        for (aUnit in units) {
            val scaleAmount = baseAmount / aUnit.inBase
            if (scaleAmount < leastAmount && MathUtils.geEpsilon(scaleAmount, 1.0)) {
                bestUnit = aUnit
                leastAmount = scaleAmount
            }
        }
        if (bestUnit == null) {
            return ingr
        }

        return Ingredient(
            ingr.itemId,
            baseAmount / bestUnit.inBase,
            bestUnit.name,
            ingr.recipeId,
            ingr.id
        )
    }

    /**
     * Find whether the unit is a volume or a weight.  Most recipe ingredients
     * are measured in volume, so we check those first.
     */
    private fun findTypeOf(unit: String): UnitType? {
        val volume = volumeDao.findByAnyName(unit)
        if (volume != null) {
            return UnitType.VOLUME
        }

        val weight = weightDao.findByAnyName(unit)
        if (weight != null) {
            return UnitType.WEIGHT
        }

        return null
    }
}