import os
import json

base_rp = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"

# 1. Legacy models/item/fishing_rod.json
legacy_model = {
    "parent": "minecraft:item/handheld_rod",
    "textures": {
        "layer0": "minecraft:item/fishing_rod"
    },
    "overrides": [
        { "predicate": { "cast": 1 }, "model": "minecraft:item/fishing_rod_cast" },
        
        { "predicate": { "custom_model_data": 1000 }, "model": "aquafishing:item/rod_acemi" },
        { "predicate": { "custom_model_data": 1000, "cast": 1 }, "model": "aquafishing:item/rod_acemi_cast" },
        
        { "predicate": { "custom_model_data": 2000 }, "model": "aquafishing:item/rod_demir" },
        { "predicate": { "custom_model_data": 2000, "cast": 1 }, "model": "aquafishing:item/rod_demir_cast" },
        
        { "predicate": { "custom_model_data": 3000 }, "model": "aquafishing:item/rod_altin" },
        { "predicate": { "custom_model_data": 3000, "cast": 1 }, "model": "aquafishing:item/rod_altin_cast" },
        
        { "predicate": { "custom_model_data": 4000 }, "model": "aquafishing:item/rod_elmas" },
        { "predicate": { "custom_model_data": 4000, "cast": 1 }, "model": "aquafishing:item/rod_elmas_cast" },
        
        { "predicate": { "custom_model_data": 5000 }, "model": "aquafishing:item/rod_melon_kadim" },
        { "predicate": { "custom_model_data": 5000, "cast": 1 }, "model": "aquafishing:item/rod_melon_kadim_cast" }
    ]
}

legacy_path = os.path.join(base_rp, "assets", "minecraft", "models", "item", "fishing_rod.json")
with open(legacy_path, "w", encoding="utf-8") as f:
    json.dump(legacy_model, f, indent=2)
print("Updated legacy models/item/fishing_rod.json")

# 2. Modern 1.21.4+ items/fishing_rod.json
items_dir = os.path.join(base_rp, "assets", "minecraft", "items")
os.makedirs(items_dir, exist_ok=True)

modern_item = {
    "model": {
        "type": "minecraft:condition",
        "property": "minecraft:fishing_rod/cast",
        "on_true": {
            "type": "minecraft:range_dispatch",
            "property": "minecraft:custom_model_data",
            "fallback": {
                "type": "minecraft:model",
                "model": "minecraft:item/fishing_rod_cast"
            },
            "entries": [
                { "threshold": 1000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_acemi_cast" } },
                { "threshold": 2000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_demir_cast" } },
                { "threshold": 3000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_altin_cast" } },
                { "threshold": 4000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_elmas_cast" } },
                { "threshold": 5000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_melon_kadim_cast" } }
            ]
        },
        "on_false": {
            "type": "minecraft:range_dispatch",
            "property": "minecraft:custom_model_data",
            "fallback": {
                "type": "minecraft:model",
                "model": "minecraft:item/fishing_rod"
            },
            "entries": [
                { "threshold": 1000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_acemi" } },
                { "threshold": 2000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_demir" } },
                { "threshold": 3000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_altin" } },
                { "threshold": 4000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_elmas" } },
                { "threshold": 5000, "model": { "type": "minecraft:model", "model": "aquafishing:item/rod_melon_kadim" } }
            ]
        }
    }
}

modern_path = os.path.join(items_dir, "fishing_rod.json")
with open(modern_path, "w", encoding="utf-8") as f:
    json.dump(modern_item, f, indent=2)
print("Updated modern items/fishing_rod.json")
