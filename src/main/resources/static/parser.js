/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
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

function checkLoadFromScreenEnabled(textArea) {
    const text = textArea.value.trim()
    const button = document.getElementById("load-from-screen")
    button.disabled = text.length === 0
}

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

function handleString(event) {
    let text = event.dataTransfer.getData("text/plain")
    console.log(text)
    event.target.value += text
}

function dragOver(event) {
    event.preventDefault()
    event.dataTransfer.dropEffect = "copy"
}

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