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
    let value = element.value;
    console.log("set cookie " + name + "=" + value);
    setCookie(name, value);

    const new_url = "BASEURL/preferences/" + name + "/" + value;
    console.log("fetch " + new_url);
    fetch(new_url, {method: "POST"})
        .then(x => x.url)
        .then(() => {
            window.location.reload()
        });
}

function selectionLoaded(element) {
    let name = element.name;
    let value = getCookie(name);
    console.log("for element " + name + ": cookie is '" + value + "'");
    element.value = value;
}

// noinspection JSUnusedGlobalSymbols
function setSelects() {
    let name = "Units";
    let select = document.getElementById(name);
    selectionLoaded(select);

    name = "unitNames";
    select = document.getElementById(name);
    selectionLoaded(select);
}

function killCookies() {
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let cookie of ca) {
        cookie = cookie.trim();
        let parse = cookie.split('=');
        setCookie(parse[0], parse[1], -1);
    }
}