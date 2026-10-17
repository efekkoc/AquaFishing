from PIL import Image, ImageDraw
import os

rp_base = r"C:\Users\Furkan Koç\AppData\Roaming\.minecraft\resourcepacks\AquaFishing-Pack"
sprites_dir = os.path.join(rp_base, "assets", "minecraft", "textures", "gui", "sprites", "boss_bar")
os.makedirs(sprites_dir, exist_ok=True)

# Modern Minecraft boss bar sprite is 182 x 5 pixels (standard)
# Background: dark metallic slate border with deep ocean blue/gray interior
# Progress: vibrant glowing gradient with highlight line

def create_bar(bg_name, prog_name, border_col, bg_col, prog_grad_start, prog_grad_end):
    # 1. Background (182x5)
    bg = Image.new("RGBA", (182, 5), (0, 0, 0, 0))
    draw_bg = ImageDraw.Draw(bg)
    # Dark border
    draw_bg.rectangle([0, 0, 181, 4], fill=bg_col, outline=border_col)
    bg.save(os.path.join(sprites_dir, f"{bg_name}.png"))
    
    # 2. Progress (182x5)
    prog = Image.new("RGBA", (182, 5), (0, 0, 0, 0))
    draw_prog = ImageDraw.Draw(prog)
    for x in range(182):
        factor = x / 181.0
        r = int(prog_grad_start[0] + factor * (prog_grad_end[0] - prog_grad_start[0]))
        g = int(prog_grad_start[1] + factor * (prog_grad_end[1] - prog_grad_start[1]))
        b = int(prog_grad_start[2] + factor * (prog_grad_end[2] - prog_grad_start[2]))
        # Top highlight line
        prog.putpixel((x, 0), (min(255, r + 50), min(255, g + 50), min(255, b + 50), 255))
        # Center fill
        prog.putpixel((x, 1), (r, g, b, 255))
        prog.putpixel((x, 2), (r, g, b, 255))
        prog.putpixel((x, 3), (r, g, b, 255))
        # Bottom shadow
        prog.putpixel((x, 4), (max(0, r - 50), max(0, g - 50), max(0, b - 50), 255))
    
    prog.save(os.path.join(sprites_dir, f"{prog_name}.png"))

# Create themed bars:
# Blue (Ocean Tournament)
create_bar("blue_background", "blue_progress", (20, 40, 60), (10, 15, 25), (0, 198, 255), (0, 114, 255))
# Yellow (Gold / Legendary Catch)
create_bar("yellow_background", "yellow_progress", (80, 60, 20), (25, 20, 10), (255, 226, 89), (255, 167, 81))
# Green (Safe Reel Bar)
create_bar("green_background", "green_progress", (20, 70, 30), (10, 25, 15), (0, 255, 136), (0, 180, 80))
# Purple (Melon Kadim Ejder Cosmic)
create_bar("purple_background", "purple_progress", (60, 20, 80), (20, 10, 30), (210, 80, 255), (140, 20, 220))
# Red (High Tension Danger)
create_bar("red_background", "red_progress", (80, 20, 20), (30, 10, 10), (255, 75, 75), (200, 20, 40))

print("Created modern custom boss bar sprites in", sprites_dir)
