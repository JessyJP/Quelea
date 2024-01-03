import tkinter as tk
from config_file_handler import ConfigurationFilesHandler
from mido_midi_device_handler import MidiFunctionality
from midi_configurator_gui import MidiConfiguratorGUI

def run_app():
    root = tk.Tk()
    source_dir = ".."  # Replace with your actual directory path
    config_handler = ConfigurationFilesHandler(source_dir)
    app = MidiConfiguratorGUI(root, config_handler)
    root.mainloop()

if __name__ == "__main__":
    run_app()
