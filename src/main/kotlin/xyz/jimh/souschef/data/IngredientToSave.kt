/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

/**
 * An [Ingredient] that is about to be saved with a [Recipe]
 * @property name food name
 * @property amount amount used
 * @property unit name of the unit
 * @property type type of the unit (WEIGHT or VOLUME)
 */
data class IngredientToSave(val name: String, val amount: Double, val unit: String, val type: String)
