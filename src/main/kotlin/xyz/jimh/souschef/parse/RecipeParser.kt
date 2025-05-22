/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.createTempFile
import mu.KotlinLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.HtmlElements
import xyz.jimh.souschef.display.HtmlElements.TABLE_NAME
import xyz.jimh.souschef.display.IngredientBuilder
import xyz.jimh.souschef.display.IngredientFormatter
import xyz.jimh.souschef.display.ResourceText

/**
 * Controller class that handles actually parsing a recipe, and displaying the page where it's entered.
 */
@RestController
class RecipeParser(private val ingredientFormatter: IngredientFormatter) {

    private val kLogger = KotlinLogging.logger {}

    /**
     * This function actually builds the web page where one can either:
     * 1. Enter (paste) a recipe as text; or
     * 2. Select a file containing a recipe. We recognize either text files or PDF files.
     */
    @Operation(summary = "Build the screen that parses recipes")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Success",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        )
    ])
    @GetMapping("/parser/screen")
    fun buildParserScreen(request: HttpServletRequest): ResponseEntity<String> {
        val html = screenTop(request)
        return ResponseEntity.ok(html.get())
    }

    private fun screenTop(request: HttpServletRequest): HtmlBuilder {
        Preferences.loadPreferenceValues(request)
        val html = Preferences.initHtml(mapOf("class" to "rendered"))
        Preferences.addScripts(html, "parser.js")

        html.addBodyElement("h1", mapOf("class" to "centered"))
            .addBodyText(Preferences.getLanguageString("parser-title"))
            .closeBodyElement()

        html.addBodyElement("table")
            .addBodyElement("tr")
            .addBodyElement("th")
            .addBodyText(Preferences.getLanguageString("paste-recipe")).closeBodyElement()
            .addBodyElement("th")
            .addBodyText(Preferences.getLanguageString("or")).closeBodyElement()
            .addBodyElement("th")
            .addBodyText(Preferences.getLanguageString("choose-recipe-file")).closeBodyElement()
            .closeBodyElement() // row

            .addBodyElement("tr")
            .addBodyElement("td", mapOf("class" to "text-box-cell centered"))
            .addBodyElement(
                "textarea",
                mapOf(
                    "rows" to "10",
                    "cols" to "80",
                    "id" to "to-parse",
                    "onkeyup" to "checkLoadFromScreenEnabled(this)",
                )
            )
            .closeBodyElement()
            .addBreak().addBreak()
            .addBodyElement(
                "input", mapOf(
                    "type" to "button",
                    "id" to "load-from-screen",
                    "value" to Preferences.getLanguageString("load"),
                    "onclick" to "loadRecipeFromScreen()",
                    "disabled" to "true",
                )
            ).closeBodyElement()
            .closeBodyElement()
            .addBodyElement("td", closing = true)
            .addBodyElement("td", mapOf("class" to "button-cell centered"))
            .addBodyElement("form", mapOf("action" to "/souschef/parser/recipeFromFile"))
            .addBodyElement("label", mapOf("for" to "chooser"))
            .addBodyText(Preferences.getLanguageString("chooser-label"))
            .closeBodyElement() // label
            .addBodyElement("input", mapOf(
                "id" to "chooser",
                "type" to "file",
                "name" to "chooser",
                "value" to Preferences.getLanguageString("chooser-button"),
                "onchange" to "loadRecipeFile(this)"
            ), closing = true)
            .closeBodyElement(/* form */).closeBodyElement(/* td */)
            .closeBodyElement() // row
            .closeBodyElement() // table

        html.addBodyElement("hr", closing = true)
        return html
    }

    /**
     * This function parses the recipe entered into the textarea on the parser screen.
     */
    @Operation(summary = "Build the screen parsing the recipe on the previous screen")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Success",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    ])
    @PostMapping("/parser/recipeFromScreen")
    fun recipeFromScreen(request: HttpServletRequest, @RequestBody toParse: String): ResponseEntity<String> {
        val html = screenTop(request)
        val reader = toParse.reader()
        reader.use { parseRecipe(it, html, request.remoteHost) }

        return ResponseEntity.ok(html.get())
    }

    /**
     * Attempts to read a file chosen by the client, and parse out a recipe. This function recognizes
     * the following media types:
     * 1. text/plain; and
     * 1. application/pdf
     */
    @Operation(summary = "Build the screen parsing the recipe read from a file")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Success",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
        ApiResponse(
            responseCode = "415",
            description = "Unsupported media type",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        )
    ])
    @PostMapping("/parser/recipeFromFile")
    fun recipeFromFile(
        request: HttpServletRequest,
        @RequestParam type: String,
        @RequestBody content: String
    ): ResponseEntity<String> {
        val html = screenTop(request)
        val text = when (type) {
            "application/pdf" -> readPdfText(content)
            "text/plain" -> content
            else -> return fileTypeErrorResponse(html, type)
        }
        val reader = text.reader()
        reader.use { parseRecipe(it, html, request.remoteHost) }

        return ResponseEntity.ok(html.get())
    }

    private fun fileTypeErrorResponse(html: HtmlBuilder, fileType: String): ResponseEntity<String> {
        val status = HttpStatus.UNSUPPORTED_MEDIA_TYPE
        html.addBodyElement("h1", mapOf("class" to "centered"))
            .addBodyText("${status.value()} ${status.reasonPhrase}")
            .closeBodyElement()
            .addBreak().addBreak()
            .addBodyElement("p", mapOf("class" to "centered"))
            .addBodyText(Preferences.getLanguageString(status.reasonPhrase))
            .addBodyText(fileType)
            .closeBodyElement()
        return ResponseEntity.status(status).body(html.get())
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun readPdfText(content: String): String {
        val decoded = Base64.decode(content)
        val file = createTempFile("recipes", ".pdf").toFile()
        try {
            file.writeBytes(decoded)
            PDDocument.load(file).use {
                val stripper = PDFTextStripper()
                return stripper.getText(it)
            }
        } finally {
            file.delete()
        }
    }

    /**
     * Read the recipe and separate it into parts.
     * Assumptions:
     *     1. The first line is the recipe title.
     *     2. The next lines are ingredients, which have format number [unit] ingredient
     *     3. At some point, there <em>might be</em> a number of servings
     *     4. finally, directions.
     */
    private fun parseRecipe(reader: Reader, html: HtmlBuilder, remoteHost: String) {
        html.addHeaderWhitespace().addHeaderElement("style")
            .addHeaderWhitespace().addHeaderText(ResourceText.getStatic("editor.css"))
            .addHeaderWhitespace().closeHeaderElement()

        // category
        HtmlElements.addCategorySelector(html)

        val bufferedReader = BufferedReader(reader, BUFFER_SIZE)
        bufferedReader.use { bReader ->

            // title
            parseTitle(bReader, html)

            // servings: Try to find a line with number of servings info.  Mark the stream to this position.
            val servesString = Preferences.getLanguageString("Serves")
            val servingsString = Preferences.getLanguageString("Servings")
            val serves = findServings(bReader, servesString, servingsString)

            buildServingsController(html, servesString, serves)

            // ingredients
            val ingredientLines = parseIngredients(bReader, servesString, servingsString)
            buildIngredientsTable(html, ingredientLines, remoteHost)

            html.addBodyElement("textarea", mapOf("rows" to "10", "cols" to "100", "id" to "directions"))
            while (true) {
                val line = bReader.readLine() ?: break
                html.addBodyText(line).addBodyText("\n")
            }
            html.closeBodyElement()
        }
    }

    private fun buildServingsController(html: HtmlBuilder, servesString: String, serves: Int) {
        html.addBodyElement("label", mapOf("for" to "serves"))
            .addBodyText(servesString).closeBodyElement()
            .addBodyElement(
                tag = "input",
                attributes = mapOf(
                    "type" to "number",
                    "min" to "0",
                    "id" to "serves",
                    "value" to ingredientFormatter.writeNumber(serves.toDouble())
                ),
                closing = true
            )
            .addBreak()
    }

    private fun parseIngredients(
        bReader: BufferedReader,
        servesString: String,
        servingsString: String
    ): List<String> {
        val splitter = IngredientSplitter()
        while (true) {
            bReader.mark(BUFFER_SIZE)
            val line = bReader.readLine() ?: break
            if (line.contains(servesString) || line.contains(servingsString)) continue
            val iParser = IngredientParser(line)
            if (!iParser.isIngredient()) {
                bReader.reset()
                break
            }
            splitter.read(line)
        }
        return splitter.getIngredients()
    }

    private fun buildIngredientsTable(
        html: HtmlBuilder,
        ingredientLines: List<String>,
        remoteHost: String
    ) {
        HtmlElements.startEditIngredientsTable(html)
        for ((counter, line) in ingredientLines.withIndex()) {
            val iParser = IngredientParser(line)
            iParser.findIngredient()
            html.startRow()

                .startCell()
                .addBodyText(IngredientBuilder.buildAmountInput("amount-$counter", iParser.amount))
                .closeBodyElement()

                .startCell()
                .addBodyText(
                    IngredientBuilder.buildUnitSelector(
                        remoteHost,
                        "unit-$counter",
                        iParser.unit?.name ?: ""
                    )
                )
                .closeBodyElement()

                .startCell()
                .addBodyText(IngredientBuilder.buildIngredientInput("ingred-$counter", iParser.item))
                .closeBodyElement(/* cell */).closeBodyElement(/* row */)
        }
        html.closeBodyElement() // table
    }

    private fun parseTitle(bReader: BufferedReader, html: HtmlBuilder) {
        val title = bReader.readLine() ?: throw IOException("Empty Recipe")
        html.addBodyElement("div", mapOf("class" to "title"))
            .addBodyText(Preferences.getLanguageString("title"))
            .addBodyElement(
                "input", mapOf(
                    "type" to "text",
                    "id" to "recipe-title",
                    "value" to title,
                    "class" to "title",
                ), closing = true
            )
            .addWhitespace()
            .addBodyElement(
                "input", mapOf(
                    "type" to "button",
                    "id" to "save",
                    "value" to Preferences.getLanguageString("Save"),
                    "onclick" to "doSave('$TABLE_NAME')",
                    "class" to "title",
                )
            ).closeBodyElement()
            .closeBodyElement() // div

            .addBodyElement(
                tag = "input",
                attributes = mapOf(
                    "type" to "hidden",
                    "id" to "recipe-id",
                    "value" to "",
                ),
                closing = true
            )
    }

    // directions

    internal fun findServings(bReader: BufferedReader, vararg servesStrings: String): Int {
        bReader.mark(BUFFER_SIZE)
        try {
            while (true) {
                val line = bReader.readLine() ?: break
                for (serve in servesStrings) {
                    if (line.contains(serve, ignoreCase = true)) {
                        val after = line.lowercase().substringAfter(serve.lowercase())
                        val number = NumberReader.parseNumber(after)
                        return number.toInt()
                    }
                }
            }
        } catch (e: NumberFormatException) {
            kLogger.debug { "found servings string, but no number found" }
        } finally {
            bReader.reset()
        }

        return 0    // not found
    }

    companion object {
        /**
         *  The buffer size used by BufferedReader objects that read files/strings.
         */
        const val BUFFER_SIZE = 1 shl 18    // 256k bytes
    }
}