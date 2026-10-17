from PIL import Image, ImageDraw
import os

rp_base = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
gui_dir = os.path.join(rp_base, "assets", "minecraft", "textures", "gui")
os.makedirs(gui_dir, exist_ok=True)

# bars.png is 256x256
bars = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
draw = ImageDraw.Draw(bars)

# Rows in bars.png:
# Row 0: Pink unnotched (y=0 bg, y=5 fg)
# Row 1: Blue unnotched (y=10 bg, y=15 fg)
# Row 2: Red unnotched (y=20 bg, y=25 fg)
# Row 3: Green unnotched (y=30 bg, y=35 fg)
# Row 4: Yellow unnotched (y=40 bg, y=45 fg)
# Row 5: Purple unnotched (y=50 bg, y=55 fg)
# Row 6: White unnotched (y=60 bg, y=65 fg)

colors = [
    (10, (0, 198, 255), (0, 114, 255)),   # Blue
    (20, (255, 75, 75), (200, 20, 40)),   # Red
    (30, (0, 255, 136), (0, 180, 80)),   # Green
    (40, (255, 226, 89), (255, 167, 81)), # Yellow
    (50, (210, 80, 255), (140, 20, 220)), # Purple
]

for y_base, col_start, col_end in colors:
    # BG: y_base to y_base + 4 (182 wide)
    draw.rectangle([0, y_base, 181, y_base + 4], fill=(20, 25, 35, 240), outline=(40, 45, 60, 255))
    # FG: y_base + 5 to y_base + 9 (182 wide)
    for x in range(182):
        f = x / 181.0
        r = int(col_start[0] + f * (col_end[0] - col_start[0]))
        g = int(col_start[1] + f * (col_end[1] - col_start[1]))
        b = int(col_start[2] + f * (col_end[2] - col_start[2]))
        bars.putpixel((x, y_base + 5), (min(255, r + 40), min(255, g + 40), min(255, b + 40), 255))
        bars.putpixel((x, y_base + 6), (r, g, b, 255))
        bars.putpixel((x, y_base + 7), (r, g, b, 255))
        bars.putpixel((x, y_base + 8), (r, g, b, 255))
        bars.putpixel((x, y_base + 9), (max(0, r - 40), max(0, g - 40), max(0, b - 40), 255))

bars_path = os.path.join(gui_dir, "bars.png")
bars.save(bars_path)
print("Saved bars.png in", bars_path)
