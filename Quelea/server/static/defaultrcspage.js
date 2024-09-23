var xmlhttp = new XMLHttpRequest();
var schhttp = new XMLHttpRequest();
var biblehttp = new XMLHttpRequest();
var statushttp = new XMLHttpRequest();
var lyriccache;
var statuscache;
var schedulecache;

var refreshTimeout = 100;

function setup()
{
    getTranslations();
    setInterval(function () {
        loadXMLDoc();
        stateCheck();
        scheduleCheck();
    }, refreshTimeout);

    xmlhttp.onreadystatechange = function ()
    {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200)
        {
            if (lyriccache !== xmlhttp.responseText) {
                document.getElementById("lyrics").innerHTML = xmlhttp.responseText;
                lyriccache = xmlhttp.responseText;
                getSectionTitles();
            }

        }
    }
    statushttp.onreadystatechange = function ()
    {
        if (statushttp.readyState == 4 && statushttp.status == 200)
        {
            if (statuscache !== statushttp.responseText) {
                statuscache = statushttp.responseText;
                var logo = document.getElementById("logoButton");
                var black = document.getElementById("blackButton");
                var clear = document.getElementById("clearButton");

                var st = statuscache.split(",");
                if (st[0] === "true") {
                    logo.className = logo.className.replace(/\unselected\b/g, "active");
                }
                else {
                    logo.className = logo.className.replace(/\active\b/g, "unselected");
                }
                if (st[1] === "true") {
                    black.className = black.className.replace(/\unselected\b/g, "active");
                }
                else {
                    black.className = black.className.replace(/\active\b/g, "unselected");
                }
                if (st[2] === "true") {
                    clear.className = clear.className.replace(/\unselected\b/g, "active");
                }
                else {
                    clear.className = clear.className.replace(/\active\b/g, "unselected");
                }
                if (document.getElementById("playbutton") !== null) {
                    document.getElementById("playbutton").innerHTML = st[3];
                }
            }
        }
    }
}
window.onload = setup;
function loadXMLDoc()
{
    xmlhttp.open("GET", "/lyrics", true);
    xmlhttp.timeout = 4000;
    xmlhttp.ontimeout = function () {
        document.getElementById("lyrics").innerHTML = "";
    }
    xmlhttp.send();
}
function stateCheck()
{
    statushttp.open("GET", "/status", true);
    statushttp.timeout = 4000;
    statushttp.ontimeout = function () {
        var logo = document.getElementById("logoButton");
        logo.className = logo.className.replace(/\active\b/g, "unselected");
        var black = document.getElementById("blackButton");
        black.className = black.className.replace(/\active\b/g, "unselected");
        var clear = document.getElementById("clearButton");
        clear.className = clear.className.replace(/\active\b/g, "unselected");
    }
    statushttp.send();
}
function scheduleCheck()
{
    schhttp.open("GET", "/schedule", true);
    schhttp.timeout = 4000;
    schhttp.ontimeout = function () {
        document.getElementById("schedule").innerHTML = "";
    }
    schhttp.onreadystatechange = function ()
    {
        if (schhttp.readyState == 4 && schhttp.status == 200)
        {
            if (schedulecache !== schhttp.responseText) {
                document.getElementById("schedule").innerHTML = "";
                var lines = schhttp.responseText.split("<br/>");
                for (var i = 0; i < lines.length; i++) {
                    var regex = /(<([^>]+)>)/ig;
                    if (lines[i].trim().replace(regex, "") !== "") {
                        var style = "";
                        if (lines[i].indexOf('i>') !== -1) {
                            style = ' preview';
                            }
                        if (lines[i].indexOf('b>') !== -1) {
                            style = ' current';
                            console.log(lines[i]);
                            }
                        document.getElementById("schedule").innerHTML += "<p class='scheduleItem" + style + "'><span onclick='gotoItem(" + i + ")'>" + lines[i] + "</span></p><hr>";
                    }
                }
                schedulecache = schhttp.responseText;
            }

        }
    }
    schhttp.send();
}
function nextSlide() {
    var request = new XMLHttpRequest();
    request.open('GET', '/next', true);
    request.send();
    request = null;
}
function previousSlide() {
    var request = new XMLHttpRequest();
    request.open('GET', '/prev', true);
    request.send();
    request = null;
}
function nextItem() {
    var request = new XMLHttpRequest();
    request.open('GET', '/nextitem', true);
    request.send();
    request = null;
}
function previousItem() {
    var request = new XMLHttpRequest();
    request.open('GET', '/previtem', true);
    request.send();
    request = null;
}
function logo() {
    var request = new XMLHttpRequest();
    request.open('GET', '/tlogo', true);
    request.send();
    request = null;
}
function black() {
    var request = new XMLHttpRequest();
    request.open('GET', '/black', true);
    request.send();
    request = null;
}
function clears() {
    var request = new XMLHttpRequest();
    request.open('GET', '/clear', true);
    request.send();
    request = null;
}
function play() {
    var request = new XMLHttpRequest();
    request.open('GET', '/play', true);
    request.send();
    request = null;
}
function section(i) {
    var request = new XMLHttpRequest();
    request.open('GET', '/section' + i, true);
    request.send();
    request = null;
}
function gotoItem(i) {
    closeDrawer();
    var request = new XMLHttpRequest();
    request.open('GET', '/gotoitem' + i, true);
    request.send();
    request = null;
}
function getSectionTitles() {
    var sections = document.getElementsByTagName("p");
    var title;
    var i;
    for (i = 0; i < sections.length; i++) {
        title = sections[i].getAttribute("data-type");
        if (title != null) {
            sections[i].innerHTML = "<div class='title'>" + title + "</div>" + sections[i].innerHTML;
        }
    }
}
function toggleDrawer() {
    if (document.getElementById("leftDrawer").style.width !== "18em") {
        openDrawer();
    } else {
        closeDrawer();
    }
}
function openDrawer() {
    document.getElementById("leftDrawer").style.width = "18em"; // The width the drawer extends to
    document.getElementById("drawerButtonContainer").style.transform = "translate(18em, 0px)";
    document.getElementById("drawerButtonContainer").style.WebkitTransform = "translate(18em, 0px)";
    document.getElementById("drawerButtonContainer").style.msTransform = "translate(18em, 0px)";
    hideButtons(); // Hide buttons when the drawer is open
}
function closeDrawer() {
    document.getElementById("leftDrawer").style.width = "0px";
    document.getElementById("drawerButtonContainer").style.transform = "translate(0px, 0px)";
    document.getElementById("drawerButtonContainer").style.WebkitTransform = "translate(0px, 0px)";
    document.getElementById("drawerButtonContainer").style.msTransform = "translate(0px, 0px)";
    showButtons(); // Show buttons when the drawer is closed
}
function toggleSearch(close) {
    var searchBox = document.getElementById("searchBox");
    var searchIcon = document.getElementById("searchIcon");
    var navBarIcon = document.getElementById("drawerIcon");
    var actionButtonContainer = document.getElementById("actionButtonContainer");
    var suggestions = document.getElementById("suggestions");
    var width = "0px";
    if (searchBox.style.width !== "16em" && !close) {
        width = "16em";
        searchBox.style.display = "inline";
        searchIcon.innerHTML = "✕";
        resultList.style.visibility = "visible";
    } else {
        searchBox.style.display = "none";
        searchIcon.innerHTML = "🔍";
        resultList.style.visibility = "hidden";
        searchBox.value = "";
        loadSearch("");
        suggestions.style.visibility = "hidden";
    }
    searchBox.style.width = width;
    actionButtonContainer.style.transform = "translate(-" + width/2 +", 0px)";
    actionButtonContainer.style.WebkitTransform = "translate(-" + width/2 + ", 0px)";
    actionButtonContainer.style.msTransform = "translate(-" + width/2 + ", 0px)";
}
function search() {
    var searchBox = document.getElementById("searchBox");
    loadSearch("/search/" + searchBox.value);
    getBooks(searchBox.value);
}

