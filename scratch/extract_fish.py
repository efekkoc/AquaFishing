import os
from PIL import Image
import numpy as np

img_paths = [
    r'C:/Users/Furkan Koç/.gemini/antigravity/brain/38292d9e-3c51-45fc-8a3c-93a40d9baaa6/.user_uploaded/media_1792247954767_fc116c64.png',
    r'C:/Users/Furkan Koç/.gemini/antigravity/brain/38292d9e-3c51-45fc-8a3c-93a40d9baaa6/.user_uploaded/media_1792247961909_c37e92fd.png',
    r'C:/Users/Furkan Koç/.gemini/antigravity/brain/38292d9e-3c51-45fc-8a3c-93a40d9baaa6/.user_uploaded/media_1792247967656_534997d4.png',
    r'C:/Users/Furkan Koç/.gemini/antigravity/brain/38292d9e-3c51-45fc-8a3c-93a40d9baaa6/.user_uploaded/media_1792247975085_6356d4f3.png'
]

# Destination folders:
dest_dirs = [
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/src/main/resources/resourcepack/AquaFishing-Pack/assets/aquafishing/textures/item',
    r'C:/Users/Furkan Koç/.gemini/antigravity/scratch/AquaFishing/test-server/resourcepack/AquaFishing-Pack/assets/aquafishing/textures/item'
]

for d in dest_dirs:
    os.makedirs(d, exist_ok=True)

fish_idx = 1
for img_i, p in enumerate(img_paths):
    im = Image.open(p).convert('RGBA')
    arr = np.array(im)
    w, h = im.size
    col_w = w / 5.0

    # Row 1 fish y-range: [20, 205] (text is >= 215)
    # Row 2 fish y-range: [280, 460] (text is >= 470)
    row_specs = [(20, 205), (280, 460)]

    for r_idx, (y1, y2) in enumerate(row_specs):
        for c_idx in range(5):
            x1 = int(c_idx * col_w)
            x2 = int((c_idx + 1) * col_w)

            cell = arr[y1:y2, x1:x2].copy()

            # Background is white/near-white (R>245, G>245, B>245)
            # Find foreground pixels
            is_fg = (cell[:, :, 0] < 242) | (cell[:, :, 1] < 242) | (cell[:, :, 2] < 242)

            coords = np.argwhere(is_fg)
            if len(coords) == 0:
                print(f"Warning: Fish #{fish_idx} not found in ({r_idx}, {c_idx})")
                fish_idx += 1
                continue

            min_y, min_x = coords.min(axis=0)
            max_y, max_x = coords.max(axis=0)

            # Crop exactly the fish sprite with 2px padding
            crop = cell[min_y:max_y+1, min_x:max_x+1].copy()

            # Set transparent alpha for background
            # Background pixels in the cropped box:
            bg_mask = (crop[:, :, 0] >= 242) & (crop[:, :, 1] >= 242) & (crop[:, :, 2] >= 242)
            crop[bg_mask, 3] = 0

            # Convert to PIL Image
            sprite = Image.fromarray(crop)

            # Pad into a square 32x32 or 48x48 or max dimension square with transparent margins
            max_dim = max(sprite.width, sprite.height)
            # Add small 2-pixel margin around the sprite
            sq_size = max_dim + 4
            square_img = Image.new('RGBA', (sq_size, sq_size), (0, 0, 0, 0))
            offset_x = (sq_size - sprite.width) // 2
            offset_y = (sq_size - sprite.height) // 2
            square_img.paste(sprite, (offset_x, offset_y), sprite)

            # Resize neatly to 32x32 pixel art (NEAREST neighbor to preserve crisp Minecraft pixel aesthetic)
            final_sprite = square_img.resize((32, 32), Image.Resampling.NEAREST)

            # Save to both target directories
            for d in dest_dirs:
                out_path = os.path.join(d, f'fish_{fish_idx}.png')
                final_sprite.save(out_path)

            print(f"Saved fish_{fish_idx}.png (original {sprite.width}x{sprite.height} -> 32x32)")
            fish_idx += 1

print("All 40 fish sprites extracted and saved successfully!")
