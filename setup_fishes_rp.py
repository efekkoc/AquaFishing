import os
import shutil
import json

base_rp = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
extracted_dir = r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\extracted_fishes"

fishes = [
    # Row 1
    ("morina", 10001),
    ("somon", 10002),
    ("balon_baligi", 10003),
    ("tropikal_balik", 10004),
    ("yayin_baligi", 10005),
    ("turna_baligi", 10006),
    ("sardalya", 10007),
    ("koi_baligi", 10008),
    # Row 2
    ("alabalik", 10009),
    ("pilic_baligi", 10010),
    ("kilic_baligi", 10011),
    ("pirana", 10012),
    ("fener_baligi", 10013),
    ("yilan_baligi", 10014),
    ("deniz_ati", 10015),
    ("vatoz", 10016),
    # Row 3
    ("altin_sazan", 10017),
    ("lav_baligi", 10018),
    ("ejder_balik", 10019),
    ("cerrah_baligi", 10020),
    ("kaplan_baligi", 10021),
    ("isik_baligi", 10022),
    ("aksulot", 10023),
    ("kopek_baligi", 10024),
    # Row 4
    ("aslan_baligi", 10025),
    ("orkinos", 10026),
    ("iskender_baligi", 10027),
    ("ucan_balik", 10028),
    ("zirhli_balik", 10029),
    ("dev_murekkep", 10030),
    ("buz_baligi", 10031),
    ("yildiz_baligi", 10032),
    # Row 5
    ("balgam_baligi", 10033),
    ("hayalet_balik", 10034),
    ("kutsal_balik", 10035),
    ("devasa_balik", 10036),
    ("palyaco_baligi", 10037),
    ("deniz_anasi", 10038),
    ("kalkan_baligi", 10039),
    ("merlin", 10040)
]

aqua_tex = os.path.join(base_rp, "assets", "aquafishing", "textures", "item")
aqua_models = os.path.join(base_rp, "assets", "aquafishing", "models", "item")
mc_tex = os.path.join(base_rp, "assets", "minecraft", "textures", "item")
mc_models = os.path.join(base_rp, "assets", "minecraft", "models", "item")

os.makedirs(aqua_tex, exist_ok=True)
os.makedirs(aqua_models, exist_ok=True)
os.makedirs(mc_tex, exist_ok=True)
os.makedirs(mc_models, exist_ok=True)

for fish_id, cmd in fishes:
    src_png = os.path.join(extracted_dir, f"{fish_id}.png")
    dst_png1 = os.path.join(aqua_tex, f"fish_{fish_id}.png")
    dst_png2 = os.path.join(mc_tex, f"fish_{fish_id}.png")
    
    shutil.copy2(src_png, dst_png1)
    shutil.copy2(src_png, dst_png2)
    
    # Model JSON
    model_content = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"aquafishing:item/fish_{fish_id}"
        }
    }
    
    with open(os.path.join(aqua_models, f"fish_{fish_id}.json"), "w", encoding="utf-8") as f:
        json.dump(model_content, f, indent=2)
    with open(os.path.join(mc_models, f"fish_{fish_id}.json"), "w", encoding="utf-8") as f:
        json.dump(model_content, f, indent=2)

print(f"Set up 40 fish textures and models!")

# Create / Update assets/minecraft/models/item/cod.json
cod_legacy = {
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "minecraft:item/cod"
    },
    "overrides": []
}

for fish_id, cmd in fishes:
    cod_legacy["overrides"].append({
        "predicate": { "custom_model_data": cmd },
        "model": f"aquafishing:item/fish_{fish_id}"
    })

with open(os.path.join(mc_models, "cod.json"), "w", encoding="utf-8") as f:
    json.dump(cod_legacy, f, indent=2)

# Create / Update assets/minecraft/items/cod.json (1.21.4+)
items_dir = os.path.join(base_rp, "assets", "minecraft", "items")
os.makedirs(items_dir, exist_ok=True)

cod_modern = {
    "model": {
        "type": "minecraft:range_dispatch",
        "property": "minecraft:custom_model_data",
        "fallback": {
            "type": "minecraft:model",
            "model": "minecraft:item/cod"
        },
        "entries": []
    }
}

for fish_id, cmd in fishes:
    cod_modern["model"]["entries"].append({
        "threshold": cmd,
        "model": {
            "type": "minecraft:model",
            "model": f"aquafishing:item/fish_{fish_id}"
        }
    })

with open(os.path.join(items_dir, "cod.json"), "w", encoding="utf-8") as f:
    json.dump(cod_modern, f, indent=2)

print("Updated cod.json model and item definition for all 40 fish!")
