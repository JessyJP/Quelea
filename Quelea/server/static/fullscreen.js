// fullscreen.js

// Function to enter fullscreen mode
function openFullScreen() {
    const elem = document.documentElement;  // Select the whole webpage
    if (elem.requestFullscreen) {
        elem.requestFullscreen();
    } else if (elem.mozRequestFullScreen) {  // For Firefox
        elem.mozRequestFullScreen();
    } else if (elem.webkitRequestFullscreen) {  // For Chrome, Safari, and Opera
        elem.webkitRequestFullscreen();
    } else if (elem.msRequestFullscreen) {  // For IE/Edge
        elem.msRequestFullscreen();
    }
}

// Function to exit fullscreen mode
function closeFullScreen() {
    if (document.exitFullscreen) {
        document.exitFullscreen();
    } else if (document.mozCancelFullScreen) {  // For Firefox
        document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {  // For Chrome, Safari, and Opera
        document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {  // For IE/Edge
        document.msExitFullscreen();
    }
}

// Function to toggle between fullscreen mode and regular mode
function toggleFullScreen() {
    if (!document.fullscreenElement &&    // Standard syntax
        !document.mozFullScreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement) {
        // If not in fullscreen mode, enter fullscreen
        openFullScreen();
        document.getElementById('fullscreenIcon').textContent = "❎"; // Change icon to a 'close fullscreen' icon (for example)
    } else {
        // If in fullscreen mode, exit fullscreen
        closeFullScreen();
        document.getElementById('fullscreenIcon').textContent = "⛶"; // Change icon back to 'enter fullscreen' icon
    }
}
