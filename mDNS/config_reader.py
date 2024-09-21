import os
from mdns_core import ServiceConfig, get_local_ip  # Ensure the ServiceConfig class is imported correctly

# =====================================================
#             Configuration Reader Function
# =====================================================


# Function to read the configuration file and extract the required ports
def read_configurations():
    # Construct the path to the properties file in the user's directory
    config_path = os.path.expanduser("~/.quelea/quelea.properties")
    configurations = []  # Use a list to store ServiceConfig objects

    # Keys we are interested in
    target_keys = {
        'remote.control.port': 'remote'+'q',
        'mob.lyrics.port': 'lyrics'+'q'
    }

    try:
        with open(config_path, 'r') as file:
            for line in file:
                # Split the line into key and value
                key_value = line.strip().split('=')
                if len(key_value) == 2:
                    key, value = key_value
                    # Check if the key is one of the target keys
                    if key in target_keys:
                        # Create a ServiceConfig instance with the extracted data and add it to the list
                        configurations.append(ServiceConfig(
                            name=target_keys[key],
                            port=int(value),
                            ip=get_local_ip(),  # Default IP can be dynamically set as needed
                            url=f"http://{target_keys[key]}.local:{value}"
                        ))

    except FileNotFoundError:
        print(f"Configuration file not found at {config_path}.")
    except ValueError:
        print("Error reading the configuration file. Check the format of the properties file.")

    # Returning the extracted configurations as a list of ServiceConfig instances
    return configurations
