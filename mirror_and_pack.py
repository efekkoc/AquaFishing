import shutil
import os
import json

base = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"

# Copy models & textures to minecraft namespace as well for maximum fallback compatibility
src_models = os.path.join(base, "assets", "aquafishing", "models", "item")
src_textures = os.path.join(base, "assets", "aquafishing", "textures", "item")

dst_models = os.path.join(base, "assets", "minecraft", "models", "item")
dst_textures = os.path.join(base, "assets", "minecraft", "textures", "item")

os.makedirs(dst_models, exist_ok=True)
os.makedirs(dst_textures, exist_ok=True)

for f in os.listdir(src_textures):
    shutil.copy2(os.path.join(src_textures, f), os.path.join(dst_textures, f))

for f in os.listdir(src_models):
    shutil.copy2(os.path.join(src_models, f), os.path.join(dst_models, f))

print("Mirrored textures and models to minecraft namespace")

# Re-zip pack
zip_dest = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
shutil.make_archive(zip_dest, 'zip', base)
print(f"Re-packaged {zip_dest}.zip")
