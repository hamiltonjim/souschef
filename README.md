# SousChef
## A Cookbook webapp
Copyright © 2025 Jim Hamilton.
[KDoc](https://hamiltonjim.github.io/souschef/index.html)

SousChef not only keeps recipes, but can adjust the ingredients for
the number of servings you want. On a recipe page, simply place a desired number in the
"Servings" field and click the "Set" button. To return to the original
number of servings, click the "Reset" button.

# Installation
## Requirements
You need access to a PostgreSQL database. With that, set the environment variable
SC_DATABASE to the URL for your database, such as 
`jdbc:postgresql://localhost:5432/postgres` (substitute your database's host name
and port for "localhost" and "5432").

Once you have your database, run the script in `db/init.sql` to build the database
tables needed by SousChef.

## Build and Run
Build the jar with Maven, and rename it `souschef.jar`; then the script file
`bash/souschef-launch` will be able to launch it (as long as that environment
variable is set correctly).

## The Preferences Panel
### Menus
There are three drop-down menus in the Preferences panel at the bottom
of the web-app:
1. **Units**: Choose from English (cups, quarts, pounds), International (grams, milliliters) or Any
2. **Unit Names**: Choose from either Full Name or Abbreviation
3. **Language**: Choose the language in which you are most comfortable.

### Categories
There is a button labelled "Categories" which will bring up a page with
a list of categories, and a field to enter a new category name. Click on 
any category link to bring up a list of recipes in that category.

### Show Deleted Recipes
If selected, will show recipes that have been (marked) Deleted, allowing them to 
be restored.

### Load a Recipe
This link will bring up the Recipe Reader page.  More on this below.

## Entering Recipes
There are three ways to enter a recipe:
1. Type it into the Edit Recipe page
2. Paste it into the Load Recipe page
3. Read a (text or PDF) file from the Load Recipe page

### Type Into the Edit Recipe Page
From the Categories page, click a category name. The existing recipes in that
category will appear, along with a button labelled "New Recipe". There will be
a form, including a field for the recipe title, the number of servings, a table
of ingredients, and a large text box for the recipe directions. That box accepts
HTML directives, and there are buttons to help with that.

Additionally, there is a rendering of the directions as they will
appear on the screen or in print.

Click on the "Add Ingredient" button for each ingredient in the Recipe. Each ingredient
consists of a number, an optional unit, and the food item. To set the unit, choose from
the drop-down menu; to clear the unit, click the small "x" next to the drop-down menu.

Every recipe must have a title, a number of servings, and at least one ingredient.

### Paste Into the Recipe Reader Page
When you paste *anything* into the textbox on the left side of the screen, the button
labelled "Read Recipe" will enable. When you click that button, the reader will make
its best estimates of what the recipe looks like.

- The first line is assumed to be the title of the recipe.
- If there is a line with "Serves" or "Servings" on it, followed by a number, that
is assumed to be the number of servings in the recipe. If there is no such line, you
are responsible for filling in that field.
- Next is a list of ingredients, in any number of columns. (You may need to edit by hand
if some ingredients take multiple *lines*.) An ingredient is an optional number, an
optional unit, and a food item. 
- Finally, the rest of the text is the recipe directions. Words like "stir," "mix,"
and "pan" are assumed to be a direction, and **might** cause a premature end of 
ingredients.

### Load a File Containing a Recipe
You may click the "Choose File" button to load a recipe from a text or PDF file. The
reader makes the same types of assumptions as from the text box. If this is a problem, 
just open the file, copy its contents, and paste into the text box, then edit; or,
just drag the file into the text box, and edit.

## Viewing, Editing, and Printing a Recipe
From the Categories screen, choose a category. From the resulting list, choose the
"View" link to read a recipe. From the recipe screen, you may click "Edit" or "Print".
The Print screen will open in a new tab; it will look like the view screen, but with
less clutter.

The Edit screen has been mentioned above; it contains a table of ingredients, which
may be lengthened or shortened, and a text box for directions.

---
Distributed under the MIT License.