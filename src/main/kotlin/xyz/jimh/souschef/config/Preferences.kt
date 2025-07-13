/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import java.util.Collections.singletonMap
import java.util.Locale
import java.util.TreeMap
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.LocaleStrings
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.ResourceText

/**
 * Controller that handles preferences.
 */
@RestController
object Preferences : Broadcaster() {
    internal lateinit var preferenceDao: PreferenceDao
    internal lateinit var locale: String
    internal lateinit var languageStrings: LocaleStrings
    private lateinit var languageOptions: String
    private val resolver = PathMatchingResourcePatternResolver()

    /**
     * Starts the [HtmlBuilder] containing the preferences pane, and builds that pane. If [prettyPrint]
     * is true, builds with no controls, to make the page suitable for printing.
     */
    fun initHtml(customAttributes: Map<String, String> = emptyMap(), prettyPrint: Boolean = false): HtmlBuilder {
        val html = HtmlBuilder()

        val customStyle = customAttributes["class"]
        val style = if (customStyle != null) {
            "$customStyle no-margin"
        } else {
            "no-margin"
        }
        val bodyAttributes = mapOf("onload" to "setSelects()", "class" to style)
        html.initialize(bodyAttributes)

        if (!prettyPrint) {
            addScripts(html,"utilFunctions.js", "cookies.js", "modal.js", "sorting.js")
                .addHeaderWhitespace()
            addPreferencesPane(html)
        }

        val map = mutableMapOf("class" to "min-margin")
        customAttributes.filter { it.key != "class" }.forEach { map[it.key] = it.value }
        return html.addBodyElement("div", map)
    }

    /**
     * Get all preferences for the requesting host.
     * @param request [HttpServletRequest]
     */
    @Operation(summary = "Get all preferences for this node")
    @GetMapping("/preferences")
    fun getPreferenceValues(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val preferenceMap = loadPreferenceValues(request)
        return ResponseEntity.ok(preferenceMap)
    }

    internal fun loadPreferenceValues(request: HttpServletRequest): MutableMap<String, String> {
        val dao = loadPreferenceDao()
        val preferences = dao.findAllByHost(request.remoteHost)
        val preferenceMap = mutableMapOf<String, String>()
        preferences.forEach {
            preferenceMap[it.key] = it.value
        }
        locale = preferenceMap["language"] ?: Locale.getDefault().toString()
        loadLanguageStrings()
        return preferenceMap
    }

    internal fun loadLanguageStrings(force: Boolean = false) {
        if (force || !this::languageStrings.isInitialized) {
            val stringsResources = resolver.getResource("classpath:/static/$locale/strings")
            languageStrings = LocaleStrings.from(stringsResources)

            val directionWords = resolver.getResource("classpath:/static/direction_strings.txt")
            languageStrings.add(StringsFileLoader().load(directionWords))
        }
    }

    /**
     * Set one [Preference] value. Host comes from the [request], and [name] and [value]
     * define the preference. If [value] is blank, nothing happens.
     */
    @Operation(summary = "Save a single preference (name, value)")
    @PostMapping("/preferences/{name}/{value}")
    fun setPreferenceValue(
        request: HttpServletRequest,
        @PathVariable name: String,
        @PathVariable value: String
    ): ResponseEntity<Preference> {
        if (value.isBlank()) {
            return ResponseEntity.noContent().build()
        }
        if (name == "language") {
            locale = value
            loadLanguageStrings(force = true)
        }
        broadcast(value, name)
        val dao = loadPreferenceDao()
        val preferenceOptional = dao.findByHostAndKey(request.remoteHost, name)
        val preference = when {
            preferenceOptional.isPresent -> {
                val preference = preferenceOptional.get()
                preference.value = value
                preference
            }

            else -> Preference(request.remoteHost, name, value)
        }
        dao.save(preference)
        return ResponseEntity.ok(preference)
    }

