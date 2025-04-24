/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

let choice;
let chooser;
let selected = null;

function chooseRecipe(element, id) {
    choice = id;
    if (selected !== null) {
        selected.classList.remove("selected");
    }
    selected = element;
    selected.classList.add("selected");
}

function closeChooser() {
    chooser.style.display = "none";
}

function showChooser() {
    chooser = document.getElementById("recipeChooser");
    chooser.style.display = "block";
}

function addLink(choice) {
    if (choice === null || choice === undefined) {
        return;
    }

    fetch("/souschef/getRecipeLink/" + choice)
        .then(response => {
            if (!response.ok) {
                throw new ResponseError("problems", response);
            }
            return response.text();
        }).then(data => addTextBreak(data) )
        .catch(error => {})

    closeChooser();
}

