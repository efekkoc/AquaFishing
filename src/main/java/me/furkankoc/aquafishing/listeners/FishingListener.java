package me.furkankoc.aquafishing.listeners;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.rod.CustomRod;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FishingListener implements Listener {

    private final AquaFishing plugin;

    public FishingListener(AquaFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        switch (event.getState()) {
            case FISHING -> {
                // Oltayı suya attığında ısırma bekleme süresini oltaya göre hızlandır
                FishHook hook = event.getHook();
                CustomRod rod = plugin.getRodManager().getEquippedRod(player);
                if (rod != null && rod.biteSpeedReductionTicks() > 0) {
                    int reduction = rod.biteSpeedReductionTicks();
                    int minWait = Math.max(20, 100 - (reduction / 2));
                    int maxWait = Math.max(40, 600 - reduction);
                    hook.setMinWaitTime(minWait);
                    hook.setMaxWaitTime(maxWait);
                }
            }

            case BITE -> {
                // Balık vurduğunda minigame başlat
                if (!plugin.getMinigameManager().hasActiveSession(player)) {
                    CustomRod rod = plugin.getRodManager().getEquippedRod(player);
                    CaughtFish fish = plugin.getFishManager().rollRandomFish(player, rod.luckMultiplier());
                    if (fish != null) {
                        plugin.getMinigameManager().startSession(player, event.getHook(), fish);
                    }
                }
            }

            case CAUGHT_FISH, REEL_IN -> {
                // Oyuncu oltayı çektiğinde sarma mekaniği
                if (plugin.getMinigameManager().hasActiveSession(player)) {
                    event.setCancelled(true);
                    plugin.getMinigameManager().handleReel(player);
                }
            }

            case FAILED_ATTEMPT, IN_GROUND -> {
                if (plugin.getMinigameManager().hasActiveSession(player)) {
                    plugin.getMinigameManager().cancelSession(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getMinigameManager().hasActiveSession(player)) {
            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK ||
                action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.getMinigameManager().handleReel(player);
            }
        }
    }
}
