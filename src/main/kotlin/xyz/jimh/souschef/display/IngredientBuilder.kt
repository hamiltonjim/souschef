package xyz.jimh.souschef.display

import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.control.UnitController
import xyz.jimh.souschef.data.CategoryDao

object IngredientBuilder {

    private var unitController: UnitController? = null
    private var categoryDao: CategoryDao? = null
    private var ingredientFormatter: IngredientFormatter? = null

    fun buildCategorySelector(identifier: String, selected: String?): String {
        loadCategoryDao()

        val html = StringBuilder("<select id=\"$identifier\">")

        val categories = categoryDao?.findAllByIdNotNullOrderByName() ?: emptyList()
        categories.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</select>")
        return html.toString()
    }

    fun buildUnitSelector(remoteHost: String, identifier: String, selected: String?): String {
        loadUnitController()

        val html = StringBuilder("<select id='$identifier'>")

        val unitTypes = Preferences.getUnitTypes(remoteHost)
        val volumes = unitController?.getVolumesAscending(unitTypes) ?: emptyList()
        html.append("<option value=''/>")
        html.append("<optgroup label='Volume'>")
        volumes.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</optgroup>")

        val weights = unitController?.getWeightsAscending(unitTypes) ?: emptyList()
        html.append("<optgroup label='Weight'>")
        weights.forEach {
            html.append(buildOption(it.name, selected))
        }
        html.append("</optgroup>")
        html.append("</select>")
        html.append("<input type='button' value='x' onclick='clearSelect(this)' />")

        return html.toString()
    }

    fun buildAmountInput(identifier: String, value: Double): String {
        loadIngredientFormatter()
        val fillValue = ingredientFormatter?.writePlainNumber(value)
        return "<input id='$identifier' name='$identifier' type='number' value='$fillValue' min='0'>"
    }

    fun buildIngredientInput(identifier: String, value: String): String {
        return "<input id='$identifier' name='$identifier' type='text' value='$value'>"
    }

    fun buildDeleteIngredient(parent: String): String {
        return "<input type='button' value='Delete' onclick='deleteTableRow(this, \"$parent\")'>"
    }

    private fun loadCategoryDao() {
        try {
            if (categoryDao == null) {
                categoryDao = SpringContext.getBean(CategoryDao::class.java)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadIngredientFormatter() {
        if (ingredientFormatter == null) {
            ingredientFormatter = SpringContext.getBean(IngredientFormatter::class.java)
        }
    }

    private fun loadUnitController() {
        try {
            if (unitController == null) {
                unitController = SpringContext.getBean(UnitController::class.java)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun buildOption(name: String, selected: String?): String {
        return when (name == selected) {
            true -> "<option value='${name}' selected='true'>${name}</option>"
            false -> "<option value='${name}'>${name}</option>"
        }
    }
}
