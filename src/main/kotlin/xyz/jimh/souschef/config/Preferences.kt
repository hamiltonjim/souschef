/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import jakarta.servlet.http.HttpServletRequest
import java.util.Collections.singletonMap
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.ResourceText

@RestController
object Preferences {
    private var preferenceDao: PreferenceDao? = null

    private val listeners = ArrayList<Listener>()

    fun addListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

    fun initHtml(): HtmlBuilder {
        val html = HtmlBuilder()
        html.initialize(singletonMap("onload", "setSelects()"))

        addScripts(html,"utilFunctions.js", "cookies.js").addHeaderWhitespace()
        return addPreferencesPane(html)
    }

    @GetMapping("/preferences")
    fun getPreferenceValues(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val dao = loadPreferenceDao() ?: return ResponseEntity.ok(emptyMap())
        val preferences = dao.findAllByHost(request.remoteHost)
        val preferenceMap = mutableMapOf<String, String>()
        preferences.forEach {
            preferenceMap[it.key] = it.value
        }
        return ResponseEntity.ok(preferenceMap)
    }

    @PostMapping("/preferences/{name}/{value}")
    fun setPreferenceValue(request: HttpServletRequest, @PathVariable name: String, @PathVariable value: String?) {
        if (value.isNullOrBlank()) {
            return
        }
        listeners.forEach { it.listen(name, value) }
        val dao = loadPreferenceDao() ?: return
        val preferenceOptional = dao.findByHostAndKey(request.remoteHost, name)
        val preference = when {
            preferenceOptional.isPresent -> {
                preferenceOptional.get().value = value
                preferenceOptional.get()
            }

            else -> Preference(request.remoteHost, name, value)
        }
        dao.save(preference)
    }

    private fun loadPreferenceDao(): PreferenceDao? {
        if (preferenceDao == null) {
            preferenceDao = SpringContext.getBean(PreferenceDao::class.java)
        }
        return preferenceDao
    }

    fun getPreference(host: String, key: String): String? {
        val dao = loadPreferenceDao() ?: return null
        val value = dao.findByHostAndKey(host, key)
        return when {
            value.isEmpty -> null
            else -> value.get().value
        }
    }

    fun getUnitTypes(host: String): UnitPreference {
        val value = getPreference(host, "units") ?: return UnitPreference.ANY
        return try {
            UnitPreference.valueOf(value.uppercase())
        } catch (ex: IllegalArgumentException) {
            UnitPreference.ANY
        }
    }

    fun getUnitNames(host: String): UnitAbbrev {
        val value = getPreference(host, "unitNames") ?: return UnitAbbrev.FULL_NAME
        return try {
            UnitAbbrev.valueOf(value.uppercase())
        } catch (ex: IllegalArgumentException) {
            UnitAbbrev.FULL_NAME
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun addScripts(html: HtmlBuilder, vararg filenames: String): HtmlBuilder {
        html.addHeaderElement("script", singletonMap("type", "text/javascript"))
        filenames.forEach {
            html.addHeaderText(ResourceText.get(it))
        }
        return html.closeHeaderElement()
    }

    private fun addPreferencesPane(html: HtmlBuilder): HtmlBuilder {
        val footer = ResourceText.get("footer.html")
        return html.addHeaderWhitespace()
            .addHeaderElement("style").addHeaderWhitespace()
            .addHeaderText(ResourceText.get("preferences.css")).addHeaderWhitespace()
            .closeHeaderElement().addHeaderWhitespace()
            .addBodyText(footer)
    }

}
