/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

/*
* Ingredient is
* Item name
* Amount
* Unit (may be null or blank)
* Unit type
* */
data class IngredientToSave(val name: String, val amount: Double, val unit: String, val type: String)
