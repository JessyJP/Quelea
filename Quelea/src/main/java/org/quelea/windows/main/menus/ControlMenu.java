package org.quelea.windows.main.menus;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.windows.main.QueleaApp;

/**
 * Quelea's control menu.
 * <p>
 * This menu contains entries related to control actions such as toggling visibility
 * of panels and searching Bible text.
 *
 * @author Michael
 */
public class ControlMenu extends Menu {

    private final CheckMenuItem togglePreviewPanelItem;
    private final MenuItem bibleTextSearchItem;

    /**
     * Create the control menu.
     */
    public ControlMenu() {
        super("Control");

        // Add checkbox to control PreviewPanel visibility
        togglePreviewPanelItem = new CheckMenuItem(LabelGrabber.INSTANCE.getLabel("Toggle Preview Panel Visibility"));
        togglePreviewPanelItem.setSelected(true); // Default to true, showing the panel initially
        togglePreviewPanelItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)); // Set shortcut Ctrl + Shift + V
        togglePreviewPanelItem.setOnAction(event ->
                QueleaApp.get().getMainWindow().getMainPanel().setPreviewPanelVisibility(togglePreviewPanelItem.isSelected()));
        getItems().add(togglePreviewPanelItem);

        // New Menu Item for Bible Text Search
        bibleTextSearchItem = new MenuItem("Bible Text Search", new ImageView(new Image("file:icons/looking_glass.png", 20, 20, false, true))); // Looking glass icon
        bibleTextSearchItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN)); // Ctrl + F
        bibleTextSearchItem.setOnAction(event -> {
            // Switch to the Bible Search tab before requesting focus on the search field
            var libraryPanel = QueleaApp.get().getMainWindow().getMainPanel().getLibraryPanel();
            var tabPane = libraryPanel.getTabPane();

            // Find and select the Bible Search tab
            libraryPanel.getTabPane().getTabs().stream()
                    .filter(tab -> tab.getContent() == libraryPanel.getBibleSearchPanel())
                    .findFirst()
                    .ifPresent(tabPane.getSelectionModel()::select);

            // Now request focus on the search field inside the Bible Search Panel
            libraryPanel.getBibleSearchPanel().getSearchField().requestFocus();
        });
        getItems().add(bibleTextSearchItem);
    }
}