    /**
     * Gets a single string localization by its [key]. Returns a 404 status if the
     * key does not exist in the strings file.
     */
    @Operation(summary = "Get a single string localization by its key")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "String localization has been found"),
        ApiResponse(responseCode = "404", description = "String localization could not be found"),
    ])
    @GetMapping("/localization/{key}")
    fun getLocalizationByKey(@PathVariable key: String): ResponseEntity<String> {
        return try {
            ResponseEntity.ok(getLanguageString(key))
        } catch (_: IllegalStateException) {
            ResponseEntity.notFound().build()
        }
    }

    internal fun getLanguageString(key: String): String {
        loadLanguageStrings()
        return languageStrings.get(key)
    }

    internal fun getLanguageArray(key: String): List<String> {
        loadLanguageStrings()
        return languageStrings.getArray(key)
    }

    /**
     * Delete the [name]d preference, if it exists. Returns the name of the
     * deleted preference, and whether or not it had existed previously, with
     * the appropriate status code.
     */
    @Operation(summary = "Delete a preference by name")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Preference deleted"),
        ApiResponse(responseCode = "404", description = "Preference not found")
    ])
    @DeleteMapping("/preferences/{name}")
    fun deletePreference(
        request: HttpServletRequest,
        @PathVariable name: String
    ): ResponseEntity<Map<String, String>> {
        broadcast("preference deleted", name)
        val dao = loadPreferenceDao()
        val preferenceOptional = dao.findByHostAndKey(request.remoteHost, name)
        val (status, text) = when {
            preferenceOptional.isPresent -> {
                dao.delete(preferenceOptional.get())
                HttpStatus.OK to "Preference $name deleted"
            }

            else -> HttpStatus.NOT_FOUND to "Preference $name not found"
        }
        preferenceOptional.ifPresent { dao.delete(it) }
        return ResponseEntity.status(status).body(mapOf(text to status.name))
    }

    private fun loadPreferenceDao(): PreferenceDao {
        if (!this::preferenceDao.isInitialized) {
            preferenceDao = SpringContext.getBean(PreferenceDao::class.java)
        }
        return preferenceDao
    }

    /**
     * Find one preference by [host] and [key].
     */
    fun getPreference(host: String, key: String): String? {
        val dao = loadPreferenceDao()
        val value = dao.findByHostAndKey(host, key)
        return when {
            value.isEmpty -> null
            else -> value.get().value
        }
    }

    /**
     * Get the unit types [Preference]; if unset, defaults to [UnitPreference.ANY].
     */
    fun getUnitTypes(host: String): UnitPreference {
        val value = getPreference(host, "units") ?: return UnitPreference.ANY
        return try {
            UnitPreference.valueOf(value.uppercase())
        } catch (_: IllegalArgumentException) {
            UnitPreference.ANY
        }
    }

    /**
     * Get the [Preference] for unit names (full name or abbrev.); if unset,
     * defaults to [UnitAbbrev.FULL_NAME].
     */
    fun getUnitNames(host: String): UnitAbbrev {
        val value = getPreference(host, "unitNames") ?: return UnitAbbrev.FULL_NAME
        return try {
            UnitAbbrev.valueOf(value.uppercase())
        } catch (_: IllegalArgumentException) {
            UnitAbbrev.FULL_NAME
        }
    }

    /**
     * Add script file(s) to an [HtmlBuilder] [html]. Notionally adding a Javascript file,
     * as the scripts are surrounded by a script tag pair.
     * [filenames] is a vararg list of files (in the classpath).
     */
    fun addScripts(html: HtmlBuilder, vararg filenames: String): HtmlBuilder {
        html.addHeaderElement("script", singletonMap("type", "text/javascript"))
        filenames.forEach {
            html.addHeaderText(ResourceText.getStatic(it))
        }
        return html.closeHeaderElement()
    }

    private fun addPreferencesPane(html: HtmlBuilder): HtmlBuilder {
        val rawFooter = ResourceText.getStatic("$locale/footer.html")
        val footer = addLanguageOptions(rawFooter)
        return html.addHeaderWhitespace()
            .addHeaderElement("style").addHeaderWhitespace()
            .addHeaderText(ResourceText.getStatic("preferences.css")).addHeaderWhitespace()
            .addHeaderText(ResourceText.getStatic("editor.css")).addHeaderWhitespace()
            .addHeaderText(ResourceText.getStatic("modal.css")).addHeaderWhitespace()
            .addHeaderText(ResourceText.getStatic("parser.css")).addHeaderWhitespace()
            .addHeaderText(ResourceText.getStatic("sorting.css")).addHeaderWhitespace()
            .closeHeaderElement().addHeaderWhitespace()
            .addBodyText(footer)
    }

    private fun addLanguageOptions(rawFooter: String): String {
        if (!this::languageOptions.isInitialized) {
            val resources = resolver.getResources("classpath:/static/**/strings")
            val languageMap = TreeMap<String, String>()
            resources.forEach { resource ->
                val rezMap = StringsFileLoader().load(resource, 2)
                val aLocale = rezMap.strings["locale"]
                val aLanguage = rezMap.strings["language"]
                if (aLocale != null && aLanguage != null) {
                    languageMap[aLanguage] = aLocale
                }
            }

            val builder = StringBuilder()
            languageMap.forEach { (language, locale) ->
                builder.append("<option value='$locale'>$language</option>")
            }

            languageOptions = builder.toString()
        }

        return rawFooter.replace("<!-- OPTIONS-->", languageOptions)
    }

}
