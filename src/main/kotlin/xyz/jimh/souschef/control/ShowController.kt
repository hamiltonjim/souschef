// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.control

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.util.*
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.UnitType
import xyz.jimh.souschef.data.VolumeDao
import xyz.jimh.souschef.data.WeightDao
import xyz.jimh.souschef.display.IngredientFormatter
import xyz.jimh.souschef.utility.MathUtils

@RestController
class ShowController(
    private val foodController: FoodController,
    private val ingredientController: IngredientController,
    private val recipeController: RecipeController,
    private val unitDao: UnitDao,
    private val volumeDao: VolumeDao,
    private val weightDao: WeightDao,
    private val ingredientFormatter: IngredientFormatter,
) : Listener {

    val kLogger = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        Preferences.addListener(this)
    }

    @PreDestroy
    fun destroy() {
        Preferences.removeListener(this)
    }

    @GetMapping("/show-recipe/{id}")
    fun showRecipe(request: HttpServletRequest, @PathVariable("id") recipeId: Long): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request.remoteHost, recipe, recipe.servings.toDouble())
        return ResponseEntity.ok(html)
    }

    @GetMapping("/show-recipe/{id}/{servings}")
    fun showRecipe(
        request: HttpServletRequest,
        @PathVariable("id") recipeId: Long,
        @PathVariable("servings") servings: Double
    ): ResponseEntity<String> {
        val recipe = recipeController.getRecipe(recipeId)
        val html = showRecipeAdjusted(request.remoteHost, recipe, servings)
        return ResponseEntity.ok(html)
    }

    private fun showRecipeAdjusted(remoteHost: String, recipe: Recipe, servings: Double): String {
        val html = Preferences.initHtml()
        val recipeId = recipe.id
        check(recipeId != null) { "Null recipe id!" }
        val ingredients = ingredientController.getIngredientInventory(recipeId)

        val multiplier = servings / recipe.servings

        // for edit button

        html.addBodyElement("h1")
            .addBodyText(recipe.name)
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to "Edit",
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
            .addBodyText("Servings:").closeBodyElement()
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
                "value" to setStr,
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
                "value" to resetStr,
                "onclick" to "openUrl('/souschef/show-recipe', $recipeId, null)"
            ),
            true
        )

        // close form
        html.closeBodyElement()

        html.startTable()
        ingredients.forEach {
            it.amount *= multiplier
            val ingred = reunitize(remoteHost, it)
            html.startRow()
                .startCell().addBodyText(ingredientFormatter.writeNumber(ingred.amount)).closeBodyElement()
            html.startCell()
            val iUnit = ingred.unit
            if (iUnit != null) {
                html.addBodyText(ingredientFormatter.writeUnit(remoteHost, iUnit))
            }
            html.closeBodyElement()
            val food = foodController.getFood(ingred.itemId)
            val name: String = food?.name ?: "unknown"
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

    override fun listen(name: String, value: String) {
        kLogger.debug { "listen: $name=$value" }
    }
}