import shutil
import os

base_rp = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
zip_dest = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"

# Make zip
shutil.make_archive(zip_dest, 'zip', base_rp)
zip_file = zip_dest + ".zip"
print(f"Created {zip_file}")

# Copy to plugin resources and test-server
res_dest = r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\src\main\resources\AquaFishing-Pack.zip"
shutil.copy2(zip_file, res_dest)
print(f"Copied to {res_dest}")

server_dest = r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\test-server\plugins\AquaFishing\AquaFishing-Pack.zip"
os.makedirs(os.path.dirname(server_dest), exist_ok=True)
shutil.copy2(zip_file, server_dest)
print(f"Copied to {server_dest}")
