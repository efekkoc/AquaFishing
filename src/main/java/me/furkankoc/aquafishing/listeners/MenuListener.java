package me.furkankoc.aquafishing.listeners;

import me.furkankoc.aquafishing.codex.CodexMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof CodexMenu) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                int slot = event.getRawSlot();
                if (slot == 53) { // Kapat butonu
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                } else if (slot >= 0 && slot < 54) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                }
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof me.furkankoc.aquafishing.shop.FishShopMenu shop) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 54) {
                shop.handleClick(slot);
            }
        }
    }
}
