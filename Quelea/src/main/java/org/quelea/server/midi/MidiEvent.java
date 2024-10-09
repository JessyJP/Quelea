package org.quelea.server.midi;

import org.jetbrains.annotations.NotNull;
import org.quelea.services.utils.LoggerUtils;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MidiEvent {
    // Constructor and properties
    private static final Logger LOGGER = LoggerUtils.getLogger();
    public boolean Enabled = true;
    public int type = ShortMessage.NOTE_ON;
    public int channel = 16 - 1;
    public int note;// Data 1
    public int velocity = 0;// Data 2
    public String callbackName;
    public String Key;

    MidiEvent(String propertyString, String callbackName_in) {
        this.stringPropertyToMidiEvent(propertyString);
        this.callbackName = callbackName_in;
        this.Key = "midi.Action."+callbackName_in.toLowerCase();
        LOGGER.log(Level.INFO, "Add midi control event:[" + this.propertiesToString() + "] for [" + propertyString + "] for [" + callbackName + "]");
    }

    // Get and Set properties
    public String propertiesToString() {
        return ""
                + Enabled + ","
                + type + ","
                + (channel + 1) + ","
                + note;
    }

    public void stringPropertyToMidiEvent(String propertyStrIn) {
        propertyStrIn = propertyStrIn.replaceAll("\\s", "");// Remove any white space
        String[] properties = propertyStrIn.split(",");
        this.Enabled = Boolean.valueOf(properties[0]);
        this.type = MidiUtils.midiTypeStrToInt(properties[1]);
        this.channel = (Integer.parseInt(properties[2]) - 1);
        this.note = Integer.valueOf(properties[3]); // TODO: NOTE! This might actually not mean note but simply midi data at the corresponding array position
    }


    // Make a qucik find key
    // --- For quick access
    @Override
    public String toString() {
        ShortMessage tmpMsg = new ShortMessage();
        try {
            tmpMsg.setMessage(this.type, this.channel, this.note, this.velocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return tmpMsg.toString();
    }

    public ShortMessage toMidiMessage() throws InvalidMidiDataException {
        //ShortMessage msg = new ShortMessage();
        //msg.setMessage(type,channel,note,velocity);
        //return  msg;
        return (new ShortMessage(type, channel, note, velocity));
    }

    public ShortMessage toMidiMessage(int velocity_) throws InvalidMidiDataException {
        //ShortMessage msg = new ShortMessage();
        //msg.setMessage(type,channel,note,velocity);
        //return  msg;
        return (new ShortMessage(type, channel, note, velocity_));
    }

    public boolean match(@NotNull MidiMessage m) throws InvalidMidiDataException {
        byte[] LB = this.toMidiMessage().getMessage();
        byte[] b = m.getMessage();
        return b[0] == LB[0] && b[1] == LB[1];
    }


    // -------------------------
    // Getters and Setters
    public boolean isEnabled() {
        return Enabled;
    }

    public int getType() {
        return type;
    }

    public int getChannel() {
        return channel;
    }

    public int getNote() {
        return note;
    }

    public void setEnabled(boolean enabled) {
        this.Enabled = enabled;
    }

    public void setType(int type) {
        // List of valid message types for MIDI
        List<Integer> validTypes = List.of(
                0xFE, 0xD0, 0xFB, 0xB0, 0xF7, 0x80, 0x90, 0xE0, 0xA0, 0xC0, 0xF2, 0xF3, 0xFA, 0xFC, 0xFF, 0xF8, 0xF6
        );

        if (validTypes.contains(type)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid MIDI message type: " + type);
        }
    }

    public void setChannel(int channel) {
        // MIDI channels are 0-indexed, and valid values are from 0 to 15.
        if (channel >= 0 && channel <= 15) {
            this.channel = channel;
        } else {
            throw new IllegalArgumentException("MIDI channel must be between 0 and 15. Provided: " + channel);
        }
    }

    public void setNote(int note) {
        // MIDI notes are between 0 and 127
        if (note >= 0 && note <= 127) {
            this.note = note;
        } else {
            throw new IllegalArgumentException("MIDI note must be between 0 and 127. Provided: " + note);
        }
    }
}
