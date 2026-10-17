from PIL import Image
import numpy as np
import os

img = Image.open(r'C:\Users\Furkan Koç\.gemini\antigravity\brain\38292d9e-3c51-45fc-8a3c-93a40d9baaa6\.user_uploaded\media_1792245138689_5a000956.png').convert('RGB')
arr = np.array(img)

out_dir = r'C:\Users\Furkan Koç\.gemini\antigravity\scratch\AquaFishing\extracted_fishes'
os.makedirs(out_dir, exist_ok=True)

fish_grid = [
    ["morina", "somon", "balon_baligi", "tropikal_balik", "yayin_baligi", "turna_baligi", "sardalya", "koi_baligi"],
    ["alabalik", "pilic_baligi", "kilic_baligi", "pirana", "fener_baligi", "yilan_baligi", "deniz_ati", "vatoz"],
    ["altin_sazan", "lav_baligi", "ejder_balik", "cerrah_baligi", "kaplan_baligi", "isik_baligi", "aksulot", "kopek_baligi"],
    ["aslan_baligi", "orkinos", "iskender_baligi", "ucan_balik", "zirhli_balik", "dev_murekkep", "buz_baligi", "yildiz_baligi"],
    ["balgam_baligi", "hayalet_balik", "kutsal_balik", "devasa_balik", "palyaco_baligi", "deniz_anasi", "kalkan_baligi", "merlin"]
]

bg_color = np.array([21.0, 21.0, 21.0])

for r in range(5):
    for c in range(8):
        name = fish_grid[r][c]
        
        # Approximate cell coordinates
        x1 = int(25 + c * 121.6)
        x2 = int(25 + (c + 1) * 121.6)
        y1 = int(28 + r * 104.0)
        # We only take the top 75 pixels to avoid the text label at the bottom of the cell
        y2 = int(y1 + 75)
        
        cell_arr = arr[y1:y2, x1:x2].astype(float)
        
        # Color distance from dark background
        dist = np.linalg.norm(cell_arr - bg_color, axis=2)
        
        # Calculate alpha
        t_low = 12.0
        t_high = 32.0
        alpha = np.clip((dist - t_low) / (t_high - t_low), 0.0, 1.0)
        
        # De-halo RGB
        mask = alpha > 0.03
        clean_rgb = np.zeros_like(cell_arr)
        for ch in range(3):
            clean_rgb[:, :, ch] = np.where(mask, np.clip((cell_arr[:, :, ch] - bg_color[ch] * (1.0 - alpha)) / np.maximum(alpha, 0.01), 0, 255), 0)
        
        out_rgba = np.dstack([clean_rgb, alpha * 255]).astype(np.uint8)
        fish_img = Image.fromarray(out_rgba, 'RGBA')
        
        # Crop to tight bounding box of the fish sprite
        bbox = fish_img.getbbox()
        if bbox:
            tight = fish_img.crop(bbox)
            # Square canvas with small padding
            max_d = max(tight.width, tight.height)
            pad_size = int(max_d * 1.15)
            sq = Image.new('RGBA', (pad_size, pad_size), (0, 0, 0, 0))
            offset = ((pad_size - tight.width) // 2, (pad_size - tight.height) // 2)
            sq.paste(tight, offset)
            
            # Resize to standard 64x64 item icon
            final_sprite = sq.resize((64, 64), Image.Resampling.LANCZOS)
            out_file = os.path.join(out_dir, f"{name}.png")
            final_sprite.save(out_file)
            print(f"Extracted {name}.png: bbox={bbox}")
        else:
            print(f"Warning: bbox empty for {name}")

print("Extraction of all 40 fish completed!")
