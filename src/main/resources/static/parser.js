/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

/**
 * Reads a file from the chooser element, and POSTs it to the host application.
 * @param chooser The file chooser element
 */
function loadRecipeFile(chooser) {
    const file = chooser.files[0];
    if (file) {
        const reader = new FileReader();

        reader.onload = (e) => {
            let fileContent = e.target.result;
            if (file.type !== "text/plain") {
                const array = new Uint8Array(fileContent);
                let string = "";
                for (let index = 0; index < array.length; ++index) {
                    string += String.fromCharCode(array[index]);
                }
                fileContent = window.btoa(string);
            }
            fetch("/souschef/parser/recipeFromFile?type=" + file.type, {
                method: "POST",
                body: fileContent
            })
                .then(response => response.text())
                .then(html => document.body.innerHTML = html)
                .catch(error => {
                    const response = error.response
                    const status = response.status
                    console.log(status)
                });
        };

        reader.onerror = (e) => {
            console.error("Error reading file:", e);
        };

        if (file.type === "text/plain") {
            reader.readAsText(file);
        } else {
            reader.readAsArrayBuffer(file);
        }
    }
}

/**
 * Checks whether there is any text in the given text area, and
 * enables the "load-from-screen" button if it's not empty.
 * @param textArea given text area
 */
function checkLoadFromScreenEnabled(textArea) {
    const text = textArea.value.trim()
    const button = document.getElementById("load-from-screen")
    button.disabled = text.length === 0
}

/**
 * Loads a file and POSTs it to the host application.
 * @param file file handle
 * @param type content-type; understands "text/plain" or "application/pdf" only
 * @param target a text area element, to which the file contents will be loaded.
 */
function handleFile(file, type, target) {
    if (type !== "text/plain" && type !== "application/pdf") {
        return
    }
    console.log(file.name)

    const reader = new FileReader()
    reader.onload = (e) => {
        let fileContent = e.target.result;
        if (file.type === "text/plain") {
            target.value += fileContent
            checkLoadFromScreenEnabled(target)
            return
        }

        const array = new Uint8Array(fileContent);
        let string = "";
        for (let index = 0; index < array.length; ++index) {
            string += String.fromCharCode(array[index]);
        }
        fileContent = window.btoa(string);
        fetch("/souschef/parser/textFromPdf", {
            method: "POST",
            body: fileContent
        })
            .then(response => response.text())
            .then(text => {
                target.value += text
                checkLoadFromScreenEnabled(target)
            })
            .catch(error => {
                const response = error.response
                const status = response.status
                console.log(status)
            });
    };

    reader.onerror = (e) => {
        console.error("Error reading file:", e);
    };

    if (file.type === "text/plain") {
        reader.readAsText(file);
    } else {
        reader.readAsArrayBuffer(file);
    }
}

/**
 * Handles a drop event; if the event contains text, append it to the event's target element.
 * @param event drop event
 */
function handleString(event) {
    let text = event.dataTransfer.getData("text/plain")
    console.log(text)
    event.target.value += text
}

/**
 * Handles a "drag over" event.
 * @param event the event
 */
function dragOver(event) {
    event.preventDefault()
    event.dataTransfer.dropEffect = "copy"
}

/**
 * Handles a drop event; if the event contains one or more files, attempts to read the files into
 * the event's target (text area).
 * @param event the event
 */
function takeDrop(event) {
    event.preventDefault()
    let files = event.dataTransfer.files
    if (files.length > 0) {
        ([...files]).forEach(item => {
            handleFile(item, item.type, event.target)
        })
    } else {
        handleString(event)
    }
}

/**
 * Handles the "load-from-screen" button click; attempts to read the text in the recipe area,
 * and create a new recipe screen (which may be further edited).
 */
function loadRecipeFromScreen() {
    const textArea = document.getElementById("to-parse")
    const text = textArea.value
    fetch("/souschef/parser/recipeFromScreen", {
        method: "POST",
        body: text
    })
        .then(response => response.text())
        .then(html => document.body.innerHTML = html)
}