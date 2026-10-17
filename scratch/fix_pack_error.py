import os, json, zipfile, uuid

# 1. Clean pack.mcmeta
clean_mcmeta = {
    "pack": {
        "pack_format": 46,
        "description": "AquaFishing Ozel Oltalar ve Baliklar"
    }
}

pack_dirs = [
    r'C:/Users/Furkan Koç/AppData/Roaming/.minecraft/resourcepacks/AquaFishing-Pack',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/resourcepack/AquaFishing-Pack'
]

for p in pack_dirs:
    meta_file = os.path.join(p, 'pack.mcmeta')
    with open(meta_file, 'w', encoding='utf-8') as f:
        json.dump(clean_mcmeta, f, indent=2)
    print(f"Updated clean pack.mcmeta in {meta_file}")

# 2. Repack zips
zip_targets = [
    r'C:/Users/Furkan Koç/AppData/Roaming/.minecraft/resourcepacks/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/plugins/AquaFishing/AquaFishing-Pack.zip'
]

src_folder = pack_dirs[0]
for target in zip_targets:
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for r, dirs, files in os.walk(src_folder):
            for file in files:
                fp = os.path.join(r, file)
                rp = os.path.relpath(fp, src_folder)
                zipf.write(fp, rp)
    print(f"Repacked {target} ({os.path.getsize(target)} bytes)")

# 3. Enable pack in options.txt
options_file = r'C:/Users/Furkan Koç/AppData/Roaming/.minecraft/options.txt'
if os.path.exists(options_file):
    with open(options_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    new_lines = []
    for line in lines:
        if line.startswith('resourcePacks:'):
            # Ensure "file/AquaFishing-Pack" is in resourcePacks
            new_lines.append('resourcePacks:["vanilla","file/AquaFishing-Pack"]\n')
        elif line.startswith('incompatibleResourcePacks:'):
            new_lines.append('incompatibleResourcePacks:[]\n')
        else:
            new_lines.append(line)
            
    with open(options_file, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
    print("Enabled file/AquaFishing-Pack in options.txt!")

# 4. Invalidate server.properties resource pack cache
server_props = r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/server.properties'
if os.path.exists(server_props):
    with open(server_props, 'r', encoding='utf-8') as f:
        sp_lines = f.readlines()
    new_sp = []
    for line in sp_lines:
        if line.startswith('resource-pack-id='):
            new_sp.append(f'resource-pack-id={uuid.uuid4()}\n')
        else:
            new_sp.append(line)
    with open(server_props, 'w', encoding='utf-8') as f:
        f.writelines(new_sp)
    print("Updated resource-pack-id in server.properties!")
