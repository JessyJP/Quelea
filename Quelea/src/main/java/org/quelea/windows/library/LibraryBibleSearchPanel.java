/*
 * This file is part of Quelea, free projection software for churches.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.library;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.quelea.data.bible.Bible;
import org.quelea.data.bible.BibleChangeListener;
import org.quelea.data.bible.BibleManager;
import org.quelea.data.bible.BibleVerse;
import org.quelea.data.bible.BibleChapter;
import org.quelea.data.displayable.BiblePassage;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.schedule.SchedulePanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The panel used to search and manage Bible passages.
 * <p>
 * This class initializes the UI components used for selecting Bibles, searching passages,
 * and interacting with chapters and verses.
 */
public class LibraryBibleSearchPanel extends VBox implements BibleChangeListener {

    // The engine used to render HTML content in the WebView
    private WebEngine webEngine;

    // Dropdown to select the Bible version
    private final ComboBox<Bible> bibleSelector;

    // Labels for search UI
    private final Label matchesLabel;
    private final Label searchLabel;

    // Text field for free text search of Bible content (e.g., "love", "forgiveness")
    private final TextField searchField;

    // Buttons for adding chapters, verses, and navigating to the Bible panel
    private final Button addChapter;
    private final Button addVerses;
    private final Button goToBibleButton;

    // Checkbox to toggle the "Go Live" feature, which may automatically display added items
    private final CheckBox goLiveCheckBox;

    // WebView to preview Bible passages, chapters, or search results
    private final WebView preview;

    // List to hold Bible verses currently being managed or displayed
    private final List<BibleVerse> verses;

    // To keep track of the selected verse
    private BibleVerse selectedVerse;

    //-------------------------
    private final int minSearchChar = 2;

