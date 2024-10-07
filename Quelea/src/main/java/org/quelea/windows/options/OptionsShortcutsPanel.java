package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import java.util.HashMap;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.services.languages.LabelGrabber;

/**
 * The panel that shows the static shortcut key binding information
 * <p/>
 *
 * @author [Your Name]
 */
public class OptionsShortcutsPanel {
    private HashMap<Field, ObservableValue> bindings;

    /**
     * Create the options shortcut panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsShortcutsPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
    }

    public Category getShortcutsTab() {
        // Create SimpleStringProperty for each shortcut
        SimpleStringProperty createServiceShortcut = new SimpleStringProperty("Ctrl + N");
        SimpleStringProperty openServiceShortcut = new SimpleStringProperty("Ctrl + O");
        SimpleStringProperty saveServiceShortcut = new SimpleStringProperty("Ctrl + S");
        SimpleStringProperty startProjectionShortcut = new SimpleStringProperty("F9");
        SimpleStringProperty fullscreenModeShortcut = new SimpleStringProperty("F11");
        SimpleStringProperty nextSlideShortcut = new SimpleStringProperty("Space / Arrow Down");
        SimpleStringProperty previousSlideShortcut = new SimpleStringProperty("Backspace / Arrow Up");
        SimpleStringProperty boldTextShortcut = new SimpleStringProperty("Ctrl + B");
        SimpleStringProperty italicTextShortcut = new SimpleStringProperty("Ctrl + I");
        SimpleStringProperty underlineTextShortcut = new SimpleStringProperty("Ctrl + U");

        return Category.of(
                LabelGrabber.INSTANCE.getLabel("shortcut.options.heading"),
                new ImageView(new Image("file:icons/shortcutsettingsicon.png")),
                Setting.of("Create a new service", createServiceShortcut),
                Setting.of("Open an existing service", openServiceShortcut),
                Setting.of("Save the current service", saveServiceShortcut),
                Setting.of("Start or stop live projection", startProjectionShortcut),
                Setting.of("Toggle full-screen mode", fullscreenModeShortcut),
                Setting.of("Move to the next slide", nextSlideShortcut),
                Setting.of("Move to the previous slide", previousSlideShortcut),
                Setting.of("Bold the selected text", boldTextShortcut),
                Setting.of("Italicize the selected text", italicTextShortcut),
                Setting.of("Underline the selected text", underlineTextShortcut)
        );
    }
}
