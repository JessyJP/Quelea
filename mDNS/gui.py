import tkinter as tk
from tkinter import messagebox
import webbrowser


# =====================================================
#                  GUI Application
# =====================================================

class ServiceGUI:
    def __init__(self, services):
        # Initialize the main window
        self.root = tk.Tk()
        self.root.title("Service Dashboard")
        self.services = services

        # Create the GUI layout
        self.create_service_buttons()

    def create_service_buttons(self):
        # Create a button for each service
        for config in self.services:
            self.add_service_button(config)

    def add_service_button(self, config):
        # Button text with service URL and IP
        button_text = f"{config.url}\n{config.ip}"
        # Create a button for each service to open its URL
        button = tk.Button(
            self.root,
            text=button_text,
            command=lambda: self.open_service_url(config.url)
        )
        button.pack(pady=10, padx=10, fill=tk.X)

        # Add extra button to open the service info URL directly
        extra_button = tk.Button(
            self.root,
            text=f"Open {config.name} in Browser",
            command=lambda: self.open_service_url(config.url)
        )
        extra_button.pack(pady=2, padx=10, fill=tk.X)

    def open_service_url(self, url):
        # Open the service URL in the default web browser
        try:
            webbrowser.open(url)
        except Exception as e:
            messagebox.showerror("Error", f"Failed to open URL: {e}")

    def run(self):
        # Start the Tkinter main loop
        self.root.mainloop()


# =====================================================
#                  Run the GUI
# =====================================================

def start_service_gui(services):
    app = ServiceGUI(services)
    app.run()