    /**
     * Create and populate a new Library Bible Search Panel.
     */
    public LibraryBibleSearchPanel() {
        // Initialize the list of verses
        verses = new ArrayList<>();
        selectedVerse = null;

        // Initialize the search field before it is used
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setDisable(true); // Disable until BibleManager is initialized

        // Add a listener to trigger search dynamically as the user types
        searchField.textProperty().addListener((observable, oldValue, newValue) -> performSearch(newValue));

        // Initialize the Bible selector dropdown (ComboBox) and set its initial state
        bibleSelector = new ComboBox<>();
        bibleSelector.setDisable(true); // Disable until Bibles are loaded

        // Add a listener to re-trigger search when the selected Bible changes
        bibleSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            performSearch(searchField.getText());
        });

        // Initialize the WebView and its engine for displaying passage previews
        preview = new WebView();
        webEngine = preview.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Initialize the labels for the UI
        searchLabel = new Label("Search:");
        matchesLabel = new Label("0 matches");

        // Initialize the buttons for adding chapters and verses
        addChapter = new Button("Add Chapter");
        addVerses = new Button("Add Verse");

        // Set the action for adding chapters
        addChapter.setOnAction(event -> addChapter());

        // Set the action for adding verses
        addVerses.setOnAction(event -> addVerses());

        // Initialize the "Go Live" checkbox
        goLiveCheckBox = new CheckBox("Go Live");

        // Initialize the "Go to Bible" button
        goToBibleButton = new Button("Go to Bible");
        goToBibleButton.setDisable(true); // Disable until a verse is selected
        goToBibleButton.setOnAction(event -> goToBible());

        // Set up the basic layout and spacing of the panel
        this.setSpacing(10);

        // Create separate rows for each section
        HBox bibleRow = new HBox(10, bibleSelector);
        HBox buttonRow = new HBox(10, addChapter, addVerses, goLiveCheckBox, goToBibleButton);
        HBox searchRow = new HBox(10, searchLabel, searchField, matchesLabel);

        // Add the rows and preview area to the main layout
        getChildren().addAll(bibleRow, buttonRow, searchRow, preview);

        // Set up key combinations
        setKeyCombinations();

        // Register this panel as a listener to Bible changes
        BibleManager.get().registerBibleChangeListener(this);

        // Check if the Bible index is initialized; if not, load it
        if (!BibleManager.get().isIndexInit()) {
            BibleManager.get().refreshAndLoad();
        }

        // Enable search and Bible selector once the Bible index is ready
        BibleManager.get().runOnIndexInit(() -> {
            searchField.setDisable(false);
            updateBibles(); // Populate the ComboBox with available Bibles
        });

        resetPreviewToCleared();
    }

    /**
     * Sets up key combinations for buttons, checkbox, and navigation.
     */
    private void setKeyCombinations() {
        // Set KeyCodeCombination for "Add Verses" (Alt + V)
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN).match(event)) {
                addVerses.fire();
            }
        });

        // Set KeyCodeCombination for "Add Chapter" (Alt + C)
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN).match(event)) {
                addChapter.fire();
            }
        });

        // Set KeyCodeCombination for "Go Live" (Alt + G)
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN).match(event)) {
                goLiveCheckBox.setSelected(!goLiveCheckBox.isSelected());
            }
        });

        // Set KeyCodeCombination for "Focus Search Field" (Ctrl + F)
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                searchField.requestFocus();
            }
        });

        // Set KeyCodeCombination for "Go to Bible" (Alt + B)
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN).match(event)) {
                goToBibleButton.fire();
            }
        });

        // Add navigation for ALT + Up and ALT + Down to move through the verse list
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN).match(event)) {
                changeSelection("up");  // Navigate up
            }
            else if (new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN).match(event)) {
                changeSelection("down");  // Navigate down
            }
        });
    }

    /**
     * Reset the preview area when the search is cleared or in its initial state.
     * Also clears any selection and disables relevant buttons.
     */
    private void resetPreviewToCleared() {
        setMatchesCount(0);
        verses.clear();
        selectedVerse = null;
        clearPreview();
        updateButtonState(); // Disable buttons when no verse is selected
    }

    /**
     * Clear the preview panel and ensure the background matches the current theme.
     */
    private void clearPreview() {
        // Check the current theme setting
        boolean isDarkTheme = QueleaProperties.get().getUseDarkTheme();

        // Set the HTML content based on the theme
        String backgroundColor = isDarkTheme ? "#2B2B2B" : "#FFFFFF"; // Dark theme: dark background, Light theme: white background
        String textColor = isDarkTheme ? "#FFFFFF" : "#000000"; // Adjust text color for visibility based on theme

        // Generate the basic HTML structure with appropriate background and text color
        String htmlContent = String.format(
                "<html><body style='background-color:%s; color:%s; margin:0; padding:0;'>" +
                        "<p style='font-family: Sans-Serif;'>No results to display. Start your search.</p>" +
                        "</body></html>", backgroundColor, textColor);

        // Load the HTML content into the WebView
        webEngine.loadContent(htmlContent);
    }

    /**
     * Set the count of matches found during the search.
     *
     * @param count the number of matches found.
     */
    private void setMatchesCount(int count) {
        matchesLabel.setText(count + " matches");
    }

    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Update the bibles in the panel based on the current bibles the BibleManager is aware of.
     */
    @Override
    public void updateBibles() {
        // Update the Bible list in the ComboBox
        Platform.runLater(() -> {
            ObservableList<Bible> bibles = FXCollections.observableArrayList(BibleManager.get().getBibles());
            bibleSelector.setItems(bibles);
            String selectedBibleName = QueleaProperties.get().getDefaultBible();
            for (int i = 0; i < bibles.size(); i++) {
                if (bibles.get(i).getName().equals(selectedBibleName)) {
                    bibleSelector.getSelectionModel().select(i);
                    break;
                }
            }
            bibleSelector.setDisable(false); // Enable the selector after updating
        });
    }

    /**
     * Perform a search operation on the input text.
     *
     * @param searchText the text to search for.
     */
    private void performSearch(String searchText) {
        // Trim leading and trailing white space
        searchText = searchText.trim();

        // Tokenize the searchText by splitting on whitespace
        String[] tokens = searchText.split("\\s+");

        // Find the longest token
        String longestToken = Arrays.stream(tokens)
                .max((a, b) -> Integer.compare(a.length(), b.length()))
                .orElse("");

        // Check if the longest token is longer than 3 characters
        if (longestToken.length() > minSearchChar) {
            Platform.runLater(() -> {
                // Clear previous results
                verses.clear();

                // Use BibleManager's filter method to search Bible chapters based on the longest token
                final BibleChapter[] initialResults = BibleManager.get().getIndex().filter(longestToken, null);

                // Reset and populate the search results
                if (!longestToken.isEmpty()) {
                    // Iterate over filtered chapters
                    for (BibleChapter chapter : initialResults) {
                        // Check if either all Bibles are selected or the current Bible matches the selection
                        if (bibleSelector.getSelectionModel().getSelectedIndex() == 0 ||
                                chapter.getBook().getBible().getName().equals(bibleSelector.getSelectionModel().getSelectedItem().getName())) {
                            // Iterate over the verses in the chapter
                            for (BibleVerse verse : chapter.getVerses()) {
                                // Check if all tokens (from the original search) are present in the verse text
                                boolean allTokensMatch = Arrays.stream(tokens)
                                        .allMatch(token -> verse.getVerseText().toLowerCase().contains(token.toLowerCase()));
                                // If all tokens match, add the verse to the results
                                if (allTokensMatch) {
                                    verses.add(verse);  // Add matching verse
                                }
                            }
                        }
                    }
                }

                // Auto-select the first verse from the search results if available
                if (!verses.isEmpty()) {
                    selectedVerse = verses.get(0);
                }

                // Update the matches label with the number of results found
                setMatchesCount(verses.size());

                // Render search results and update the button state based on selection
                renderSearchResults(verses);
                updateButtonState(); // Update button state after selection
            });
        } else {
            // Reset the matches count and clear any displayed verses when the search is too short
            resetPreviewToCleared();
        }
    }

    /**
     * Change the selection in the filtered verse list.
     * @param direction "up" or "down" to move the selection accordingly.
     */
    private void changeSelection(String direction) {
        if (verses.isEmpty()) {
            return;
        }

        int currentIndex = verses.indexOf(selectedVerse);

        if ("up".equals(direction) && currentIndex > 0) {
            selectedVerse = verses.get(currentIndex - 1);
        } else if ("down".equals(direction) && currentIndex < verses.size() - 1) {
            selectedVerse = verses.get(currentIndex + 1);
        }

        // Update the view with the new selection and ensure visibility
        updateSelectedVerseInView();
    }

    /**
     * Update the selected verse in the WebView preview and ensure it is visible.
     */
    private void updateSelectedVerseInView() {
        if (selectedVerse != null) {
            // Re-render search results to reflect the new selection in the HTML preview
            renderSearchResults(verses);

            // Scroll the selected verse into view (if not already visible)
            Platform.runLater(() -> {
                String verseId = generateVerseId(selectedVerse);
                webEngine.executeScript("scrollTo('" + verseId + "');");
            });
        }
    }

    /**
     * Helper method to generate the unique ID for the verse based on the book name, chapter, and verse number.
     */
    private String generateVerseId(BibleVerse verse) {
        String bookName = (verse.getChapter().getBook().getName() != null)
                ? verse.getChapter().getBook().getName().replaceAll("\\s+", "-")
                : "null";
        int bookIndex = verse.getChapter().getBook().getBookNumber();
        return "verse-" + bookIndex + "-" + bookName + "-" + verse.getChapter().getNum() + "-" + verse.getNum();
    }

    /**
     * Render the search results in the preview panel.
     * Each verse will be rendered in an HTML format and made clickable for selection.
     *
     * @param results The list of Bible verses to render.
     */
    private void renderSearchResults(List<BibleVerse> results) {
        // Check the current theme setting
        boolean isDarkTheme = QueleaProperties.get().getUseDarkTheme();

        // Set the HTML content style based on the theme
        String backgroundColor = isDarkTheme ? "#2B2B2B" : "#FFFFFF"; // Dark theme: dark background, Light theme: white background
        String textColor = isDarkTheme ? "#FFFFFF" : "#000000"; // Adjust text color for visibility based on theme

        // Set the font family, size, and line height
        String fontFamily = "font-family: 'Arial', 'Helvetica', 'Sans-Serif';";
        String fontSize = "font-size: 16px;";
        String lineHeight = "line-height: 1.5;";
        String padding = "padding: 10px;";
        String paragraphStyle = fontFamily + fontSize + lineHeight + padding;

        // Generate the HTML structure for displaying verses with appropriate styling
        StringBuilder htmlContent = new StringBuilder(
                String.format("<html><body style='background-color:%s; color:%s;'>", backgroundColor, textColor));

        for (BibleVerse verse : results) {
            // Generate a unique ID that includes the book name, chapter, and verse number
            String bookName = (verse.getChapter().getBook().getName() != null)
                    ? verse.getChapter().getBook().getName().replaceAll("\\s+", "-")  // Replace spaces in book names with dashes
                    : "null";  // Use "null" if the book name is null
//            int bookIndex = verse.getChapter().getBook().getBookNumber();  // Get the book index
//            String verseId = "verse-" + bookIndex + "-" + bookName + "-" + verse.getChapter().getNum() + "-" + verse.getNum();
            String verseId = generateVerseId(verse);

            // Set the background and border color for the selected verse
            String backgroundColorStyle = verse.equals(selectedVerse) ? "rgba(0, 255, 0, 0.2)" : "transparent"; // Green transparent background for selected verse
            String borderColor = verse.equals(selectedVerse) ? "green" : "transparent";

            htmlContent.append(String.format(
                    "<p id='%s' style='border: 2px solid %s; background-color: %s; %s' onclick='selectVerse(\"%s\")'><strong>%s %d:%d</strong> %s</p>",
                    verseId, borderColor, backgroundColorStyle, paragraphStyle, verseId, bookName, verse.getChapter().getNum(), verse.getNum(),
                    verse.getVerseText()));
        }

        htmlContent.append(
                "<script>" +
                        "var selectedVerseId = null;" + // Global variable to store the selected verse
                        "function selectVerse(verseId) {" +
                        "   var allVerses = document.querySelectorAll('p[id^=\"verse-\"]');" +
                        "   allVerses.forEach(v => {" +
                        "       v.style.border = '2px solid transparent';" +
                        "       v.style.backgroundColor = 'transparent';" +
                        "   });" + // Reset all borders and background colors
                        "   var selectedVerse = document.getElementById(verseId);" +
                        "   selectedVerse.style.border = '2px solid green';" + // Highlight selected verse
                        "   selectedVerse.style.backgroundColor = 'rgba(0, 255, 0, 0.2)';" + // Set green transparent background
                        "   selectedVerseId = verseId;" + // Save the selected verse ID
                        "   window.javaHandler.onVerseSelected(verseId);" + // Pass the selected verse back to Java (optional)
                        "}" +
                        "function scrollTo(eleID) {" + // Ensure scrolling behavior
                        "   var e = document.getElementById(eleID);" +
                        "   if (!!e && e.scrollIntoView) {" +
                        "       e.scrollIntoView({ behavior: 'smooth', block: 'center' });" + // Scroll smoothly and center the element
                        "   }" +
                        "}" +
                        "</script>"
        );

        htmlContent.append("</body></html>");

        webEngine.loadContent(htmlContent.toString());

        // Set up JavaScript bridge for handling verse selections
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.javaHandler = { onVerseSelected: function(verseId) { window.javaApp.onVerseSelected(verseId); } };");
                updateSelectedVerseFromID(); // Update selected verse after the page is loaded
            }
        });
    }

    /**
     * Reads the selected verse ID from the JavaScript side.
     */
    private void updateSelectedVerseFromID() {
        // Get the selected verse ID from JavaScript
        String selectedVerseId = (String) webEngine.executeScript("selectedVerseId"); // Reads the global JS variable

        if (selectedVerseId != null && !selectedVerseId.isEmpty()) {
            onVerseSelected(selectedVerseId); // Call the existing method to handle the selection
        } else {
            // Handle case where no verse is selected
            System.out.println("No verse is selected.");
        }
    }

    // JavaScript bridge method to handle verse selection
    public void onVerseSelected(String verseId) {
        // Logic to handle verse selection
        String[] parts = verseId.split("-");
        int bookIndex = Integer.parseInt(parts[1]); // Book index
        String bookName = parts[2].equals("null") ? null : parts[2].replace("-", " "); // Convert book name back by replacing dashes with spaces or set to null
        int chapterNum = Integer.parseInt(parts[3]); // Chapter number
        int verseNum = Integer.parseInt(parts[4]); // Verse number

        selectedVerse = verses.stream()
                .filter(verse -> verse.getChapter().getBook().getBookNumber() == bookIndex
                        && (bookName == null || verse.getChapter().getBook().getName().equalsIgnoreCase(bookName)) // Ignore book name if it's null
                        && verse.getChapter().getNum() == chapterNum
                        && verse.getNum() == verseNum)
                .findFirst()
                .orElse(null);

        // Re-render the verses to apply the green border and background color to the selected verse
        renderSearchResults(verses);
        updateButtonState(); // Update button state based on selection
    }

    /**
     * Switch to the LibraryBiblePanel and focus on the selected verse.
     */
    private void goToBible() {
        updateSelectedVerseFromID();
        if (selectedVerse != null) {
            var libraryPanel = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel();
            var tabPane = libraryPanel.getTabPane();
            LibraryBiblePanel biblePanel = libraryPanel.getBiblePanel();

            // Find and select the Bible Search tab
            libraryPanel.getTabPane().getTabs().stream()
                    .filter(tab -> tab.getContent() == biblePanel)
                    .findFirst()
                    .ifPresent(tabPane.getSelectionModel()::select);

            // Set the Bible, book, chapter, and verse in the LibraryBiblePanel
            biblePanel.getBibleSelector().getSelectionModel().select(selectedVerse.getChapter().getBook().getBible());
            biblePanel.getBookSelector().getSelectionModel().select(selectedVerse.getChapter().getBook());
            biblePanel.getPassageSelector().setText(String.format("%d:%d", selectedVerse.getChapter().getNum(), selectedVerse.getNum()));
        }
    }

    /**
     * Update the button state based on the selected verse.
     * Enables or disables the "Go to Bible", "Add Chapter", and "Add Verses" buttons.
     */
    private void updateButtonState() {
        boolean isVerseSelected = selectedVerse != null;
        goToBibleButton.setDisable(!isVerseSelected); // Enable "Go to Bible" button only if a verse is selected
        addChapter.setDisable(!isVerseSelected); // Enable "Add Chapter" button only if a verse is selected
        addVerses.setDisable(!isVerseSelected); // Enable "Add Verses" button only if a verse is selected
    }


    /**
     * Add Chapter action method.
     * Instead of extracting all verses first, we construct the BiblePassage with chapter information.
     */
    private void addChapter() {
        if (selectedVerse != null) {
            Bible selectedBible = bibleSelector.getSelectionModel().getSelectedItem();
            if (selectedBible != null) {
                // Construct a BiblePassage using the selected Bible and chapter
                BiblePassage passage = new BiblePassage(
                        selectedBible.getName(),                             // Bible name
                        selectedVerse.getChapter().getBook().getName(),      // Book name
                        selectedVerse.getChapter().getVerses(),              // All verses in the selected chapter
                        true                                                 // Multi flag for multiple verses
                );

                // Add the passage to the schedule
                addToSchedule(passage, selectedVerse.getNum()-1);
            }
        }
    }

    /**
     * Add Verse action method.
     * Constructs a BiblePassage with only the selected verse.
     */
    private void addVerses() {
        if (selectedVerse != null) {
            Bible selectedBible = bibleSelector.getSelectionModel().getSelectedItem();
            if (selectedBible != null) {
                // Construct a BiblePassage with the single selected verse
                BiblePassage passage = new BiblePassage(
                        selectedBible.getName(),                             // Bible name
                        selectedVerse.getChapter().getBook().getName(),      // Book name
                        new BibleVerse[]{selectedVerse},                     // Single verse
                        false                                                // Not multiple verses, so multi is false
                );

                // Add the passage to the schedule
                addToSchedule(passage, 0);
            }
        }
    }

    /**
     * General method to add the selected verse(s) or chapter to the schedule.
     * @param passage The constructed BiblePassage to be added to the schedule.
     */
    private void addToSchedule(BiblePassage passage,int index) {
        // Get the SchedulePanel and add the newly created BiblePassage to it
        SchedulePanel schedulePanel = QueleaApp.get().getMainWindow().getMainPanel().getSchedulePanel();
        schedulePanel.getScheduleList().add(passage);  // Add the passage to the schedule

        // Optionally, trigger Go Live if the "Go Live" checkbox is selected
        if (goLiveCheckBox.isSelected()) {
            Platform.runLater(() -> schedulePanel.goLiveWithLastAddedItem(index));
        }

        // Clear the search input field after adding the passage
        searchField.setText("");
    }


    /**
     * Get selected verses based on the current chapter.
     */
    private BibleVerse[] getSelectedVersesForChapter(BibleVerse verse) {
        return verse.getChapter().getVerses();
    }

}
