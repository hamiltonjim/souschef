/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.Collections
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.Preferences.languageStrings
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Preference
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
     * Retrieves the given [Recipe] by its [recipeId] and builds the screen to display it.
     */
    @Operation(summary = "Build the screen that shows a recipe as originally written.")
    @GetMapping("/show-recipe/{id}")
    fun showRecipe(request: HttpServletRequest, @PathVariable("id") recipeId: Long): ResponseEntity<String> {
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
    @GetMapping("/show-recipe/{id}/{servings}")
    fun showRecipe(
        request: HttpServletRequest,
        @PathVariable("id") recipeId: Long,
        @PathVariable("servings") servings: Double
    ): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request, recipe, servings)
        return ResponseEntity.ok(html)
    }

    private fun showRecipeAdjusted(request: HttpServletRequest, recipe: Recipe, servings: Double): String {
        Preferences.loadPreferenceValues(request)
        val remoteHost = request.remoteHost
        val html = Preferences.initHtml()
        val recipeId = recipe.id
        check(recipeId != null) { languageStrings.get("Null recipe id") }
        val ingredients = ingredientController.getIngredientInventory(recipeId)

        val multiplier = servings / recipe.servings

        // for edit button

        html.addBodyElement("h1")
            .addBodyText(recipe.name)
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to languageStrings.get("Edit"),
                    "onclick" to "openUrl('/souschef/edit-recipe/${recipe.id}')"
                ),
                true
            )
            .closeBodyElement()
            .addBreak()

        val servingsStr = "servings"
        val writeNumber = ingredientFormatter.writeNumber(servings)

        // servings?
        html.addBodyElement("form")
            .addBodyElement("label", Collections.singletonMap("for", "servings"))
            .addBodyText(languageStrings.get("Servings")).closeBodyElement()
            .addBodyElement(
                "input",
                mapOf(
                    "id" to servingsStr,
                    "name" to servingsStr,
                    "type" to "number",
                    "value" to writeNumber,
                    "min" to writeNumber
                ),
                true
            ).addBreak()

        // buttons
        val setStr = "Set"
        html.addBodyElement(
            "input",
            mapOf(
                "id" to setStr,
                "name" to setStr,
                "type" to "button",
                "value" to languageStrings.get(setStr),
                "onclick" to "openUrl('/souschef/show-recipe', $recipeId, 'servings')"
            ),
            true
        )

        val resetStr = "Reset"
        html.addBodyElement(
            "input",
            mapOf(
                "id" to resetStr,
                "name" to resetStr,
                "type" to "button",
                "value" to languageStrings.get(resetStr),
                "onclick" to "openUrl('/souschef/show-recipe', $recipeId, null)"
            ),
            true
        )

        // close form
        html.closeBodyElement()

        html.startTable()
        ingredients.forEach {
            val copy = it.copy()
            copy.amount *= multiplier
            val ingred = reunitize(remoteHost, copy)
            html.startRow()
                .startCell().addBodyText(ingredientFormatter.writeNumber(ingred.amount)).closeBodyElement()
            html.startCell()
            val iUnit = ingred.unit
            if (iUnit != null) {
                html.addBodyText(ingredientFormatter.writeUnit(remoteHost, iUnit))
            }
            html.closeBodyElement()
            val food = foodController.getFood(ingred.itemId)
            val name: String = if (food.isPresent) food.get().name else languageStrings.get("unknown")
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

    @Suppress("SpellCheckingInspection")
    private fun reunitize(remoteHost: String, ingr: Ingredient): Ingredient {
        val unit = ingr.unit ?: return ingr     // if there is no unit, there's nothing to do

        val unitTypes = Preferences.getUnitTypes(remoteHost)
        // One special case: we don't mind a fraction of a cup -- measuring cups come in 1/8 cup to 3 cups.
        if (unitTypes != UnitPreference.INTERNATIONAL && unit == "cup"
            && MathUtils.geEpsilon(ingr.amount, 0.25)
            && MathUtils.leEpsilon(ingr.amount, 3.9)
        ) {
            return ingr
        }

        // Try to find the right unit in the DB, by either name or abbreviation. If none,
        // give up and use existing unit.
        val unitType = findTypeOf(unit) ?: return ingr
        val record = unitDao.findByAnyNameAndType(unit, unitType) ?: return ingr

        val baseAmount = ingr.amount * record.inBase
        val type = record.type
        val units = when (unitTypes) {
            UnitPreference.ANY -> unitDao.findAllByType(type)
            UnitPreference.ENGLISH -> unitDao.findAllByTypeAndIntlFalse(type)
            UnitPreference.INTERNATIONAL -> unitDao.findAllByTypeAndIntlTrue(type)
        }

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

    /**
     * Listener for changes in [Preference] values.
     */
    override fun listen(name: String, value: Any) {
        lastMessage = Pair(name, value)
        lastMessageTime = Instant.now()
        kLogger.debug { "listen: $name=$value" }
    }
}