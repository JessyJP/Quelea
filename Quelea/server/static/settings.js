// ===========================================================================
// Class Definition: Manages application state and handles saving, loading, and resetting settings
// ===========================================================================
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

    // Reset to default values
    reset() {
        this.refreshInterval = 100;
        this.controlsVisible = true;
        this.theme = "default";
        this.saveState();
    }
}

// ===========================================================================
// Global State Variables and Configurations
// ===========================================================================
const appState = new State();
appState.loadState();

// ===========================================================================
// Methods to Handle the Settings and the Window
// ===========================================================================

// Function to load settings into the settings panel
function showSettingsOverlayWindow() {
    const settingsFrame = document.getElementById("settingsFrame");
    settingsFrame.src = "static/settings.html";
    settingsFrame.style.display = "block"; // Show the iframe
}

// Function to close settings when clicking outside the content area
function closeSettings(event) {
    const overlay = document.querySelector('.settingsOverlay');
    const content = document.querySelector('.settingsContent');
    // Check if the click is outside the settings content
    if (event.target === overlay && !content.contains(event.target)) {
        overlay.style.display = 'none'; // Hide the settings overlay
    }
}

// Attach the close function to click on the overlay itself
document.querySelector('.settingsOverlay').addEventListener('click', closeSettings);

// Function to close settings via the close button
function closeSettingsFromButton() {
    document.querySelector('.settingsOverlay').style.display = 'none';
}

// Function to reset settings
function resetSettings() {
    appState.reset();
    // Update the UI with default values
    document.getElementById("refreshIntervalInput").value = appState.refreshInterval;
    document.getElementById("controlsVisibilityCheckbox").checked = appState.controlsVisible;
    document.getElementById("themeSelect").value = appState.theme;
}

// ===========================================================================
// Methods to Handle Specific Settings
// ===========================================================================

// Function to toggle controls visibility from settings
function toggleControlsFromSettings() {
    const controlsVisibilityCheckbox = document.getElementById("controlsVisibilityCheckbox");
    if (controlsVisibilityCheckbox.checked) {
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
}

// Function to hide controls
function hideControls() {
    document.querySelector('.toggleButtonContainer').style.display = "none";
    document.querySelector('.navButtonContainer').style.display = "none";
    document.getElementById('center').classList.remove("center-default");
    document.getElementById('center').classList.add("center-simple-controls");
}
