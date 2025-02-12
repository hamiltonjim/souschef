// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.config

enum class UnitPreference(private val label: String) {
    INTERNATIONAL("International"),
    ENGLISH("English"),
    ANY("Any");

    @Suppress("unused")
    fun getLabel(): String {
        return label
    }
}