/*
 * Copyright (c) 2025 Jim Hamilton. All rights reserved.
 */

const TABLE_NAME = "ingredients-table"
const toggleButtonId = "sort-ingredients"
const toggleStateId = "toggle-state"
const SORT_key = "sort"
const FINISHED_key = "finishedSort"

const DELETE_key = "Delete"
let DELETE_value

const firstIngredientRow = 3
const extraHeaderRow = 2

class eventAndHandler {
    constructor (name, handler) {
        this.name = name
        this.handler = handler
    }
}

function toggleSortIngredients() {
    const button = document.getElementById(toggleButtonId)
    const stateElement = document.getElementById(toggleStateId)
    let state = stateElement.value
    if (state === "off") {
        state = "on"
    } else {
        state = "off"
    }
    stateElement.value = state
    const newLabelKey = state === "on" ? FINISHED_key : SORT_key
    fetch("/souschef/localization/" + newLabelKey)
        .then(response => {
            if (response.status !== 200) {
                throw new Error('response code: ' + response.status)
            }
            return response.text();
        })
        .then(str => button.value = str)
        .catch(error => button.value = newLabelKey)

    fetch("/souschef/localization/" + DELETE_key)
        .then(response => {
            if (response.status !== 200) {
                throw new Error("response code: " + response.status)
            }
            return response.text()
        })
        .then(str => DELETE_value = str)
        .catch(error => DELETE_value = DELETE_key)

    if (state === "on") {
        enableDragging()
    } else {
        disableDragging(DELETE_value)
    }
}

function enableDragging() {
    const table = document.getElementById(TABLE_NAME)
    const rows = table.rows
    const dragHeader = document.createElement('th')
    dragHeader.setAttribute("colspan", "4")
    const dragKey = 'DragInstruction'
    fetch("/souschef/localization/" + dragKey)
        .then(response => {
            if (response.status !== 200) {
                throw new Error("response code: " + response.status + " fetching " + dragKey)
            }
            return response.text()
        })
        .then(str => dragHeader.innerText = str)
        .catch(error => dragHeader.innerText = dragKey)

    const row = rows[1]
    row.appendChild(dragHeader)
    for (let cellNum = 0; cellNum < 3; ++cellNum) {
        row.children[cellNum].setAttribute("hidden", "true")
    }

    const clarifyKey = 'DragClarify'
    const clarifyRow = document.createElement('tr')
    row.parentNode.insertBefore(clarifyRow, row.nextSibling)
    const clarifyHeader = document.createElement('th')
    clarifyRow.appendChild(clarifyHeader)
    clarifyHeader.setAttribute("colspan", "4")
    fetch("/souschef/localization/" + clarifyKey)
        .then(response => {
            if (response.status !== 200) {
                throw new Error("response code: " + response.status + " fetching " + dragKey)
            }
            return response.text()
        })
        .then(str => clarifyHeader.innerText = str)
        .catch(error => clarifyHeader.innerText = clarifyKey)

    const numRows = rows.length
    for (let rowNum = firstIngredientRow; rowNum < numRows; ++rowNum) {
        const row = rows[rowNum]
        const text =
            row.children[0].firstChild.value + ' ' +
            row.children[1].firstChild.value + ' ' +
            row.children[2].firstChild.value
        let cell = document.createElement("td")
        cell.setAttribute("colspan", "4")
        cell.innerText = text
        for (let cellNum = 0; cellNum < 4; ++cellNum) {
            row.children[cellNum].setAttribute("hidden", "true")
        }
        row.appendChild(cell)
        row.setAttribute("draggable", "true")
        row.classList.add("draggable")
    }

    table.addEventListener('dragover', handleDragover)
    table.addEventListener('dragstart', dragStart)
    table.addEventListener('dragend', dragEnd)
    table.addEventListener('drop', handleDrop)
}

function disableDragging() {
    const table = document.getElementById(TABLE_NAME)
    const rows = table.rows
    const numRows = rows.length
    for (let rowNum = firstIngredientRow; rowNum < numRows; ++rowNum) {
        const row = rows[rowNum]
        for (let cellNum = 0; cellNum < 4; ++cellNum) {
            row.children[cellNum].removeAttribute("hidden")
        }
        row.removeChild(row.children[4])
        row.removeAttribute("draggable")
        row.classList.remove("draggable")
    }

    const row = rows[1]
    for (let cellNum = 0; cellNum < 3; ++cellNum) {
        row.children[cellNum].removeAttribute("hidden")
    }
    row.removeChild(row.children[3])

    table.deleteRow(extraHeaderRow)

    table.removeEventListener('dragover', handleDragover)
    table.removeEventListener('dragstart', dragStart)
    table.removeEventListener('dragend', dragEnd)
    table.removeEventListener('drop', handleDrop)
}

function dragStart(event) {
    const index = event.target.rowIndex
    event.dataTransfer.setData("row", index)
}

function reEnableDragging(rows) {
    for (let rowNum = 2; rowNum < rows.length; ++rowNum) {
        rows[rowNum].classList.remove("drop-target")
        rows[rowNum].classList.add("draggable")
    }
}

function dragEnd(event) {
    const rows = document.getElementById(TABLE_NAME).rows
    reEnableDragging(rows);
}

function handleDrop(event) {
    const target = event.target
    if (target === null || target === undefined) {
        return
    }
    const table = document.getElementById(TABLE_NAME)
    const rows = document.getElementById(TABLE_NAME).rows

    const tag = target.tagName
    let targetRow
    switch (tag) {
        case 'TD':
            targetRow = target.parentNode
            break
        case 'TH':
            targetRow = table.rows[1]
            break
        default:
            console.log('invalid drop target')
            return
    }

    const sourceIndex = event.dataTransfer.getData("row")
    const sourceRow = rows[sourceIndex]
    table.deleteRow(sourceIndex)
    targetRow.parentNode.insertBefore(sourceRow, targetRow.nextSibling)
    reEnableDragging(rows)
}

function handleDragover(event) {
    event.preventDefault()
    event.dataTransfer.dropEffect = 'move'
    let targetRow = event.target.parentNode

    const rows = document.getElementById(TABLE_NAME).rows
    for (let rowNum = 2; rowNum < rows.length; rowNum++) {
        const row = rows[rowNum]
        if (row === targetRow) {
            row.classList.add("drop-target")
            row.classList.remove("draggable")
        } else {
            row.classList.add("draggable")
            row.classList.remove("drop-target")
        }
    }
    return false
}