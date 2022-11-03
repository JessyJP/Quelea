package org.quelea.server;

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
}
