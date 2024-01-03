import mido
import time

class MidiFunctionality:
    def __init__(self):
        pass

    def list_midi_input_devices(self):
        """List available MIDI input devices."""
        input_names = mido.get_input_names()
        return input_names if input_names else ['']

    def wait_for_midi_input(self, device_name, timeout=10):
        input_names = mido.get_input_names()
        """Wait for MIDI input on a specific device and return the MIDI message."""
        if device_name not in input_names:
            print("Device not found.")
            return None

        with mido.open_input(device_name) as inport:
            print(f"Listening for MIDI input on '{device_name}'...")
            start_time = time.time()
            while True:
                for msg in inport.iter_pending():
                    return msg

                # Check timeout
                if time.time() - start_time > timeout:
                    print("Timeout waiting for MIDI input.")
                    return None

                time.sleep(0.001)

    def send_midi_message(self, msg):
        """Send a MIDI message."""
        output_names = mido.get_output_names()
        if not output_names:
            print("No output MIDI device available.")
            return

        with mido.open_output(output_names[0]) as outport:
            outport.send(msg)

    def midi_message_to_string(self, midi_msg):
        """Convert a mido MIDI message to a string format consistent with the Java implementation."""
        enabled = "true"
        type_str = midi_msg.type
        channel = midi_msg.channel + 1 if hasattr(midi_msg, 'channel') else 0
        data1 = self.get_data1_from_message(midi_msg)

        return f"{enabled},{self.type_to_string(type_str)},{channel},{data1}"

    def get_data1_from_message(self, midi_msg):
        """Extract the data1 value from the MIDI message."""
        if hasattr(midi_msg, 'note'):
            return midi_msg.note
        elif hasattr(midi_msg, 'control'):
            return midi_msg.control
        elif hasattr(midi_msg, 'program'):
            return midi_msg.program
        elif hasattr(midi_msg, 'value'):
            return midi_msg.value
        elif hasattr(midi_msg, 'pitch'):
            return midi_msg.pitch
        return 0


    def string_to_midi_message(self, string_message):
        """Convert a string-formatted MIDI message to a mido MIDI message."""
        parts = string_message.split(',')
        enabled = parts[0] == 'true'
        type_str = self.string_to_type(parts[1])
        channel = int(parts[2]) - 1
        data1 = int(parts[3])
        # TODO: This could be a control change instead or something else.
        # TODO: The method is not universal
        return mido.Message(type_str, channel=channel, note=data1, velocity=0)

    def type_to_string(self, midi_type):
        """Map a mido MIDI type to its string representation."""
        mapping = {
            'note_off': "NOTE_OFF",
            'note_on': "NOTE_ON",
            'control_change': "CONTROL_CHANGE",
            'program_change': "PROGRAM_CHANGE",
            'pitchwheel': "PITCH_BEND",
            'channel_pressure': "CHANNEL_PRESSURE",
            'polytouch': "POLY_PRESSURE",
            # Add other mappings as needed
        }
        return mapping.get(midi_type, "UNKNOWN")

    def string_to_type(self, type_str):
        """Map a string representation of MIDI type to a mido MIDI type."""
        mapping = {
            "NOTE_OFF": 'note_off',
            "NOTE_ON": 'note_on',
            "CONTROL_CHANGE": 'control_change',
            "PROGRAM_CHANGE": 'program_change',
            "PITCH_BEND": 'pitchwheel',
            "CHANNEL_PRESSURE": 'channel_pressure',
            "POLY_PRESSURE": 'polytouch',
            # Add other mappings as needed
        }
        return mapping.get(type_str, None)
    
# Example Usage
if __name__ == "__main__":
    midi_func = MidiFunctionality()
    input_devices = midi_func.list_midi_input_devices()
    print("Input MIDI Devices:", input_devices)

    # Example of sending a MIDI message
    note_on_msg = mido.Message('note_on', note=60, velocity=112)
    midi_func.send_midi_message(note_on_msg)

    # Example of waiting for MIDI input
    if input_devices:
        device_name = input_devices[0]
        midi_message = midi_func.wait_for_midi_input(device_name)
        if midi_message:
            print(f"MIDI Message received: {midi_message}")
            print("String format of MIDI message:", midi_func.midi_message_to_string(midi_message))

            # Convert back to MIDI message
            string_message = midi_func.midi_message_to_string(midi_message)
            converted_message = midi_func.string_to_midi_message(string_message)
            print("Converted back to MIDI message:", converted_message)
