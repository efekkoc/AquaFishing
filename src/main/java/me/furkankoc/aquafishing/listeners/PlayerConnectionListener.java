package me.furkankoc.aquafishing.listeners;

import me.furkankoc.aquafishing.AquaFishing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final AquaFishing plugin;

    public PlayerConnectionListener(AquaFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getContestManager().onPlayerJoin(event.getPlayer());
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                plugin.getPackServer().sendPack(event.getPlayer());
            }
        }, 15L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getContestManager().onPlayerQuit(event.getPlayer());
        plugin.getMinigameManager().cancelSession(event.getPlayer());
    }
}
