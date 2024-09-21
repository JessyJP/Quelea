import argparse
import time
from mdns_core import initialize_services  # Import the initialization function from mdns_core
from config_reader import read_configurations  # Import the configuration reader
from gui import start_service_gui  # Import the GUI launcher


# =====================================================
#               Helper Method to Get Services
# =====================================================

# Function to read configurations and initialize services
def get_services():
    # Read configurations
    configurations = read_configurations()

    # Check if configurations were read successfully
    if not configurations:
        print("No configurations found. Exiting.")
        return None, None

    # Initialize services
    zeroconf_services = initialize_services(configurations)
    return zeroconf_services, configurations


# =====================================================
#                 Main Application Loop
# =====================================================

# Function to keep the application running (terminal version)
def run_terminal_loop():
    # Get services and configurations
    zeroconf_services, configurations = get_services()

    # Check if services were initialized successfully
    if zeroconf_services is None:
        return

    print("Terminal loop started. Press Ctrl+C to stop.")
    try:
        while True:
            time.sleep(1)  # Sleep for 1 second to keep the loop running without consuming CPU
    except KeyboardInterrupt:
        print("Stopping terminal loop...")
    finally:
        # Clean up services after the loop ends
        for zeroconf, service_info in zeroconf_services:
            zeroconf.unregister_service(service_info)
            zeroconf.close()
        print("All services stopped.")


# Function to run the GUI loop
def run_gui_loop():
    # Get services and configurations
    zeroconf_services, configurations = get_services()

    # Check if services were initialized successfully
    if zeroconf_services is None:
        return

    print("Launching GUI version of the application...")
    start_service_gui(configurations)

    # Clean up services after the GUI is closed
    for zeroconf, service_info in zeroconf_services:
        zeroconf.unregister_service(service_info)
        zeroconf.close()
    print("All services stopped.")


# =====================================================
#                       Main Method
# =====================================================

# Main method with argument parsing and loop selection
def main():
    # Create the argument parser
    parser = argparse.ArgumentParser(description="Run the mDNS application.")
    parser.add_argument('--gui', action='store_true', help="Run the application with a GUI.")
    args = parser.parse_args()

    # Determine which loop to run based on the --gui flag
    if args.gui:
        run_gui_loop()
    else:
        run_terminal_loop()


if __name__ == "__main__":
    main()
