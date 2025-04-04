/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import java.util.*
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao

/**
 * Controller for getting and saving [FoodItem]s.
 * @constructor Automagically built with a [FoodItemDao].
 */
@RestController
class FoodController(private val foodItemDao: FoodItemDao) {

    /**
     * Enables logging for this controller
     */
    val logger = KotlinLogging.logger {}

    /**
     * Saves a [FoodItem] in the database, passed in as [food].
     */
    @PostMapping("/foods")
    fun postFood(@RequestBody food: FoodItem): FoodItem {
        val foodItem = foodItemDao.save(food)
        logger.info { "saved ${foodItem.name} to ${foodItem.id}" }
        return foodItem
    }

    /**
     * Finds a [FoodItem] by its [id].
     */
    @GetMapping("/foods/{id}")
    fun getFood(@PathVariable id: Long): Optional<FoodItem> {
        return foodItemDao.findById(id)
    }

    /**
     * Gets all [FoodItem]s in the database.
     */
    @GetMapping("/foods")
    fun getAllFood(): List<FoodItem> {
        return foodItemDao.findAll()
    }
}