/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryCount
import xyz.jimh.souschef.data.CountDao
import xyz.jimh.souschef.data.Recipe

/**
 * [InfoContributor] that returns the number of [Recipe]s in
 * each [Category].
 * @property countDao The object that accesses the stored procedure which fills the count table.
 */
@Component
class RecipeCount(val countDao: CountDao) : InfoContributor {
    /**
     * Returns the [CategoryCount] for each [Category] in a JSON document.
     */
    override fun contribute(builder: Info.Builder?) {
        require(builder != null) { "builder must not be null" }
        val counts = countDao.getCategoryCounts()
        builder.withDetail("recipesByCategory", counts)
    }
}