/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.Optional
import org.springframework.http.MediaType
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
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The list of units",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/units", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],)
    fun getUnits(): List<AUnit> {
        return unitDao.findAll()
    }

    /**
     * Gets a unit by its [name].
     */
    @Operation(summary = "Look up a unit by its name")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested unit",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/units/{name}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getUnit(@PathVariable("name") name: String): AUnit? {
        val unit = unitDao.findByName(name) ?: unitDao.findByAbbrev(name)
        return unit
    }

    /**
     * Gets a list of all units of [Volume].
     */
    @Operation(summary = "Get all volume amounts as a list")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested volume amounts",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/volumes", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getVolumes(): List<Volume> {
        return volumeDao.findAll()
    }

    /**
     * Gets a [Volume] by its [volumeId].
     */
    @Operation(summary = "Get a volume by its unique ID (among volumes)")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested volume",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/volumes/{volumeId}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getVolume(@PathVariable volumeId: Long): Optional<Volume> {
        return volumeDao.findById(volumeId)
    }

    /**
     * Gets a list of all units of [Weight].
     */
    @Operation(summary = "Get all weight amounts as a list")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested weight amounts",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/weights", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getWeights(): List<Weight> {
        return weightDao.findAll()
    }

    /**
     * Gets a [Weight] by its [weightId].
     */
    @Operation(summary = "Get a weight by its unique ID (among weights)")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested weight",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/weights/{weightId}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getWeight(@PathVariable weightId: Long): Optional<Weight> {
        return weightDao.findById(weightId)
    }

    /**
     * Saves changes to a unit of [Weight].
     */
    @Operation(summary = "Update a weight unit")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The saved weight",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PutMapping(
        "/weights",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
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
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The new weight",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/weights",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun addWeight(@RequestBody weight: Weight): Weight? {
        return weightDao.save(weight)
    }

    /**
     * Saves changes to a unit of [Volume].
     */
    @Operation(summary = "Update a volume unit")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The saved volume unit",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PutMapping(
        "/volumes",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
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
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The new volume unit",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/volumes",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
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
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The volumes, by ascending",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        value = ["/volumes-ascending","/volumes-ascending/{intl}"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
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
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The weights, sorted by ascending mass",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        value = ["/weights-ascending","/weights-ascending/{intl}"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun getWeightsAscending(@PathVariable intl: UnitPreference = UnitPreference.ANY): List<Weight> {
        return when (intl) {
            UnitPreference.ANY -> weightDao.findAllByInBaseGreaterThanOrderByInBase(0.0)
            UnitPreference.ENGLISH -> weightDao.findAllByIntlIsFalseOrderByInBase()
            UnitPreference.INTERNATIONAL -> weightDao.findAllByIntlIsTrueOrderByInBase()
        }

    }
}