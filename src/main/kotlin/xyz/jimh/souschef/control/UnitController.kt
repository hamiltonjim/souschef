/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import java.util.Optional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.VolumeDao
import xyz.jimh.souschef.data.Weight
import xyz.jimh.souschef.data.WeightDao

/**
 * Controller that provides a direct interface to the units table in the database.
 * @constructor Automagically built with [UnitDao], [VolumeDao], and [WeightDao].
 */
@RestController
class UnitController(
    private val unitDao: UnitDao,
    private val volumeDao: VolumeDao,
    private val weightDao: WeightDao
) {

//    val logger = KotlinLogging.logger {}

    /**
     * Gets a list of all [AUnit]s.
     */
    @Operation(summary = "Get all units as a list")
    @GetMapping("/units")
    fun getUnits(): List<AUnit> {
        return unitDao.findAll()
    }

    /**
     * Gets a unit by its [name].
     */
    @Operation(summary = "Look up a unit by its name")
    @GetMapping("/units/{name}")
    fun getUnit(@PathVariable("name") name: String): AUnit? {
        val unit = unitDao.findByName(name) ?: unitDao.findByAbbrev(name)
        return unit
    }

    /**
     * Gets a list of all units of [Volume].
     */
    @Operation(summary = "Get all volume amounts as a list")
    @GetMapping("/volumes")
    fun getVolumes(): List<Volume> {
        return volumeDao.findAll()
    }

    /**
     * Gets a [Volume] by its [volumeId].
     */
    @Operation(summary = "Get a volume by its unique ID (among volumes)")
    @GetMapping("/volumes/{volumeId}")
    fun getVolume(@PathVariable volumeId: Long): Optional<Volume> {
        return volumeDao.findById(volumeId)
    }

    /**
     * Gets a list of all units of [Weight].
     */
    @Operation(summary = "Get all weight amounts as a list")
    @GetMapping("/weights")
    fun getWeights(): List<Weight> {
        return weightDao.findAll()
    }

    /**
     * Gets a [Weight] by its [weightId].
     */
    @Operation(summary = "Get a volume by its unique ID (among weights)")
    @GetMapping("/weights/{weightId}")
    fun getWeight(@PathVariable weightId: Long): Optional<Weight> {
        return weightDao.findById(weightId)
    }

    /**
     * Saves changes to a unit of [Weight].
     */
    @Operation(summary = "Update a weight unit")
    @PutMapping("/weights")
    fun updateWeight(@RequestBody weight: Weight): Weight? {
        val weightId = weight.id
        require(weightId != null) { "WeightId can not be null" }
        val inWeight = weightDao.findById(weightId)
        check(inWeight.isPresent) { "update weight with id $weightId not found" }
        weight.id = weightId
        return weightDao.save(weight)
    }

    /**
     * Saves a new unit of [Weight].
     */
    @Operation(summary = "Create a new weight unit")
    @PostMapping("/weights")
    fun addWeight(@RequestBody weight: Weight): Weight? {
        return weightDao.save(weight)
    }

    /**
     * Saves changes to a unit of [Volume].
     */
    @Operation(summary = "Update a volume unit")
    @PutMapping("/volumes")
    fun updateVolume(@RequestBody volume: Volume): Volume? {
        val volumeId = volume.id
        require(volumeId != null) { "VolumeId can not be null" }
        val inVolume = volumeDao.findById(volumeId)
        check(inVolume.isPresent) { "update volume with id $volumeId not found" }
        return volumeDao.save(volume)
    }

    /**
     * Saves a new unit of [Volume].
     */
    @Operation(summary = "Create a new volume unit")
    @PostMapping("/volumes")
    fun addVolume(@RequestBody volume: Volume): Volume? {
        return volumeDao.save(volume)
    }

    /**
     * Gets all [Volume]s of the given [UnitPreference], in order by ascending volume.
     */
    @Operation(summary = "Get a list of volume units, sorted by ascending volume",
        description = "Use the 'intl' parameter to get only English, only international, or all units. " +
                "The value in the field 'inBase' is the number of milliliters for each unit."
    )
    @GetMapping("/volumes-ascending/{intl}")
    fun getVolumesAscending(@PathVariable intl: UnitPreference = UnitPreference.ANY): List<Volume> {
        return when (intl) {
            UnitPreference.ANY -> volumeDao.findAllByInBaseGreaterThanOrderByInBase(0.0)
            UnitPreference.ENGLISH -> volumeDao.findAllByIntlIsFalseOrderByInBase()
            UnitPreference.INTERNATIONAL -> volumeDao.findAllByIntlIsTrueOrderByInBase()
        }
    }

    /**
     * Gets all [Weight]s of the given [UnitPreference], in order by ascending mass.
     */
    @Operation(summary = "Get a list of weight units, sorted by ascending mass",
        description = "Use the 'intl' parameter to get only English, only international, or all units. " +
                "The value in the field 'inBase' is the number of grams for each unit."
    )
    @GetMapping("/weights-ascending/{intl}")
    fun getWeightsAscending(@PathVariable intl: UnitPreference = UnitPreference.ANY): List<Weight> {
        return when (intl) {
            UnitPreference.ANY -> weightDao.findAllByInBaseGreaterThanOrderByInBase(0.0)
            UnitPreference.ENGLISH -> weightDao.findAllByIntlIsFalseOrderByInBase()
            UnitPreference.INTERNATIONAL -> weightDao.findAllByIntlIsTrueOrderByInBase()
        }

    }
}