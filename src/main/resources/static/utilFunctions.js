/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

// noinspection JSUnusedGlobalSymbols

let dirty = false

function openUrl(base, p1, element) {
    let url = base;
    if (p1 !== null && p1 !== undefined) {
        url += '/' + p1
    }
    console.log(url);
    console.log(element);
    if (element !== null && element !== undefined) {
        const elementValue = document.getElementById(element).value;
        url += '/' + elementValue;
    }
    window.open(url, "_self");
}

function enableWhenNotEmpty(hostElement, element) {
    let disable = hostElement.value.trim().length === 0;
    const button = document.getElementById(element);
    if (button !== null) {
        button.disabled = disable;
    }
    dirtyWindow()
}

function dirtyWindow() {
    if (dirty) {
        return
    }
    dirty = true
    window.onbeforeunload = function (event) {
        event.returnValue = "foobar"
    }
}

function cleanWindow() {
    if (!dirty) {
        return
    }
    dirty = false
    window.onbeforeunload = null
}

function clearSelect(clearElement) {
    const element = clearElement.parentNode.firstElementChild;
    if (element !== null && element !== undefined) {
        const options = element.children
        const opt = options.item(0)
        opt.selected = true
    }
}

function deleteTableRow(row, tableId) {
    // noinspection JSUnresolvedReference
    const index = row.parentNode.parentNode.rowIndex
    document.getElementById(tableId).deleteRow(index)
}

function insertNewRow(tableId) {
    const table = document.getElementById(tableId)
    const len = table.rows.length  // before adding new row == index of new row

    // clone last ingredient row
    let newRow = table.rows[len - 1].cloneNode(true)

    newRow.childNodes.forEach(cell => {
        const child = cell.childNodes[0]
        if (child.type === "text") {
            child.value = ""
        } else if (child.type === "number") {
            child.value = ""
        } else if (child.nodeName === "SELECT") {
            clearSelect(child)
        }
    })
    table.appendChild(newRow)
}

class ResponseError extends Error {
    get response() {
        return this._response
    }

    set response(value) {
        this._response = value
    }

    constructor(message, response) {
        super(message)
        this._response = response
    }
}


/*
* Grab all data from the table, and build an object:
* Recipe id
* Recipe name
* Category
* Servings
* Ingredients array
*
* Ingredient is
* Item name
* Amount
* Unit (may be null or blank)
* */
function doSave(tableId) {
    let recipeObject = {}

    let id = document.getElementById("recipe-id").value
    recipeObject.id = (id === "null") ? null : id
    recipeObject.name = document.getElementById("recipe-title").value
    recipeObject.category = document.getElementById("category").value
    recipeObject.servings = document.getElementById("serves").value
    recipeObject.directions = document.getElementById("directions").value

    const table = document.getElementById(tableId)
    const rows = table.rows
    const numRows = rows.length
    let ingredientArray = []
    for (let index = 2; index < numRows; ++index) {
        const row = rows[index]
        let ingredient = {}
        ingredient.name = childValue(row.children[2])
        ingredient.amount = childValue(row.children[0])
        const select = row.children[1]
        ingredient.unit = childValue(select)
        ingredient.type = groupValue(select)
        ingredient.index = index;

        ingredientArray.push(ingredient)
    }

    recipeObject.ingredients = ingredientArray

    fetch("/souschef/save-recipe", {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(recipeObject)
    }).then(response => {
        if (!response.ok) {
            throw new ResponseError("problems", response)
        }
        cleanWindow()
        return response.json()
    }).then(data => {
        openUrl( "/souschef/show-recipe/" + data.id)
    }).catch(error => {
        console.error("Fetch error:", error)
        return error.response.json()
    }).then(errorData => {
        console.log(errorData)
        let msg = errorData.status + " " + errorData.error + "\n\n"
        const errs = JSON.parse(errorData.message)
        errs.errors.forEach(value => msg += value + "\n")
        alert(msg)
    })
}

function childValue(element) {
    const child = element.children[0]
    return child.value
}

function groupValue(element) {
    const child = element.children[0]  // a select
    if (child.nodeName !== "SELECT") {
        return ""
    }

    const selected = child.options[child.selectedIndex]
    let optGroup = selected.parentElement
    if (optGroup.nodeName !== "OPTGROUP") {
        return "NONE"
    }
    return optGroup.label.toUpperCase()
}

function addTextBreak(addText = "", addBreak = true, placeCursorAfter = true) {
    const text = document.getElementById("directions")
    let start = text.selectionStart
    const before = text.value.substring(0, start)
    const after = text.value.substring(start)
    const br = addBreak ? "<br/>\n" : ""
    text.value = before + addText + br + after

    if (placeCursorAfter) {
        start += br.length + addText.length
    }

    text.selectionStart = start
    text.selectionEnd = start
    text.focus()
    render()
}

let rawHtml = null
let renderedHtml = null

function render() {
    if (rawHtml === null) {
        rawHtml = document.getElementById("directions")
    }
    if (renderedHtml === null) {
        renderedHtml = document.getElementById("render-directions")
    }

    renderedHtml.innerHTML = rawHtml.value
    dirtyWindow()
}

let startListButton = null
let endListButton = null
let startItemButton = null
let endItemButton = null

function getListButtons() {
    if (startListButton === null) {
        startListButton = document.getElementById("startList")
    }
    if (endListButton === null) {
        endListButton = document.getElementById("endList")
    }
    if (startItemButton === null) {
        startItemButton = document.getElementById("startItem")
    }
    if (endItemButton === null) {
        endItemButton = document.getElementById("endItem")
    }
}
function startList() {
    getListButtons()
    startListButton.style.display = "none"
    endListButton.style.display = "inline"

    addTextBreak("<ol>\n", false)
}

function endList() {
    getListButtons()
    endListButton.style.display = "none"
    startListButton.style.display = "inline"
    addTextBreak("</ol>", true)

    render()
}

function addListItem() {
    getListButtons()
    startItemButton.style.display = "none"
    endItemButton.style.display = "inline"
    addTextBreak("<li>", false)
}

function endListItem() {
    getListButtons()
    endItemButton.style.display = "none"
    startItemButton.style.display = "inline"
    addTextBreak("</li>\n", false)
}

