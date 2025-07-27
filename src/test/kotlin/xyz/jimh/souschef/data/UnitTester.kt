/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.control.UnitController

class UnitTester {

    private lateinit var unitDao: UnitDao
    private lateinit var volumeDao: VolumeDao
    private lateinit var weightDao: WeightDao

    private lateinit var controller: UnitController

    @BeforeEach
    fun setUp() {
        unitDao = mockk()
        volumeDao = mockk()
        weightDao = mockk()

        controller = UnitController(unitDao, volumeDao, weightDao)

        val stringSlot = slot<String>()
        every { unitDao.findByName(capture(stringSlot)) } answers { unitList.firstOrNull { it.name == stringSlot.captured } }
        every { unitDao.findByAbbrev(capture(stringSlot)) } answers { unitList.firstOrNull { it.abbrev == stringSlot.captured } }
        every { volumeDao.findAll() } answers {
            unitList.filter { it.type == UnitType.VOLUME }.map { Volume(it) }
        }
        every { weightDao.findAll() } answers {
            unitList.filter { it.type == UnitType.WEIGHT }.map { Weight(it) }
        }
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(unitDao, volumeDao, weightDao)
    }

    /*
     * This LOOKS like it's actually testing the test; in reality, it's testing the
     * AUnit constructors in Weight and Volume.
     */
    @Test
    fun `units convert to their real types`() {
        val volumes = controller.getVolumes()
        val weights = controller.getWeights()
        assertAll(
            { assertEquals(10, volumes.size, "incorrect volume count") },
            { assertEquals(7, weights.size, "incorrect weight count") },
        )

        verify {
            volumeDao.findAll()
            weightDao.findAll()
        }
    }

    @Test
    fun `equals and hashCode work as expected`() {
        val volumes = controller.getVolumes()
        volumes.forEach {
            val unit = controller.getUnit(it.name)
            assertAll(
                { assertEquals(it.name, unit?.name, "Volume ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.id, unit?.id, "Volume ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.inBase, unit?.inBase, "Volume ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.intl, unit?.intl, "Volume ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.abbrev, unit?.abbrev, "Volume ${it.name} is not equal to AUnit ${unit?.name}") },
            )
        }

        val weights = controller.getWeights()
        weights.forEach {
            val unit = controller.getUnit(it.name)
            assertAll(
                { assertEquals(it.name, unit?.name, "Weight ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.id, unit?.id, "Weight ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.inBase, unit?.inBase, "Weight ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.intl, unit?.intl, "Weight ${it.name} is not equal to AUnit ${unit?.name}") },
                { assertEquals(it.abbrev, unit?.abbrev, "Weight ${it.name} is not equal to AUnit ${unit?.name}") },
            )
        }

        verify {
            unitDao.findByName(allAny())
            volumeDao.findAll()
            weightDao.findAll()
        }
    }

    companion object {
        val unitList = listOf(
            AUnit(1, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(2, "pint", UnitType.VOLUME, 473.176473, false, "pt."),
            AUnit(3, "quart", UnitType.VOLUME, 946.352946, false, "qt."),
            AUnit(4, "gallon", UnitType.VOLUME, 3785.411784, false, "gal."),
            AUnit(5, "liter", UnitType.VOLUME, 1000.0, true, "l"),
            AUnit(6, "fluid ounce", UnitType.VOLUME, 29.57352956, false, "fl.oz."),
            AUnit(7, "tablespoon", UnitType.VOLUME, 14.78676478, false, "tbsp."),
            AUnit(8, "teaspoon", UnitType.VOLUME, 4.92892159, false, "tsp."),
            AUnit(9, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(10, "firkin", UnitType.VOLUME, 34068.706056, false,"fk"),

            AUnit(1, "ounce", UnitType.WEIGHT, 28.34952312, false, "oz."),
            AUnit(2, "pound", UnitType.WEIGHT, 453.59237, false, "lb."),
            AUnit(3, "kilogram", UnitType.WEIGHT, 1000.0, true, "kg"),
            AUnit(4, "dram", UnitType.WEIGHT, 1.7718452, false),
            AUnit(5, "stone", UnitType.WEIGHT, 6350.29318, false, "st."),
            AUnit(6, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(7, "slug", UnitType.WEIGHT, 14593.90293721, false),
        )

    }
}