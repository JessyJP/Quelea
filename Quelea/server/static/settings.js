class State {
    constructor() {
        // Initialize default values
        this.refreshInterval = 100;  // milliseconds
        this.controlsVisible = true;  // Default to visible
        this.theme = "default";  // Default theme
    }

    // Save the current state to localStorage
    saveState() {
        localStorage.setItem('refreshInterval', this.refreshInterval);
        localStorage.setItem('controlsVisible', this.controlsVisible);
        localStorage.setItem('theme', this.theme);
    }

    // Load the state from localStorage (if available)
    loadState() {
        const savedInterval = localStorage.getItem('refreshInterval');
        const savedControls = localStorage.getItem('controlsVisible');
        const savedTheme = localStorage.getItem('theme');

        // If state exists in localStorage, use it, otherwise stick with defaults
        this.refreshInterval = savedInterval ? parseInt(savedInterval) : this.refreshInterval;
        this.controlsVisible = savedControls === "false" ? false : true;  // Default to true
        this.theme = savedTheme ? savedTheme : this.theme;
    }

    // Update refresh interval
    setRefreshInterval(interval) {
        this.refreshInterval = interval;
        this.saveState();
    }

    // Toggle controls visibility
    toggleControlsVisibility() {
        this.controlsVisible = !this.controlsVisible;
        this.saveState();
        return this.controlsVisible;
    }

    // Set theme
    setTheme(theme) {
        this.theme = theme;
        this.saveState();
    }
}

// Global state object
const appState = new State();

// Load the saved state from localStorage (or use defaults)
appState.loadState();

// Function to show the settings overlay panel
function openSettings() {
    const overlay = document.getElementById("settingsOverlay");
    const refreshIntervalInput = document.getElementById("refreshIntervalInput");
    const controlsVisibilityCheckbox = document.getElementById("controlsVisibilityCheckbox");
    const themeSelect = document.getElementById("themeSelect");

    // Set current state in the form inputs
    refreshIntervalInput.value = appState.refreshInterval;
    controlsVisibilityCheckbox.checked = appState.controlsVisible;
    themeSelect.value = appState.theme;

    // Show the overlay
    overlay.style.display = "block";
}

// Function to close the settings overlay panel
function closeSettings() {
    const overlay = document.getElementById("settingsOverlay");
    overlay.style.display = "none";
}

// Function to save settings from the overlay
function saveSettings() {
    const refreshIntervalInput = document.getElementById("refreshIntervalInput");
    const controlsVisibilityCheckbox = document.getElementById("controlsVisibilityCheckbox");
    const themeSelect = document.getElementById("themeSelect");

    // Update the app state based on user input
    appState.setRefreshInterval(parseInt(refreshIntervalInput.value));
    appState.controlsVisible = controlsVisibilityCheckbox.checked;
    appState.setTheme(themeSelect.value);

    // Apply controls visibility based on updated state
    if (appState.controlsVisible) {
        showControls();
    } else {
        hideControls();
    }

    // Close the settings overlay
    closeSettings();
}

// Function to toggle the visibility of controls (called from toggleControls)
function toggleControls() {
    if (appState.toggleControlsVisibility()) {
        showControls();
    } else {
        hideControls();
    }
}

// Function to show controls
function showControls() {
    document.querySelector('.toggleButtonContainer').style.display = "block";
    document.querySelector('.navButtonContainer').style.display = "block";
    document.getElementById('center').classList.remove("center-simple-controls");
    document.getElementById('center').classList.add("center-default");
    document.getElementById('simpleControlIcon').innerHTML = "🛠️";
}

// Function to hide controls
function hideControls() {
    document.querySelector('.toggleButtonContainer').style.display = "none";
    document.querySelector('.navButtonContainer').style.display = "none";
    document.getElementById('center').classList.remove("center-default");
    document.getElementById('center').classList.add("center-simple-controls");
    document.getElementById('simpleControlIcon').innerHTML = "❌";
}

// Load state when the page loads
window.onload = function() {
    if (appState.controlsVisible) {
        showControls();
    } else {
        hideControls();
    }
};
