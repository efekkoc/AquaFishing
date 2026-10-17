import os, shutil, json, zipfile
from PIL import Image

# 1. Fish mapping (1 to 40 in exact order)
fish_ids = [
    "morina", "somon", "balon_baligi", "tropikal_balik", "yayin_baligi",
    "turna_baligi", "sardalya", "koi_baligi", "alabalik", "pilic_baligi",
    "kilic_baligi", "pirana", "fener_baligi", "yilan_baligi", "deniz_ati",
    "vatoz", "altin_sazan", "lav_baligi", "ejder_balik", "cerrah_baligi",
    "kaplan_baligi", "isik_baligi", "aksulot", "kopek_baligi", "aslan_baligi",
    "orkinos", "iskender_baligi", "ucan_balik", "zirhli_balik", "dev_murekkep",
    "buz_baligi", "yildiz_baligi", "balgam_baligi", "hayalet_balik", "kutsal_balik",
    "devasa_balik", "palyaco_baligi", "deniz_anasi", "kalkan_baligi", "merlin"
]

rod_ids = [
    "cirak", "bambu", "demir", "kiziltas", "altin",
    "ametist", "prizmarin", "elmas", "netherite", "melon_kadim"
]

# Source textures dir:
src_tex_dir = r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack/assets/aquafishing/textures/item'

# Target folders to update:
pack_roots = [
    r'C:/Users/Furkan Koç/AppData/Roaming/.minecraft/resourcepacks/AquaFishing-Pack',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/resourcepack/AquaFishing-Pack'
]

# Copy fish textures with BOTH names: fish_<id>.png AND fish_<number>.png
for root in pack_roots:
    tex_dir = os.path.join(root, 'assets', 'aquafishing', 'textures', 'item')
    models_dir = os.path.join(root, 'assets', 'aquafishing', 'models', 'item')
    items_dir = os.path.join(root, 'assets', 'aquafishing', 'items')
    mc_models = os.path.join(root, 'assets', 'minecraft', 'models', 'item')
    os.makedirs(tex_dir, exist_ok=True)
    os.makedirs(models_dir, exist_ok=True)
    os.makedirs(items_dir, exist_ok=True)
    os.makedirs(mc_models, exist_ok=True)

    # FISH:
    cod_overrides = []
    for idx, fid in enumerate(fish_ids, 1):
        num_tex = os.path.join(src_tex_dir, f'fish_{idx}.png')
        if os.path.exists(num_tex):
            target_id = os.path.join(tex_dir, f'fish_{fid}.png')
            target_num = os.path.join(tex_dir, f'fish_{idx}.png')
            if os.path.abspath(num_tex) != os.path.abspath(target_id):
                shutil.copy2(num_tex, target_id)
            if os.path.abspath(num_tex) != os.path.abspath(target_num):
                shutil.copy2(num_tex, target_num)

        # model json for fish_<fid> and fish_<idx>
        for name in [f'fish_{fid}', f'fish_{idx}']:
            model_content = {
                "parent": "minecraft:item/generated",
                "textures": {
                    "layer0": f"aquafishing:item/{name}"
                }
            }
            with open(os.path.join(models_dir, f'{name}.json'), 'w') as f:
                json.dump(model_content, f, indent=2)

            item_content = {
                "model": {
                    "type": "minecraft:model",
                    "model": f"aquafishing:item/{name}"
                }
            }
            with open(os.path.join(items_dir, f'{name}.json'), 'w') as f:
                json.dump(item_content, f, indent=2)

        # cod override
        cod_overrides.append({
            "predicate": {"custom_model_data": 10000 + idx},
            "model": f"aquafishing:item/fish_{fid}"
        })

    with open(os.path.join(mc_models, 'cod.json'), 'w') as f:
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "minecraft:item/cod"}, "overrides": cod_overrides}, f, indent=2)

    # RODS:
    rod_overrides = []
    rod_overrides.append({
        "predicate": {"cast": 1},
        "model": "minecraft:item/fishing_rod_cast"
    })

    for idx, rid in enumerate(rod_ids, 1):
        num_tex = os.path.join(src_tex_dir, f'rod_{idx}.png')
        if os.path.exists(num_tex):
            target_id = os.path.join(tex_dir, f'rod_{rid}.png')
            target_num = os.path.join(tex_dir, f'rod_{idx}.png')
            if os.path.abspath(num_tex) != os.path.abspath(target_id):
                shutil.copy2(num_tex, target_id)
            if os.path.abspath(num_tex) != os.path.abspath(target_num):
                shutil.copy2(num_tex, target_num)

        for name in [f'rod_{rid}', f'rod_{idx}']:
            model_content = {
                "parent": "minecraft:item/handheld_rod",
                "textures": {
                    "layer0": f"aquafishing:item/{name}"
                }
            }
            with open(os.path.join(models_dir, f'{name}.json'), 'w') as f:
                json.dump(model_content, f, indent=2)

            item_content = {
                "model": {
                    "type": "minecraft:model",
                    "model": f"aquafishing:item/{name}"
                }
            }
            with open(os.path.join(items_dir, f'{name}.json'), 'w') as f:
                json.dump(item_content, f, indent=2)

        # CMD overrides (both uncast and cast)
        cmd = idx * 1000
        rod_overrides.append({
            "predicate": {"custom_model_data": cmd},
            "model": f"aquafishing:item/rod_{rid}"
        })
        rod_overrides.append({
            "predicate": {"custom_model_data": cmd, "cast": 1},
            "model": f"aquafishing:item/rod_{rid}"
        })

    with open(os.path.join(mc_models, 'fishing_rod.json'), 'w') as f:
        json.dump({"parent": "minecraft:item/handheld_rod", "textures": {"layer0": "minecraft:item/fishing_rod"}, "overrides": rod_overrides}, f, indent=2)

    # Ensure pack.mcmeta exists
    mcmeta_path = os.path.join(root, 'pack.mcmeta')
    if not os.path.exists(mcmeta_path):
        mcmeta = {
            "pack": {
                "pack_format": 46,
                "description": "AquaFishing Ozel Oltalar ve Baliklar"
            }
        }
        with open(mcmeta_path, 'w') as f:
            json.dump(mcmeta, f, indent=2)

# Repack zips
zip_targets = [
    r'C:/Users/Furkan Koç/AppData/Roaming/.minecraft/resourcepacks/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack.zip',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/plugins/AquaFishing/AquaFishing-Pack.zip'
]

source_pack = pack_roots[1]
for target in zip_targets:
    os.makedirs(os.path.dirname(target), exist_ok=True)
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for r, dirs, files in os.walk(source_pack):
            for file in files:
                fp = os.path.join(r, file)
                rp = os.path.relpath(fp, source_pack)
                zipf.write(fp, rp)
    print(f"Repacked {target} ({os.path.getsize(target)} bytes)")

# 2. Overwrite test-server plugin config with new 10 rods
src_cfg = r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/config.yml'
dst_cfg = r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/plugins/AquaFishing/config.yml'
shutil.copy2(src_cfg, dst_cfg)
print("Updated test-server/plugins/AquaFishing/config.yml!")
