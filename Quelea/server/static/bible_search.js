function toggleBibleSearchOverlay() {
    var overlay = document.getElementById("bibleSearchOverlay");
    if (overlay.style.display === "block") {
        overlay.style.display = "none"; // Hide the overlay
    } else {
        overlay.style.display = "block"; // Show the overlay
    }
}


function performSearch() {
    // Dummy search function (replace with actual logic)
    const searchText = document.getElementById("searchField").value;
    const resultsContainer = document.getElementById("searchResults");
    const matchesLabel = document.getElementById("matchesLabel");

    // Clear previous results
    resultsContainer.innerHTML = "";

    if (searchText.length > 2) {
        // Example: Fake results (replace with actual search logic)
        let results = [];
        for (let i = 1; i <= 5; i++) {
            results.push(`<p>Result for "${searchText}" - Match ${i}</p>`);
        }

        // Update the results and matches count
        resultsContainer.innerHTML = results.join('');
        matchesLabel.textContent = `${results.length} matches`;
    } else {
        matchesLabel.textContent = "0 matches";
    }
}

function addChapter() {
    alert("Add Chapter button clicked!");
}

function addVerses() {
    alert("Add Verse button clicked!");
}
