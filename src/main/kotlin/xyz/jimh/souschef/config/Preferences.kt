package xyz.jimh.souschef.config

import java.util.Collections.singletonMap
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.ResourceText

@RestController
object Preferences {
    var unitTypes: UnitPreference = UnitPreference.ENGLISH
    var unitNameSetting: UnitAbbrev = UnitAbbrev.FULL_NAME

    private val listeners = ArrayList<Listener>()

    fun addListener(listener: Listener) {
//        listeners.add(listener)
        listeners += listener
    }

    fun removeListener(listener: Listener) {
//        listeners.remove(listener)
        listeners -= listener
    }

    fun initHtml(baseUrl: String): HtmlBuilder {
        val html = HtmlBuilder()
        html.initialize(singletonMap("onload", "setSelects()"))

        addScripts(html, baseUrl, "utilFunctions.js", "cookies.js").addHeaderWhitespace()
        return addPrefsPane(html, baseUrl)
    }

    @GetMapping("/roundTest/{places}")
    fun roundTest(@PathVariable places: Int): ResponseEntity<Double> {
        return ResponseEntity.ok((1000.0 * PI).round(places))
    }

    @PostMapping("/preferences/{name}/{value}")
    fun setPreferenceValue(@PathVariable name: String, @PathVariable value: String?) {
        if (value.isNullOrBlank()) {
            return
        }
        when (name) {
            "Units" -> unitTypes = UnitPreference.valueOf(value.uppercase())
            "unitNames" -> unitNameSetting = UnitAbbrev.valueOf(value.uppercase())
            else -> throw RuntimeException("Unknown preference name: $name ($value)")
        }
        listeners.forEach { it.listen(name, value) }
    }

    fun addScripts(html: HtmlBuilder, baseUrl: String, vararg filenames: String): HtmlBuilder {
        val map = HashMap<String, String>()
        map["type"] = "text/javascript"
        html.addHeaderElement("script", map)
        filenames.forEach {
            val text = ResourceText.get(it).replace("BASEURL", baseUrl)
            html.addHeaderText(text)
        }
        return html.closeHeaderElement()
    }

    private fun addPrefsPane(html: HtmlBuilder, baseUrl: String): HtmlBuilder {
        val footer = ResourceText.get("footer.html").replace("BASEURL", baseUrl)
        return html.addHeaderWhitespace()
            .addHeaderElement("style").addHeaderWhitespace()
            .addHeaderText(ResourceText.get("preferences.css")).addHeaderWhitespace()
            .closeHeaderElement().addHeaderWhitespace()
            .addBodyText(footer)
    }

}

fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}
