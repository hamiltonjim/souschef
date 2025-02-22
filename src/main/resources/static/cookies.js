// Copyright © 2025 Jim Hamilton. All rights reserved.

// noinspection IncorrectFormatting

function setCookie(cname, cookieValue, expireDays = null) {
    const date = new Date();
    if (cookieValue === null || cookieValue === undefined) {
        cookieValue = "";
    }
    if (expireDays === null || expireDays === undefined) {
        document.cookie = cname + "=" + cookieValue + " ;path=/";
    } else {
        date.setTime(date.getTime() + (expireDays * 24 * 60 * 60 * 1000));
        let expires = "expires=" + date.toUTCString();
        document.cookie = cname + "=" + cookieValue + ";" + expires + ";path=/";
    }
}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let cookie of ca) {
        cookie = cookie.trim();
        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length, cookie.length);
        }
    }
    return "";
}

function selectionChanged(element) {
    let name = element.name;
    const type = element.type;
    let value;

    if (type === "checkbox" || type === "radio") {
        value = element.checked;
    } else {
        value = element.value;
    }

    console.log("set cookie " + name + "=" + value);
    setCookie(name, value);

    const new_url = "/souschef/preferences/" + name + "/" + value;
    console.log("fetch " + new_url);
    fetch(new_url, {method: "POST"})
        .then(x => x.url)
        .then(() => window.location.reload())
}

function setValue(element, value) {
    const type = element.type;
    const change = element.onchange;
    element.onchange = null;
    if (type === "checkbox" || type === "radio") {
        element.checked = value === "true";
    } else {
        element.value = value;
    }
    element.onchange = change;
}

// noinspection JSUnusedGlobalSymbols
function setSelects() {
    const pos = document.URL.search("/souschef");
    const URL = document.URL
    const base = URL.substring(0, pos + 9);

    const getPreferencesUrl = base + "/preferences";
    fetch(getPreferencesUrl)
        .then(result => result.json())
        .then((preferences) =>{
            for (const key in preferences) {
                const element = document.getElementById(key);
                if (element !== null) {
                    const value = preferences[key];
                    setValue(element, value);
                }
            }
        });
}
