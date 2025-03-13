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

@RestController
class FoodController(private val foodItemDao: FoodItemDao) {

    val logger = KotlinLogging.logger {}

    @PostMapping("/foods")
    fun postFood(@RequestBody food: FoodItem): FoodItem {
        val foodItem = foodItemDao.save(food)
        logger.info { "saved ${foodItem.name} to ${foodItem.id}" }
        return foodItem
    }

    @GetMapping("/foods/{id}")
    fun getFood(@PathVariable id: Long): Optional<FoodItem> {
        return foodItemDao.findById(id)
    }

    @GetMapping("/foods")
    fun getAllFood(): List<FoodItem> {
        return foodItemDao.findAll()
    }
}