import shutil
import os

src_dir = r"C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\extracted_rods"
dst_tex = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack\assets\aquafishing\textures\item"
os.makedirs(dst_tex, exist_ok=True)

mappings = [
    ("rod_acemi", ["rod_acemi", "rod_wooden"]),
    ("rod_demir", ["rod_demir", "rod_iron"]),
    ("rod_altin", ["rod_altin", "rod_gold"]),
    ("rod_elmas", ["rod_elmas", "rod_diamond"]),
    ("rod_melon_kadim", ["rod_melon_kadim"])
]

for src_name, aliases in mappings:
    for suffix in ["", "_cast"]:
        src_file = os.path.join(src_dir, f"{src_name}{suffix}.png")
        for alias in aliases:
            dst_file = os.path.join(dst_tex, f"{alias}{suffix}.png")
            shutil.copy2(src_file, dst_file)
            print(f"Copied {src_name}{suffix}.png -> {alias}{suffix}.png")

dst_models = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack\assets\aquafishing\models\item"
os.makedirs(dst_models, exist_ok=True)

all_model_names = ["rod_acemi", "rod_wooden", "rod_demir", "rod_iron", "rod_altin", "rod_gold", "rod_elmas", "rod_diamond", "rod_melon_kadim"]
for name in all_model_names:
    for suffix in ["", "_cast"]:
        model_path = os.path.join(dst_models, f"{name}{suffix}.json")
        tex_ref = f"aquafishing:item/{name}{suffix}"
        with open(model_path, "w", encoding="utf-8") as f:
            f.write(f'{{\n  "parent": "minecraft:item/handheld_rod",\n  "textures": {{\n    "layer0": "{tex_ref}"\n  }}\n}}\n')
        print(f"Created model: {name}{suffix}.json")
