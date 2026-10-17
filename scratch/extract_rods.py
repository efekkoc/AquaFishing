import os, json
from PIL import Image
import numpy as np

img_path = r'C:/Users/Furkan Koç/.gemini/antigravity/brain/38292d9e-3c51-45fc-8a3c-93a40d9baaa6/.user_uploaded/media_1792248611636_b85860bd.png'

dest_dirs = [
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/resourcepack/AquaFishing-Pack',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack'
]

im = Image.open(img_path).convert('RGBA')
arr = np.array(im)
w, h = im.size
col_w = w / 5.0

# 2 rows of 5 rods:
# Row 0: y in [10, 200]
# Row 1: y in [295, 495]
row_specs = [(10, 200), (295, 495)]

rod_idx = 1
for r_idx, (y1, y2) in enumerate(row_specs):
    for c_idx in range(5):
        x1 = int(c_idx * col_w)
        x2 = int((c_idx + 1) * col_w)

        cell = arr[y1:y2, x1:x2].copy()

        # Foreground mask (R<242 or G<242 or B<242)
        is_fg = (cell[:, :, 0] < 242) | (cell[:, :, 1] < 242) | (cell[:, :, 2] < 242)
        coords = np.argwhere(is_fg)
        if len(coords) == 0:
            print(f"Error: Rod #{rod_idx} not found!")
            rod_idx += 1
            continue

        min_y, min_x = coords.min(axis=0)
        max_y, max_x = coords.max(axis=0)

        crop = cell[min_y:max_y+1, min_x:max_x+1].copy()

        # Background transparent
        bg_mask = (crop[:, :, 0] >= 242) & (crop[:, :, 1] >= 242) & (crop[:, :, 2] >= 242)
        crop[bg_mask, 3] = 0

        sprite = Image.fromarray(crop)

        # Pad to square
        max_dim = max(sprite.width, sprite.height)
        sq_size = max_dim + 4
        square_img = Image.new('RGBA', (sq_size, sq_size), (0, 0, 0, 0))
        offset_x = (sq_size - sprite.width) // 2
        offset_y = (sq_size - sprite.height) // 2
        square_img.paste(sprite, (offset_x, offset_y), sprite)

        final_sprite = square_img.resize((32, 32), Image.Resampling.NEAREST)

        for base_dir in dest_dirs:
            tex_dir = os.path.join(base_dir, 'assets', 'aquafishing', 'textures', 'item')
            os.makedirs(tex_dir, exist_ok=True)
            final_sprite.save(os.path.join(tex_dir, f'rod_{rod_idx}.png'))

        print(f"Saved rod_{rod_idx}.png (original {sprite.width}x{sprite.height} -> 32x32)")
        rod_idx += 1

# Now create models and overrides
for base_dir in dest_dirs:
    # 1. Model files for each rod
    models_dir = os.path.join(base_dir, 'assets', 'aquafishing', 'models', 'item')
    os.makedirs(models_dir, exist_ok=True)
    for i in range(1, 11):
        rod_model = {
            "parent": "minecraft:item/handheld_rod",
            "textures": {
                "layer0": f"aquafishing:item/rod_{i}"
            }
        }
        with open(os.path.join(models_dir, f'rod_{i}.json'), 'w', encoding='utf-8') as f:
            json.dump(rod_model, f, indent=2)

    # 2. Vanilla fishing_rod.json overrides
    mc_models_dir = os.path.join(base_dir, 'assets', 'minecraft', 'models', 'item')
    os.makedirs(mc_models_dir, exist_ok=True)
    
    overrides = []
    for i in range(1, 11):
        cmd = i * 1000
        overrides.append({
            "predicate": {
                "custom_model_data": cmd
            },
            "model": f"aquafishing:item/rod_{i}"
        })

    fishing_rod_json = {
        "parent": "minecraft:item/handheld_rod",
        "textures": {
            "layer0": "minecraft:item/fishing_rod"
        },
        "overrides": overrides
    }
    with open(os.path.join(mc_models_dir, 'fishing_rod.json'), 'w', encoding='utf-8') as f:
        json.dump(fishing_rod_json, f, indent=2)

print("All 10 rods cropped, textures and model JSONs created successfully!")
