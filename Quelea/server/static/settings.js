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

// Function to show the settings overlay window
function showSettingsOverlayWindow() {
    const overlay = document.getElementById('settingsOverlay');
    overlay.style.display = 'block'; // Show the settings overlay
}

// Function to close settings when clicking outside the content area
function closeSettings(event) {
    const overlay = document.getElementById('settingsOverlay');
    // Check if the click is outside the settings content
    if (event.target === overlay) {
        overlay.style.display = 'none'; // Hide the settings overlay
    }
}

// Function to close settings via the close button
function closeSettingsFromButton() {
    document.getElementById('settingsOverlay').style.display = 'none';
}

// Attach the close function to the overlay
document.getElementById('settingsOverlay').addEventListener('click', closeSettings);

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
    appState.toggleControlsVisibility();
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

// ===========================================================================
// Initialization
// ===========================================================================

// Initialize settings input values from the current state
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById("refreshIntervalInput").value = appState.refreshInterval;
    document.getElementById("controlsVisibilityCheckbox").checked = appState.controlsVisible;
    document.getElementById("themeSelect").value = appState.theme;

    // Set event listeners for input changes
    document.getElementById("refreshIntervalInput").addEventListener('input', (event) => {
        appState.setRefreshInterval(parseInt(event.target.value) || 100);
    });

    document.getElementById("themeSelect").addEventListener('change', (event) => {
        appState.setTheme(event.target.value);
        document.body.className = event.target.value; // Apply the theme class to the body
    });
});
