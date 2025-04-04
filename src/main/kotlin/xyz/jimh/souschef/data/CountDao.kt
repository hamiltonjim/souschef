/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Interface to a stored procedure to get the number of [Recipe]s in each
 * [Category].
 */
@Hidden
interface CountDao : JpaRepository<CategoryCount, String> {
    /**
     * Returns records of [CategoryCount].
     */
    @Query("select * from get_category_count(:includeDeleted)", nativeQuery = true)
    fun getCategoryCounts(includeDeleted: Boolean = false): List<CategoryCount>
}