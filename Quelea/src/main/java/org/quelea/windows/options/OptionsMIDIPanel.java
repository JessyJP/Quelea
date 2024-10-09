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

    /**
     * Create the options MIDI panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    OptionsMIDIPanel(HashMap<Field, ObservableValue> bindings) {
        this.bindings = bindings;
        this.MIC = QueleaApp.get().getMidiInterfaceConnector();
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
        // Bind the ComboBox to the globalMidiChannelProperty
        channelDropdown.valueProperty().bindBidirectional(globalMidiChannelProperty.asObject());

        // Dynamically generate MIDI control groups from the MIDI event list in MidiInterfaceConnector
        List<Group> midiControlGroups = MIC.getMidiEventMap().values().stream()
                .map(this::createMidiControlGroup)
                .collect(Collectors.toList());

        // Combine all groups, add the MIDI options including the new dropdown for MIDI channels
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

    private Group createMidiControlGroup(MidiEvent midiEvent) {
//        BooleanProperty actionEnabled = new SimpleBooleanProperty(midiEvent.isEnabled());
//        SimpleIntegerProperty midiChannel = new SimpleIntegerProperty(midiEvent.getChannel() + 1); // Channels are 0-indexed
        SimpleStringProperty midiType = new SimpleStringProperty(MidiUtils.midiTypeIntToStr(midiEvent.getType()));
        SimpleIntegerProperty midiValue = new SimpleIntegerProperty(midiEvent.getNote());

        return Group.of(LabelGrabber.INSTANCE.getLabel(midiEvent.Key),
//                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.enable.label"), actionEnabled),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.type.label"), midiType),
//                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.channel.label"), midiChannel, 1, 16),
                Setting.of(LabelGrabber.INSTANCE.getLabel("midi.action.value.label"), midiValue, 0, 127)
        );
    }
}
