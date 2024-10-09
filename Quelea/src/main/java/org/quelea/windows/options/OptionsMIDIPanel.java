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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.quelea.server.midi.MidiInterfaceConnector;
import org.quelea.server.midi.MidiUtils;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.windows.main.QueleaApp;

import javax.sound.midi.MidiDevice;
import org.quelea.server.midi.MidiEvent;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.quelea.services.utils.QueleaPropertyKeys.*;

/**
 * A panel where the MIDI options in the program are set.
 * <p/>
 *
 * @author Arvid
 */
public class OptionsMIDIPanel {
    private MidiInterfaceConnector MIC;
    private BooleanProperty enableMidiProperty;
    private SimpleStringProperty selectedInputDeviceProperty;
    private SimpleIntegerProperty globalMidiChannelProperty;
    private HashMap<Field, ObservableValue> bindings;

    // Create dynamic groups for MIDI control events
    List<String> midiControlEvents = List.of(
            midiAction_Clear, midiAction_Black, midiAction_Gotoitem, midiAction_Next, midiAction_Nextitem,
            midiAction_Play, midiAction_Prev, midiAction_Previtem, midiAction_Section, midiAction_Tlogo,
            midiAction_TransposeDown1, midiAction_TransposeDown2, midiAction_TransposeDown3, midiAction_TransposeDown4,
            midiAction_TransposeDown5, midiAction_TransposeDown6, midiAction_TransposeUp0, midiAction_TransposeUp1,
            midiAction_TransposeUp2, midiAction_TransposeUp3, midiAction_TransposeUp4, midiAction_TransposeUp5,
            midiAction_TransposeUp6
    );

    /**
     * Create the options MIDI panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsMIDIPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        enableMidiProperty = new SimpleBooleanProperty(QueleaProperties.get().getUseMidiControl());
        globalMidiChannelProperty = new SimpleIntegerProperty(QueleaProperties.get().getGlobalMidiChannel());

        // Initialize MIDI device dropdown
        ObservableList<String> midiDevices = FXCollections.observableArrayList(
                MidiUtils.getMidiInputDeviceList().stream().map(MidiDevice.Info::getName).collect(Collectors.toList())
        );
        midiDevices.addAll(
                MidiUtils.getMidiOutputDeviceList().stream().map(MidiDevice.Info::getName).collect(Collectors.toList())
        );

        selectedInputDeviceProperty = new SimpleStringProperty(midiDevices.isEmpty() ? null : midiDevices.get(0));
    }

    public Category getMidiTab() {
        // Create ComboBox for MIDI devices
        ComboBox<String> deviceDropdown = new ComboBox<>();
        deviceDropdown.setItems(FXCollections.observableArrayList(
                MidiUtils.getMidiInputDeviceList().stream().map(MidiDevice.Info::getName).collect(Collectors.toList())
        ));
        deviceDropdown.getItems().addAll(
                MidiUtils.getMidiOutputDeviceList().stream().map(MidiDevice.Info::getName).collect(Collectors.toList())
        );
        deviceDropdown.valueProperty().bindBidirectional(selectedInputDeviceProperty);
        // Create ComboBox for MIDI channels
        ObservableList<Integer> midiChannels = FXCollections.observableArrayList();
        for (int i = 1; i <= 16; i++) {
            midiChannels.add(i);
        }

        ComboBox<Integer> channelDropdown = new ComboBox<>(midiChannels);
        channelDropdown.valueProperty().bindBidirectional(globalMidiChannelProperty.asObject());



        // Generate groups for each MIDI control event
        List<Group> midiControlGroups = midiControlEvents.stream()
                .map(this::createMidiControlGroup)
                .collect(Collectors.toList());

        // Combine all groups
        Group[] allGroups = new Group[midiControlGroups.size() + 1];
        allGroups[0] = Group.of(LabelGrabber.INSTANCE.getLabel("midi.options.group"),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.enable.label"), enableMidiProperty).customKey(midiEnabled),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.device.label"), selectedInputDeviceProperty).customKey(midiInterfaceIn),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.channel.label"), IntegerProperty.integerProperty(channelDropdown.valueProperty())).customKey(midiGlobalChannel)
        );
        System.arraycopy(midiControlGroups.toArray(), 0, allGroups, 1, midiControlGroups.size());

        return Category.of(LabelGrabber.INSTANCE.getLabel("midi.options.heading"), new ImageView(new Image("file:icons/midisettingsicon.png")),
                allGroups
        );
    }

    private Group createMidiControlGroup(String midiAction) {
        String actionValue = QueleaProperties.get().getProperty(midiAction, "true,NOTE_ON,16,0");
        String[] parts = actionValue.split(",");
        BooleanProperty actionEnabled = new SimpleBooleanProperty(Boolean.parseBoolean(parts[0]));
        SimpleStringProperty midiType = new SimpleStringProperty(parts[1]);
        SimpleIntegerProperty midiChannel = new SimpleIntegerProperty(Integer.parseInt(parts[2]));
        SimpleIntegerProperty midiValue = new SimpleIntegerProperty(Integer.parseInt(parts[3]));

        return Group.of(LabelGrabber.INSTANCE.getLabel(midiAction),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.enable.label"), actionEnabled),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.type.label"), midiType),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.channel.label"), midiChannel, 1, 16),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.value.label"), midiValue, 0, 127)
        );
    }
}
