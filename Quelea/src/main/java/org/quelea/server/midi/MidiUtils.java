package org.quelea.server.midi;

import org.quelea.services.utils.LoggerUtils;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MidiUtils {

    private static final Logger LOGGER = LoggerUtils.getLogger();

    /**
     * Retrieves a list of available MIDI input devices.
     * @return List<MidiDevice.Info> List of input device information
     */
    public static List<MidiDevice.Info> getMidiInputDeviceList() {
        List<MidiDevice.Info> inputDevices = new ArrayList<>();
        MidiDevice.Info[] midiInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : midiInfos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxTransmitters() != 0) {
                    inputDevices.add(info);
                }
            } catch (MidiUnavailableException e) {
                LOGGER.log(Level.WARNING, "MIDI device unavailable: {0}", info);
            }
        }
        return inputDevices;
    }

    /**
     * Retrieves a list of available MIDI output devices.
     * @return List<MidiDevice.Info> List of output device information
     */
    public static List<MidiDevice.Info> getMidiOutputDeviceList() {
        List<MidiDevice.Info> outputDevices = new ArrayList<>();
        MidiDevice.Info[] midiInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : midiInfos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxReceivers() != 0) {
                    outputDevices.add(info);
                }
            } catch (MidiUnavailableException e) {
                LOGGER.log(Level.WARNING, "MIDI device unavailable: {0}", info);
            }
        }
        return outputDevices;
    }

    /**
     * Receives a single MIDI event from the specified device.
     * @param deviceInfo The MIDI device info.
     * @return The received MIDI message.
     * @throws MidiUnavailableException
     * @throws InterruptedException
     */
    public static MidiMessage receiveMidiEvent(MidiDevice.Info deviceInfo) throws MidiUnavailableException, InterruptedException {
        MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
        device.open();
        AtomicReference<MidiMessage> receivedMessage = new AtomicReference<>();

        Transmitter transmitter = device.getTransmitter();
        transmitter.setReceiver(new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                receivedMessage.set(message);
                synchronized (receivedMessage) {
                    receivedMessage.notify();
                }
            }

            @Override
            public void close() {
            }
        });

        synchronized (receivedMessage) {
            while (receivedMessage.get() == null) {
                receivedMessage.wait();
            }
        }

        transmitter.close();
        device.close();
        return receivedMessage.get();
    }

    /**
     * Sends a single MIDI event to the specified device.
     * @param deviceInfo The MIDI device info.
     * @param message The MIDI message to send.
     * @throws MidiUnavailableException
     */
    public static void sendMidiEvent(MidiDevice.Info deviceInfo, MidiMessage message) throws MidiUnavailableException {
        MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
        device.open();
        Receiver receiver = device.getReceiver();
        receiver.send(message, -1);
        receiver.close();
        device.close();
    }

    /**
     * Converts a MIDI message type (integer) to its corresponding string representation.
     * @param typeInt MIDI message type as an integer.
     * @return String representation of the MIDI message type.
     */
    public static String midiTypeIntToStr(int typeInt) {
        switch (typeInt) {
            case 0xFE:
                return "ACTIVE_SENSING";
            case 0xD0:
                return "CHANNEL_PRESSURE";
            case 0xFB:
                return "CONTINUE";
            case 0xB0:
                return "CONTROL_CHANGE";
            case 0xF7:
                return "END_OF_EXCLUSIVE";
            case 0xF1:
                return "MIDI_TIME_CODE";
            case 0x80:
                return "NOTE_OFF";
            case 0x90:
                return "NOTE_ON";
            case 0xE0:
                return "PITCH_BEND";
            case 0xA0:
                return "POLY_PRESSURE";
            case 0xC0:
                return "PROGRAM_CHANGE";
            case 0xF2:
                return "SONG_POSITION_POINTER";
            case 0xF3:
                return "SONG_SELECT";
            case 0xFA:
                return "START";
            case 0xFC:
                return "STOP";
            case 0xFF:
                return "SYSTEM_RESET";
            case 0xF8:
                return "TIMING_CLOCK";
            case 0xF6:
                return "TUNE_REQUEST";
            default:
                throw new IllegalArgumentException("Unknown MIDI message type: " + typeInt);
        }
    }

    /**
     * Converts a string representation of a MIDI message type to its corresponding integer value.
     * @param typeStr String representation of the MIDI message type.
     * @return Integer value of the MIDI message type.
     */
    public static int midiTypeStrToInt(String typeStr) {
        switch (typeStr) {
            case "ACTIVE_SENSING":
                return 0xFE;
            case "CHANNEL_PRESSURE":
                return 0xD0;
            case "CONTINUE":
                return 0xFB;
            case "CONTROL_CHANGE":
                return 0xB0;
            case "END_OF_EXCLUSIVE":
                return 0xF7;
            case "MIDI_TIME_CODE":
                return 0xF1;
            case "NOTE_OFF":
                return 0x80;
            case "NOTE_ON":
                return 0x90;
            case "PITCH_BEND":
                return 0xE0;
            case "POLY_PRESSURE":
                return 0xA0;
            case "PROGRAM_CHANGE":
                return 0xC0;
            case "SONG_POSITION_POINTER":
                return 0xF2;
            case "SONG_SELECT":
                return 0xF3;
            case "START":
                return 0xFA;
            case "STOP":
                return 0xFC;
            case "SYSTEM_RESET":
                return 0xFF;
            case "TIMING_CLOCK":
                return 0xF8;
            case "TUNE_REQUEST":
                return 0xF6;
            default:
                throw new IllegalArgumentException("Unknown MIDI message type: " + typeStr);
        }
    }
}
