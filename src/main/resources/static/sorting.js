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

class eventAndHandler {
    constructor (name, handler) {
        this.name = name
        this.handler = handler
    }
}

let rowStarts
function fillRowStartsArray() {
    const table = document.getElementById(TABLE_NAME)
    const rows = table.rows

    const tableOffset = table.offsetTop
    rowStarts = new Array(rows.length + 1).fill(0)
    for (index = 2; index < rows.length; index++) {
        const row = rows[index]
        rowStarts[index] = row.offsetTop + (row.offsetHeight >> 1) + tableOffset
    }
    const lastRow = rows[rows.length - 1]
    rowStarts[rowStarts.length - 1] = lastRow.offsetTop + lastRow.offsetHeight + tableOffset
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
        fillRowStartsArray()
        enableDragging()
    } else {
        disableDragging(DELETE_value)
    }
}

function enableDragging() {
    const dragButton = document.createElement("img")
    dragButton.width = "40"
    dragButton.height = "20"
    dragButton.src = "../drag.png"
    placeElementInCells(true, dragButton, TABLE_NAME, 3, 2, -1,
        [
            new eventAndHandler("dragstart", function(event) { dragStart(event) }),
            new eventAndHandler("dragend", function(event) { dragEnd(event) })
    ])
}

function disableDragging(label) {
    const deleteButton = document.createElement("input")
    deleteButton.type = "button"
    deleteButton.value = label
    placeElementInCells(false, deleteButton, TABLE_NAME, 3, 2, -1,
        [new eventAndHandler("click", function () { deleteTableRow(this, TABLE_NAME) })])
}

function placeElementInCells(enabling, element, tableId, column, firstRow, lastRow = -1, events = null) {
    const table = document.getElementById(tableId)
    const rows = table.rows
    const numRows = rows.length

    if (enabling) {
        table.addEventListener("dragover", function (event) { handleDragover(event) })
        table.addEventListener("drop", function (event) { handleDrop(event) })
    } else {
        table.ondragover = null
        table.ondrop = null
    }

    for (let index = 2; index < numRows; ++index) {
        if (lastRow >= 0 && index >= lastRow) {
            break
        }
        const row = rows[index]
        const cell = row.children[3]
        const clone = element.cloneNode(true)
        cell.removeChild(cell.children[0])
        cell.appendChild(clone)
        if (events !== null) {
            events.forEach(ev => {
                if (ev.event === "click") {
                    clone.addEventListener(ev.event, ev.eventListener)
                } else {
                    row.addEventListener(ev.event, ev.eventListener)
                }
            })
        }
        row.draggable = enabling
    }
}

function disableElementsIn(row, disable) {
    const cells = row.childNodes
    cells.forEach(cell => {
        const elements = cell.childNodes
        elements.forEach(el => {
            if (disable) {
                el.setAttribute("disabled", true)
            } else {
                el.removeAttribute("disabled")
            }
        })
    })
}

function dragStart(event) {
    const index = event.target.parentNode.parentNode.rowIndex
    event.dataTransfer.setData("row", index)

    const rows = document.getElementById(TABLE_NAME).rows
    rows.forEach(row => disableElementsIn(row, true))
}

function dragEnd(event) {
    const rows = document.getElementById(TABLE_NAME).rows
    rows.forEach(row => {
        disableElementsIn(row, false);
        row.classList.remove("drop-above")
        row.classList.remove("drop-below")
    })
}

function handleDrop(event) {

}

function findTargetRow(event) {
    const currentY = event.clientY
    for (let row = 2; row < rowStarts.length; row++) {
        if (currentY <= rowStarts[row]) {
            return row
        }
    }
    return -1
}

function handleDragover(event) {
    event.preventDefault()
    let targetRow
    if (event.target.id !== TABLE_NAME) {
        targetRow = 0
    } else {
        targetRow = findTargetRow(event)
    }

    const rows = document.getElementById(TABLE_NAME).rows
    rows[1].classList.remove("drop-below")
    for (let row = 2; row < rows.length; row++) {
        if (row === targetRow) {
            rows[row - 1].classList.add("drop-below")
            rows[row].classList.add("drop-above")
        } else {
            rows[row].classList.remove("drop-below")
            rows[row].classList.remove("drop-above")
        }
    }
}