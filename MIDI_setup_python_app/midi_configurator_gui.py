import tkinter as tk
from tkinter import filedialog, Canvas, Scrollbar, Frame, Text
from tkinter import messagebox
from config_file_handler import ConfigurationFilesHandler
from mido_midi_device_handler import MidiFunctionality
import re

class MidiConfiguratorGUI:
    def __init__(self, root, config_handler):
        self.root = root
        self.root.title("Midi Configurator")
        self.root.geometry("650x600")  # Set initial window dimensions

        self.config_handler = config_handler  # MidiConfigHandler instance
        self.midi_func = MidiFunctionality()  # Initialize MidiFunctionality

        # Row 1: File selection and Import/Export
        row1 = tk.Frame(self.root)
        row1.pack(fill=tk.X)

        self.reset_button = tk.Button(row1, text="Reset", command=self.reset_to_defaults)
        self.reset_button.pack(side=tk.LEFT, padx=5, pady=5)

        self.select_file_button = tk.Button(row1, text="Select Quelea Properties File", command=self.select_file)
        self.select_file_button.pack(side=tk.LEFT, padx=5, pady=5)

        self.file_path_entry = tk.Entry(row1)
        self.file_path_entry.pack(side=tk.LEFT, expand=True, fill=tk.X, padx=5, pady=5)

        import_button = tk.Button(row1, text="Import", command=self.import_from_config_file)
        import_button.pack(side=tk.LEFT, padx=5, pady=5)

        export_button = tk.Button(row1, text="Export", command=self.export_to_config_file)
        export_button.pack(side=tk.LEFT, padx=5, pady=5)

        self.config_handler.get_source_midi_properties() # Load default settings
        self.configuration_map = self.config_handler.create_output_dictionary() # Crete the default in terms of dictionary
        self.setup_midi_settings_gui()# Create the buttons based on the defaults
        self.reset_to_defaults()

        # ---------------------------------        
        self.current_assignment_key = None
        self.original_button_style = {"bg": "SystemButtonFace", "fg": "black"}  # Default button style

    def setup_midi_settings_gui(self):

        # MIDI Configuration Row
        midi_config_row = tk.Frame(self.root)
        midi_config_row.pack(fill=tk.X, padx=5, pady=5)

        # MIDI Enabled Checkbox
        self.midi_enabled_var = tk.BooleanVar()
        midi_enabled_checkbox = tk.Checkbutton(midi_config_row, text="MIDI Enabled", variable=self.midi_enabled_var)
        midi_enabled_checkbox.grid(row=0, column=0, padx=5, pady=5)
        midi_enabled_checkbox.config(command=lambda: self.update_property('midi.enabled', self.midi_enabled_var.get()))

        # Midi Device Name Label and Entry
        midi_device_label = tk.Label(midi_config_row, text="MIDI Device Name:")
        midi_device_label.grid(row=0, column=1, padx=5, pady=5)

        # MIDI Device Dropdown
        self.midi_device_var = tk.StringVar()
        self.midi_device_options = self.midi_func.list_midi_input_devices()  # Get list of MIDI input devices
        self.midi_device_menu = tk.OptionMenu(midi_config_row, self.midi_device_var, *self.midi_device_options)
        self.midi_device_menu.grid(row=0, column=2, padx=5, pady=5)
        self.midi_device_var.set(self.midi_device_options[0])  # Default value
        self.midi_device_var.trace("w", lambda *args: self.update_property('midi.interface', self.midi_device_var.get()))

        # Global Midi Channel Label and Spinbox
        midi_channel_label = tk.Label(midi_config_row, text="Global MIDI Channel:")
        midi_channel_label.grid(row=0, column=3, padx=5, pady=5)
        # Global Midi Channel Spinbox
        # Global Midi Channel Spinbox setup
        self.midi_channel_var = tk.StringVar()
        self.midi_channel_spinbox = tk.Spinbox(midi_config_row, from_=1, to=16, width=5, textvariable=self.midi_channel_var)
        self.midi_channel_spinbox.grid(row=0, column=4, padx=5, pady=5)
        self.midi_channel_var.trace("w", lambda *args: self.update_property('midi.globalChannel', self.midi_channel_var.get()))

        # ------------------------------------------------------------
        # Create the container for the scrollable frame and text area
        container = tk.Frame(self.root)
        container.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        # Scrollable Frame for MIDI Event Action Buttons
        self.setup_scrollable_midi_action_buttons(container)

        # Output Text Area
        self.output_text = Text(container)
        self.output_text.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True)

    def setup_scrollable_midi_action_buttons(self, container):
        canvas_width = 150  # Set a suitable width based on your UI needs

        # Create a canvas and a vertical scrollbar
        canvas = Canvas(container, width=canvas_width)
        scrollbar = Scrollbar(container, orient="vertical", command=canvas.yview)

        # Configure the canvas to work with the scrollbar
        canvas.configure(yscrollcommand=scrollbar.set)

        # Create a frame inside the canvas to hold the buttons
        self.scrollable_frame = Frame(canvas)
        canvas.create_window((0, 0), window=self.scrollable_frame, anchor="nw")

        # Bind the frame's configuration to adjust the scroll region of the canvas
        self.scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )

        # Bind the mousewheel to the canvas for scrolling
        canvas.bind_all("<MouseWheel>", lambda e: canvas.yview_scroll(int(-1*(e.delta/120)), "units"))

        # Pack the canvas and scrollbar into the container
        canvas.pack(side=tk.LEFT, fill=tk.Y, expand=True)
        scrollbar.pack(side=tk.LEFT, fill=tk.Y)

        # Populate the scrollable frame with buttons
        self.populate_midi_action_buttons(self.scrollable_frame)

    def populate_midi_action_buttons(self, container):
        # Create the midi event trigger assignment buttons
        self.midi_action_buttons = {}  # Initialize as a dictionary
        # Dynamically create MIDI Event Action Buttons
        for key in self.configuration_map.keys():
            if 'midi.Action' in key:  # Filter only MIDI action keys
                button_text = key.replace("midi.Action.", "").replace("_", " ").title()
                button = tk.Button(container, text=button_text, width=20,
                                   command=lambda key=key: self.midi_event_action_callback(key))
                button.pack(padx=5, pady=5)
                self.midi_action_buttons[key] = button  # Store the button with its key

    def update_midi_device_dropdown(self):
        # Get updated list of MIDI input devices
        self.midi_device_options = self.midi_func.list_midi_input_devices()

        # Clear the current menu entries
        menu = self.midi_device_menu["menu"]
        menu.delete(0, "end")

        # Add new options to the menu
        for device in self.midi_device_options:
            menu.add_command(label=device, 
                             command=lambda value=device: self.midi_device_var.set(value))

        # Update the dropdown's variable to the first device in the new list
        if self.midi_device_options:
            self.midi_device_var.set(self.midi_device_options[0])
        else:
            self.midi_device_var.set("")
            
    # ------------------------ Loading Callbacks ------------------------
    def reset_to_defaults(self):
        self.update_midi_device_dropdown()
        self.output_text.delete("1.0", tk.END)
        self.config_handler.get_source_midi_properties() # Load default settings
        self.configuration_map = self.config_handler.create_output_dictionary()
        self.update_ui_text_area()
        self.update_ui_from_configuration()

    def update_ui_from_configuration(self):
        # TODO: should i have defaults here or simply check and update of property is found
        # Update MIDI Enabled Checkbox
        midi_enabled_value = self.configuration_map.get('midi.enabled', 'false').lower()
        self.midi_enabled_var.set(midi_enabled_value == 'true')

        # Update MIDI Device Dropdown
        midi_device_value = self.configuration_map.get('midi.interface', '')
        matching_device = next((device for device in self.midi_device_options if midi_device_value in device), None)
        if matching_device:
            self.midi_device_var.set(matching_device)
        else:
            print(f"MIDI Device '{midi_device_value}' not found in available options.")

        # Update Global MIDI Channel Spinbox
        midi_channel_value = self.configuration_map.get('midi.globalChannel', '16')
        try:
            midi_channel_value = int(midi_channel_value)
            if 1 <= midi_channel_value <= 16:
                self.midi_channel_var.set(str(midi_channel_value))
            else:
                print("MIDI Channel value out of range (1-16).")
        except ValueError:
            print("Invalid MIDI Channel value.")

    # ------------------------ Modification Callbacks ------------------------
            
    def update_property(self, key, value):
        # Format the value based on the key
        if key == 'midi.enabled':
            formatted_value = 'true' if value else 'false'
        elif key == 'midi.interface':
            # Strip trailing digits and spaces for display, retain original for internal use
            formatted_value = re.sub(r'\s*\d+$', '', value)
        elif key == 'midi.globalChannel':
            formatted_value = str(value)
        else:
            formatted_value = value  # Default case, no formatting

        # Update the dictionary
        self.configuration_map[key] = formatted_value

        # Update the UI or any other components that depend on this property
        self.update_ui_text_area()

    def update_ui_text_area(self):
        # Clear and repopulate the UI text area with the current configuration
        self.output_text.delete("1.0", tk.END)
        for key, value in self.configuration_map.items():
            self.output_text.insert(tk.END, f"{key}={value}\n")

    def midi_event_action_callback(self, action_key):
        # Handle MIDI action button press for the specific action_key
        print(f"Action triggered for key: {action_key}")
        if self.current_assignment_key is None:
            # Enter assignment mode
            self.current_assignment_key = action_key
            button = self.midi_action_buttons[action_key]
            button.config(text="= Waiting for MIDI input =", bg="red", fg="white")
            button.update()
            # Start listening for MIDI input
            midi_input = self.midi_func.wait_for_midi_input(self.midi_device_var.get())
            if midi_input:
                # Update the configuration with the received MIDI input
                formatted_value = self.midi_func.midi_message_to_string(midi_input)
                key_match = self.is_unique_midi_assignment(formatted_value, action_key)
                if key_match is None:
                    self.update_property(action_key, formatted_value)
                else:
                    messagebox.showwarning("Duplicate MIDI Assignment", 
                                           f"This MIDI configuration is already assigned to another action:{key_match}.")
            # TODO:NOTE alternatively just move the code below outside of the else clause. 
            self.midi_event_action_callback(action_key)  # Recursively call to revert button style
        else:
            # Assignment is done, revert button style and exit assignment mode
            button = self.midi_action_buttons[action_key]
            button.config(text=action_key.replace("midi.Action.", "").replace("_", " ").title(),
                          bg=self.original_button_style["bg"], fg=self.original_button_style["fg"])
            self.current_assignment_key = None

    def is_unique_midi_assignment(self, formatted_value, action_key):
        """Check if the formatted MIDI value is unique across the configuration map."""
        for key, value in self.configuration_map.items():
            try:
                if key != action_key and value.split(',', 1)[1] == formatted_value.split(',', 1)[1]:
                    return key
            except:
                print(f"MIDI message can't be mapped for non-action setting: {key}")
        return None
    
    # ------------------------ Import/Export callbacks Callbacks ------------------------
    def select_file(self):
        """Open a dialog to select a file and update the file_path_entry."""
        file_path = filedialog.askopenfilename(defaultextension=".txt",
                                               filetypes=[("Text Files", "quelea.properties"), ("All Files", "*.*")])
        if file_path:
            self.file_path_entry.delete(0, tk.END)
            self.file_path_entry.insert(0, file_path)

    def import_from_config_file(self):
        """Load configuration from a selected file."""
        file_path = self.file_path_entry.get()
        if not file_path:
            self.select_file()
            file_path = self.file_path_entry.get()

        if not file_path:
            messagebox.showinfo("Import", "No file selected.")
            return

        imported_properties = self.config_handler.import_from_settings_file(file_path, self.configuration_map)
        if imported_properties:
            self.configuration_map.update(imported_properties)
            self.update_ui_from_configuration()
            messagebox.showinfo("Import", "Configuration imported successfully.")
        else:
            messagebox.showerror("Import", "Failed to import any midi configuration.")

    def export_to_config_file(self):
        """Save configuration to a file."""
        file_path = self.file_path_entry.get()
        if not file_path:
            self.select_file()
            file_path = self.file_path_entry.get()

        if not file_path:
            return  # User cancelled the dialog or didn't select a file

        self.config_handler.export_to_settings_file(file_path, self.configuration_map)
        messagebox.showinfo("Export", "Configuration exported successfully.")

def run_app():
    root = tk.Tk()
    source_dir = ".."  # Replace with your actual directory path
    config_handler = ConfigurationFilesHandler(source_dir)
    app = MidiConfiguratorGUI(root, config_handler)
    root.mainloop()

if __name__ == "__main__":
    run_app()