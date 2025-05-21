/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.control.UnitController
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.Weight

/**
 * Object that builds the HTML to display and edit an ingredient.
 */
object IngredientBuilder {

    private lateinit var unitController: UnitController
    private lateinit var categoryDao: CategoryDao
    private lateinit var ingredientFormatter: IngredientFormatter

    /**
     * Builds the HTML SELECT element to set the [Category] (initially set to [selected]).
     */
    fun buildCategorySelector(identifier: String, selected: String): String {
        loadCategoryDao()

        val html = StringBuilder("<select id=\"$identifier\">")

        html.append(buildOption("", selected))
        val categories = categoryDao.findAllByIdNotNullOrderByName()
        categories.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</select>")
        return html.toString()
    }

    /**
     * Builds the HTML SELECT element to choose a unit ([Weight] or [Volume]). If the
     * [Ingredient] has a unit already associated with it, that unit will be [selected],
     * and the SELECT element should mark that unit as selected.
     */
    fun buildUnitSelector(remoteHost: String, identifier: String, selected: String): String {
        loadUnitController()

        val html = StringBuilder("<select id='$identifier'>")

        val unitTypes = Preferences.getUnitTypes(remoteHost)
        val volumes = unitController.getVolumesAscending(unitTypes)
        html.append("<option value=''/>")
        html.append("<optgroup label='Volume'>")
        volumes.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</optgroup>")

        val weights = unitController.getWeightsAscending(unitTypes)
        html.append("<optgroup label='Weight'>")
        weights.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</optgroup>")
        html.append("</select>")
        html.append("<input type='button' value='x' onclick='clearSelect(this)' />")

        return html.toString()
    }

    private fun buildOption(name: String, selected: String): String {
        return when (name == selected) {
            true -> "<option value='${name}' selected='true'>${name}</option>"
            false -> "<option value='${name}'>${name}</option>"
        }
    }

    /**
     * Builds the HTML INPUT element to hold the amount of an [Ingredient], given as [value].
     */
    fun buildAmountInput(identifier: String, value: Double): String {
        loadIngredientFormatter()
        val fillValue = ingredientFormatter.writePlainNumber(value)
        return "<input id='$identifier' name='$identifier' type='number' value='$fillValue' min='0'>"
    }

    /**
     * Builds the HTML INPUT element to hold the [FoodItem], given as [value].
     */
    fun buildIngredientInput(identifier: String, value: String): String {
        return "<input id='$identifier' name='$identifier' type='text' value='$value'>"
    }

    /**
     * Builds the HTML BUTTON element to delete an ingredient (given as [parent]).
     */
    fun buildDeleteIngredient(parent: String): String {
        val delete = Preferences.getLanguageString("Delete")
        return "<input type='button' value='$delete' onclick='deleteTableRow(this, \"$parent\")'>"
    }

    private fun loadCategoryDao() {
        if (!this::categoryDao.isInitialized) {
            categoryDao = SpringContext.getBean(CategoryDao::class.java)
        }
    }

    private fun loadIngredientFormatter() {
        if (!this::ingredientFormatter.isInitialized) {
            ingredientFormatter = SpringContext.getBean(IngredientFormatter::class.java)
        }
    }

    private fun loadUnitController() {
        if (!this::unitController.isInitialized) {
            unitController = SpringContext.getBean(UnitController::class.java)
        }
    }
}
