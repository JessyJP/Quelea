import rtmidi
from rtmidi.midiconstants import (
    NOTE_ON, NOTE_OFF, CONTROL_CHANGE, PROGRAM_CHANGE,
    PITCH_BEND, CHANNEL_PRESSURE, POLY_PRESSURE,
    SONG_POSITION_POINTER, SONG_SELECT, TUNE_REQUEST,
    END_OF_EXCLUSIVE, TIMING_CLOCK, ACTIVE_SENSING, SYSTEM_RESET, MIDI_TIME_CODE
)
# Additional MIDI message constants
START = 0xFA  # Start message (decimal value: 250)
STOP = 0xFC   # Stop message (decimal value: 252)
CONTINUE = 0xFB  # Continue message (decimal value: 251)


import time

class MidiFunctionality:
    def __init__(self):
        self.midiin = rtmidi.MidiIn()
        self.midiout = rtmidi.MidiOut()

    def list_midi_input_devices(self):
        """List available MIDI input devices."""
        input_devices = self.midiin.get_ports()
        return input_devices if input_devices else ['']

    def wait_for_midi_input(self, device_name):
        """Wait for MIDI input on a specific device and return the MIDI message."""
        port_index = self.get_device_index(device_name)
        if port_index is None:
            print("Device not found.")
            return None

        # Open the MIDI port
        self.midiin.open_port(port_index)
        print(f"Listening for MIDI input on '{device_name}'...")

        midi_message = None
        while True:
            message, delta_time = self.midiin.get_message()
            if message:
                midi_message = message
                break

            time.sleep(0.001)

        # Close the MIDI port
        self.midiin.close_port()
        return midi_message
    
    def get_device_index(self, device_name):
        """Get the index of the device with the given name."""
        input_devices = self.list_midi_input_devices()
        for index, name in enumerate(input_devices):
            if device_name in name:
                return index
        return None

    def send_midi_message(self, type, channel, note, velocity):
        """Send a MIDI message."""
        try:
            self.midiout.open_port(0)  # Assuming the first port is the desired one
            midi_message = [type, note, velocity]
            self.midiout.send_message(midi_message)
        finally:
            self.midiout.close_port()

    def midi_message_to_string(self, midi_message):
        """Convert MIDI message to string format."""
        type, note, velocity = midi_message
        enabled = "true"
        return f"{enabled},{self.type_to_string(type)},{note+1},{velocity}"

    def string_to_midi_message(self, string_message):
        """Convert string formatted MIDI message to MIDI message components."""
        parts = string_message.split(',')
        enabled = parts[0] == 'true'
        type = self.string_to_type(parts[1])
        channel = int(parts[2]) - 1
        note = int(parts[3])
        return enabled, type, channel, note

    def type_to_string(self, type):
        """Map the integer MIDI type to its string representation."""
        mapping = {
            NOTE_OFF: "NOTE_OFF",
            NOTE_ON: "NOTE_ON",
            CONTROL_CHANGE: "CONTROL_CHANGE",
            PROGRAM_CHANGE: "PROGRAM_CHANGE",
            PITCH_BEND: "PITCH_BEND",
            CHANNEL_PRESSURE: "CHANNEL_PRESSURE",
            POLY_PRESSURE: "POLY_PRESSURE",
            SONG_POSITION_POINTER: "SONG_POSITION_POINTER",
            SONG_SELECT: "SONG_SELECT",
            TUNE_REQUEST: "TUNE_REQUEST",
            END_OF_EXCLUSIVE: "END_OF_EXCLUSIVE",
            TIMING_CLOCK: "TIMING_CLOCK",
            START: "START",
            CONTINUE: "CONTINUE",
            STOP: "STOP",
            ACTIVE_SENSING: "ACTIVE_SENSING",
            SYSTEM_RESET: "SYSTEM_RESET",
            MIDI_TIME_CODE: "MIDI_TIME_CODE"
        }
        return mapping.get(type, "UNKNOWN")

    def string_to_type(self, type_str):
        """Map the string representation of MIDI type to its integer value."""
        mapping = {
            "NOTE_OFF": NOTE_OFF,
            "NOTE_ON": NOTE_ON,
            "CONTROL_CHANGE": CONTROL_CHANGE,
            "PROGRAM_CHANGE": PROGRAM_CHANGE,
            "PITCH_BEND": PITCH_BEND,
            "CHANNEL_PRESSURE": CHANNEL_PRESSURE,
            "POLY_PRESSURE": POLY_PRESSURE,
            "SONG_POSITION_POINTER": SONG_POSITION_POINTER,
            "SONG_SELECT": SONG_SELECT,
            "TUNE_REQUEST": TUNE_REQUEST,
            "END_OF_EXCLUSIVE": END_OF_EXCLUSIVE,
            "TIMING_CLOCK": TIMING_CLOCK,
            "START": START,
            "CONTINUE": CONTINUE,
            "STOP": STOP,
            "ACTIVE_SENSING": ACTIVE_SENSING,
            "SYSTEM_RESET": SYSTEM_RESET,
            "MIDI_TIME_CODE": MIDI_TIME_CODE
        }
        return mapping.get(type_str, 0)

# Example Usage
if __name__ == "__main__":
    midi_func = MidiFunctionality()
    input_devices = midi_func.list_midi_input_devices()
    print("Input MIDI Devices:", input_devices)

    # Example of sending a MIDI message
    midi_func.send_midi_message(NOTE_ON, 0, 60, 112)  # Middle C with velocity 112

    # Example of waiting for MIDI input
    if input_devices:
        device_name = input_devices[0]
        midi_message = midi_func.wait_for_midi_input(device_name)
        print(f"MIDI Message received: {midi_message}")
        print("String format of MIDI message:", midi_func.midi_message_to_string(midi_message))

        # Convert back to MIDI message
        string_message = midi_func.midi_message_to_string(midi_message)
        converted_message = midi_func.string_to_midi_message(string_message)
        print("Converted back to MIDI message:", converted_message)
