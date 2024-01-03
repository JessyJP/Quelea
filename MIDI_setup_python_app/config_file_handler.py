import os
import re

class ConfigurationFilesHandler:
    def __init__(self, source_directory):
        self.source_directory = source_directory
        self.midi_properties = {}
        self.default_values = {}
        # Set the file paths
        relative_file_path_keys =   "Quelea/src/main/java/org/quelea/services/utils/QueleaPropertyKeys.java"
        self.file_path_keys = os.path.join(self.source_directory, relative_file_path_keys)
        relative_file_path_values = "Quelea/src/main/java/org/quelea/services/utils/QueleaProperties.java"
        self.file_path_values =  os.path.join(self.source_directory, relative_file_path_values)

        self.template_filepath =  os.path.join(self.source_directory, "MIDI_setup_python_app/property_template.txt")

    # Source methods
    def get_source_midi_properties(self):
        self.extract_midi_properties()
        self.extract_default_values()
        # self.verify_getter_setter_methods()

        return self.midi_properties, self.default_values

    def extract_midi_properties(self):
        with open(self.file_path_keys, 'r') as file:
            content = file.read()

        lines = content.split('\n')
        for line in lines:
            if 'midi.' in line:
                parts = line.split('=')
                key = parts[0].strip().split(' ')[-1]
                value = parts[1].split(';')[0].strip().strip('"')
                self.midi_properties[key] = value
        
        return self.midi_properties

    def extract_default_values(self):
        with open(self.file_path_values, 'r') as file:
            content = file.read()

        if len(self.midi_properties) == 0:
            self.extract_midi_properties()

        for key in self.midi_properties.keys():
            # Generalized regex to find the default value in the getter method
            pattern = fr"public\s+[^\n]*\s+return\s+[^\n]*getProperty\s*\(\s*{key}\s*,\s*\"([^\"]+)\"\s*\)"
            match = re.search(pattern, content)
            if match:
                self.default_values[key] = match.group(1)

        return self.default_values

    def verify_getter_setter_methods(self):
        with open(self.file_path_values, 'r') as file:
            content = file.read()

        missing_methods = []
        for key in self.midi_properties.keys():
            # Generalized regex for getter and setter methods
            getter_pattern = fr"public\s+(?:boolean|string|int)\s+get{key}\(\)\s*\{{.*?\}}"
            setter_pattern = fr"public\s+void\s+set{key}\s*\(.*?\)\s*\{{.*?\}}"
            if not re.search(getter_pattern, content, re.DOTALL) or not re.search(setter_pattern, content, re.DOTALL):
                missing_methods.append(key)
        if missing_methods:
            raise Exception(f"Missing getter/setter methods for keys: {missing_methods}")

    def create_output_dictionary(self):
        """Creates a consolidated dictionary of MIDI properties and their default values."""
        output_dict = {}
        for key in self.midi_properties.keys():
            default_value = self.default_values.get(key, " === No default value ===")
            output_dict[self.midi_properties[key]] = default_value
        return output_dict

    # Template methods
    def export_to_file(self, export_path):
        """Exports MIDI properties and default values to a specified file."""
        with open(export_path, 'w') as file:
            for key, value in self.midi_properties.items():
                default_value = self.default_values.get(key, " === No default value ===")
                file.write(f"{key}={default_value}\n")

    ## ------------------------------------------------------------------------
    # Import settings from Quelea config file
    def import_from_settings_file(self, import_path, configuration_map):
        """Imports MIDI properties from a specified file and updates the given configuration map."""
        imported_properties = {}
        with open(import_path, 'r') as file:
            for line in file:
                # Skip lines that don't contain the '=' sign
                if '=' not in line:
                    continue

                key, value = line.strip().split('=', 1)
                if key in configuration_map:
                    imported_properties[key] = value
        return imported_properties
    
    # Export to settings to Quelea config file
    def export_to_settings_file(self, settings_path, configuration_map):
        """Updates an existing settings file with MIDI properties."""
        if not os.path.exists(settings_path):
            with open(settings_path, 'w') as file:
                for key, value in configuration_map.items():
                    file.write(f"{key}={value}\n")
            return

        # Read and update the file content
        updated_lines = []
        found_keys = set()
        with open(settings_path, 'r') as file:
            for line in file:
                if '=' in line:
                    key, existing_value = line.strip().split('=', 1)
                    found_keys.add(key)
                    if key in configuration_map:
                        updated_lines.append(f"{key}={configuration_map[key]}\n")
                    else:
                        updated_lines.append(line)
                else:
                    updated_lines.append(line)

        # Append any configurations not already included
        for key, value in configuration_map.items():
            if key not in found_keys:
                updated_lines.append(f"{key}={value}\n")

        # Write the updated content back to the file
        with open(settings_path, 'w') as file:
            file.writelines(updated_lines)

# Example Usage
if __name__ == "__main__":
    source_dir = ".."  # Replace with your actual directory path
    midi_handler = ConfigurationFilesHandler(source_dir)
    midi_properties, default_values = midi_handler.get_source_midi_properties()
    print("MIDI Properties:", midi_properties)
    print("\n\nDefault Values:", default_values)

    midi_handler.export_to_file(midi_handler.template_filepath)