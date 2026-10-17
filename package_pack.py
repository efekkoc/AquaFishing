import shutil
import os
import re

rp_dir = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
zip_path = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"

# Make zip archive
shutil.make_archive(zip_path, 'zip', rp_dir)
print(f"Created {zip_path}.zip")

# Update options.txt to enable the resource pack if not present
options_file = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\options.txt"
if os.path.exists(options_file):
    with open(options_file, "r", encoding="utf-8") as f:
        content = f.read()

    # Pattern for resourcePacks:[...]
    # We want ["vanilla", "file/AquaFishing-Pack.zip"]
    if 'file/AquaFishing-Pack.zip' not in content:
        new_content = re.sub(
            r'resourcePacks:\[(.*?)\]',
            r'resourcePacks:[\1, "file/AquaFishing-Pack.zip"]' if r'\1' else r'resourcePacks:["vanilla", "file/AquaFishing-Pack.zip"]',
            content
        )
        # Clean up any leading comma
        new_content = new_content.replace('[, ', '[').replace('["", ', '[')
        with open(options_file, "w", encoding="utf-8") as f:
            f.write(new_content)
        print("Updated options.txt to automatically enable AquaFishing-Pack.zip")
    else:
        print("AquaFishing-Pack.zip already in options.txt")
