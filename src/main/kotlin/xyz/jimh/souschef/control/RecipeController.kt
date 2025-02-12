package xyz.jimh.souschef.control

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao
import java.time.Instant

@RestController
class RecipeController(private val recipeDao: RecipeDao) {

    @PostMapping("/recipes")
    fun addRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    @GetMapping("/recipes")
    fun getRecipes() : List<Recipe> {
        return recipeDao.findAllByDeletedIsFalse()
    }

    @GetMapping("/recipes/{id}")
    fun getRecipe(@PathVariable("id") id : Long) : Recipe {
        val recipe = recipeDao.findById(id)
        check(recipe.isPresent) { "Recipe with id $id not found" }
        return recipe.get()
    }

    @GetMapping("/recipes/name/{name}")
    fun getRecipe(@PathVariable("name") name : String) : Recipe {
        val recipe = recipeDao.findByName(name)
        check(recipe.isPresent) { "Recipe with name $name not found" }
        return recipe.get()
    }

    @GetMapping("/recipes/category/{category}")
    fun getRecipes(@PathVariable("category") category : Long) : List<Recipe> {
        return recipeDao.findAllByCategoryIdAndDeletedIsFalse(category)
    }

    @PutMapping("/recipes")
    fun updateRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    @DeleteMapping("/recipes/{id}")
    fun deleteRecipe(@PathVariable("id") id : Long) : Recipe {
        val optional = recipeDao.findById(id)
        check(optional.isPresent) { "Recipe with id $id not found" }
        val recipe = optional.get()
        recipe.deleted = true
        recipe.deletedOn = Instant.now()
        return recipeDao.save(recipe)
    }

    @DeleteMapping("/recipes/name/{name}")
    fun deleteRecipe(@PathVariable("name") name : String) : Recipe {
        val optional = recipeDao.findByName(name)
        check(optional.isPresent) { "Recipe with name: $name not found" }
        val recipe = optional.get()
        recipe.deleted = true
        recipe.deletedOn = Instant.now()
        return recipeDao.save(recipe)
    }
}