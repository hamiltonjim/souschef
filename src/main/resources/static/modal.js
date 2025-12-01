/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

let choice;
let chooser;
let selected = null;

/**
 * Choose a recipe title from the database, for creating a link to that recipe in
 * another recipe. If a previous recipe had been selected, clears that selection
 * first.
 * @param element the element holding the chosen recipe reference
 * @param id the (database) ID of the chosen recipe
 */
function chooseRecipe(element, id) {
    choice = id;
    if (selected !== null) {
        selected.classList.remove("selected");
    }
    selected = element;
    selected.classList.add("selected");
}

/**
 * Hides the chooser element
 */
function closeChooser() {
    chooser.style.display = "none";
}

/**
 * Shows the chooser element
 */
function showChooser() {
    chooser = document.getElementById("recipeChooser");
    chooser.style.display = "block";
}

/**
 * When a recipe is chosen, adds a link to that recipe in the one
 * currently being edited.
 * @param choice The ID of the recipe to link.
 */
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
        }).then(data => addTextBreak(data))
        .catch(error => {
        })

    closeChooser();
}

