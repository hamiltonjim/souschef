/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import xyz.jimh.souschef.data.CountDao

@Component
class RecipeCount(val countDao: CountDao) : InfoContributor {
    override fun contribute(builder: Info.Builder?) {
        require(builder != null) { "builder must not be null" }
        val counts = countDao.getCategoryCounts()
        builder.withDetail("recipesByCategory", counts)
    }
}