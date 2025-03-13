/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import java.util.*
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

@RestController
class UnitController(
    private val unitDao: UnitDao,
    private val volumeDao: VolumeDao,
    private val weightDao: WeightDao
) {

//    val logger = KotlinLogging.logger {}

    @GetMapping("/units")
    fun getUnits(): List<AUnit> {
        return unitDao.findAll()
    }

    @GetMapping("/units/{name}")
    fun getUnit(@PathVariable("name") name: String): AUnit? {
        val unit = unitDao.findByName(name) ?: unitDao.findByAbbrev(name)
        return unit
    }

    @GetMapping("/volumes")
    fun getVolumes(): List<Volume> {
        return volumeDao.findAll()
    }

    @GetMapping("/volumes/{volumeId}")
    fun getVolume(@PathVariable volumeId: Long): Optional<Volume> {
        return volumeDao.findById(volumeId)
    }

    @GetMapping("/weights")
    fun getWeights(): List<Weight> {
        return weightDao.findAll()
    }

    @GetMapping("/weights/{weightId}")
    fun getWeight(@PathVariable weightId: Long): Optional<Weight> {
        return weightDao.findById(weightId)
    }

    @PutMapping("/weights")
    fun updateWeight(@RequestBody weight: Weight): Weight? {
        val weightId = weight.id
        require(weightId != null) { "WeightId can not be null" }
        val inWeight = weightDao.findById(weightId)
        check(inWeight.isPresent) { "update weight with id $weightId not found" }
        weight.id = weightId
        return weightDao.save(weight)
    }

    @PostMapping("/weights")
    fun addWeight(@RequestBody weight: Weight): Weight? {
        return weightDao.save(weight)
    }

    @PutMapping("/volumes")
    fun updateVolume(@RequestBody volume: Volume): Volume? {
        val volumeId = volume.id
        require(volumeId != null) { "VolumeId can not be null" }
        val inVolume = volumeDao.findById(volumeId)
        check(inVolume.isPresent) { "update volume with id $volumeId not found" }
        return volumeDao.save(volume)
    }

    @PostMapping("/volumes")
    fun addVolume(@RequestBody volume: Volume): Volume? {
        return volumeDao.save(volume)
    }

    @GetMapping("/volumes-ascending/{intl}")
    fun getVolumesAscending(@PathVariable intl: UnitPreference = UnitPreference.ANY): List<Volume> {
        return when (intl) {
            UnitPreference.ANY -> volumeDao.findAllByInMlGreaterThanOrderByInMl(0.0)
            UnitPreference.ENGLISH -> volumeDao.findAllByIntlIsFalseOrderByInMl()
            UnitPreference.INTERNATIONAL -> volumeDao.findAllByIntlIsTrueOrderByInMl()
        }
    }

    @GetMapping("/weights-ascending/{intl}")
    fun getWeightsAscending(@PathVariable intl: UnitPreference = UnitPreference.ANY): List<Weight> {
        return when (intl) {
            UnitPreference.ANY -> weightDao.findAllByInGramsGreaterThanOrderByInGrams(0.0)
            UnitPreference.ENGLISH -> weightDao.findAllByIntlIsFalseOrderByInGrams()
            UnitPreference.INTERNATIONAL -> weightDao.findAllByIntlIsTrueOrderByInGrams()
        }

    }
}