function getTranslations()
{
    var xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
            var lines = xmlhttp.responseText.split("\n");

            for (var i = 0; i < lines.length; i++) {
                if (lines[i].length > 0)
                    document.getElementById("translations").innerHTML += "<option value=\""+ lines[i] +"\">"+ lines[i] + "</option>";
            }
            return xmlhttp.responseText;
        }
    }
    xmlhttp.open("GET", "/translations", false);
    xmlhttp.send();
}
function getBooks(query)
{
    var xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
            var lines = xmlhttp.responseText.split("\n");
            var suggestions = document.getElementById("suggestions");
            suggestions.innerHTML = "";
            suggestions.style.visibility = "hidden";
            for (var i = 0; i < lines.length; i++) {
                if (lines[i].length > 0) {
                    if (lines[i].toLowerCase().indexOf(query.toLowerCase()) >= 0) {
                        suggestions.style.visibility = "visible";
                        suggestions.innerHTML += "<p onclick=\"setSearch('"+ lines[i] +"')\">"+ lines[i] + "</p>";
                    } else if (query.indexOf(" ") > 0 && query.match(/([^\x00-\x7F]|\w)+\s\d(:\d+)?(-\d+)?/)) {
                        suggestions.style.visibility = "visible";
                        suggestions.innerHTML = "<p onclick=\"getMessageFromServer('/addbible/" + document.getElementById("translations").value + "/"+ query.split(/[\s\d]/)[0] + "/" + query.match(/([^\x00-\x7F]|\w)+\s(\d(:\d+)?(-\d+)?)/m)[2] + "');\">[click.to.add]: " + query + "</p>";
                    }
                }
            }
            return xmlhttp.responseText;
        }
    }
    xmlhttp.open("GET", "/books/" + document.getElementById("translations").value, false);
    xmlhttp.send();
}
function loadSearch(url){
    var songResults=document.getElementById("songResults");
    var clone=songResults.cloneNode(true);
    clone.setAttribute('src',url);
    songResults.parentNode.replaceChild(clone,songResults)
}
function setSearch(query) {
    var searchBox = document.getElementById("searchBox");
    searchBox.value = query;
    document.getElementById("suggestions").style.visibility = "hidden";
    setCaretPosition(query);
}
function getMessageFromServer(url)
{
    document.getElementById("suggestions").style.visibility = "hidden";
    var xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if (xmlhttp.readyState==4 && xmlhttp.status==200)
        {
            alert(xmlhttp.responseText);
        }
    }
    xmlhttp.open("GET", url, false);
    xmlhttp.send();
    return true;
}
function setCaretPosition(query) {
    var elem = document.getElementById("searchBox");
    var caretPos = query.length;
    elem.focus();
    elem.setSelectionRange(caretPos, caretPos);
}


// --------------------------------------------------------------------------
function hideButtons() {
    var toggleButtons = document.getElementsByClassName("toggleButtonContainer")[0];
    var navButtons = document.getElementsByClassName("navButtonContainer")[0];

    if (toggleButtons) {
        toggleButtons.style.display = "none"; // Hide the toggle buttons
    }
    if (navButtons) {
        navButtons.style.display = "none"; // Hide the navigation buttons
    }
}

function showButtons() {
    var toggleButtons = document.getElementsByClassName("toggleButtonContainer")[0];
    var navButtons = document.getElementsByClassName("navButtonContainer")[0];

    if (toggleButtons) {
        toggleButtons.style.display = "block"; // Show the toggle buttons
    }
    if (navButtons) {
        navButtons.style.display = "block"; // Show the navigation buttons
    }
}
