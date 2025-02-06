package xyz.jimh.souschef.control

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao

@RestController
class IngredientController(private val ingredientDao: IngredientDao) {
    @GetMapping("/ingredients")
    fun getIngredients(): List<Ingredient> {
        return ingredientDao.findAll()
    }

    @GetMapping("/ingredients/{id}")
    fun getIngredient(@PathVariable id: Long): Ingredient {
        val optional = ingredientDao.findById(id)
        if (optional.isEmpty) {
            throw RuntimeException("Could not find ingredient: $id")
        }
        return optional.get()
    }

    @GetMapping("/ingredients/{recipeId}/inventory")
    fun getIngredientInventory(@PathVariable recipeId: Long): List<Ingredient> {
        return ingredientDao.findAllByRecipeId(recipeId)
    }

    @PostMapping("/ingredients")
    fun addIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }

    @PutMapping("/ingredients")
    fun updateIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }
}