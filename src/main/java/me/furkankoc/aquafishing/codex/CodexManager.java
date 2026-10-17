package me.furkankoc.aquafishing.codex;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.database.CodexRepository;
import me.furkankoc.aquafishing.database.DatabaseFactory;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-level manager responsible for tracking player discoveries and coordinating persistence
 * through the underlying CodexRepository (SQLite or YAML).
 */
public class CodexManager {

    private final AquaFishing plugin;
    private final CodexRepository repository;
    private final Map<UUID, PlayerCodex> cache = new ConcurrentHashMap<>();

    public CodexManager(AquaFishing plugin) {
        this.plugin = plugin;
        String storageType = plugin.getConfig().getString("settings.database.type", "SQLITE");
        this.repository = DatabaseFactory.createRepository(plugin, storageType);
    }

    public PlayerCodex getCodex(UUID uuid) {
        return cache.computeIfAbsent(uuid, repository::load);
    }

    public void recordCatch(Player player, CaughtFish fish) {
        PlayerCodex codex = getCodex(player.getUniqueId());
        boolean isFirst = codex.recordCatch(fish.template().id(), fish.weight());

        if (isFirst) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            String prefix = plugin.getConfig().getString("messages.prefix", "");
            player.sendMessage(MessageUtil.parse(prefix + "<gradient:#ffe259:#ffa751><b>[YENİ TÜR KEŞFEDİLDİ!]</b></gradient> <white>" +
                    fish.template().name() + " <gray>balık defterine eklendi! <yellow>(/fish book)"));
        }

        saveToDiskAsync(codex);
    }

    private void saveToDiskAsync(PlayerCodex codex) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.save(codex));
    }

    public void saveToDisk(PlayerCodex codex) {
        repository.save(codex);
    }

    public void unloadPlayer(UUID uuid) {
        PlayerCodex codex = cache.remove(uuid);
        if (codex != null) {
            repository.save(codex);
        }
    }

    public void saveAll() {
        repository.saveAll(cache.values());
    }

    public void shutdown() {
        saveAll();
        repository.close();
    }

    public CodexRepository getRepository() {
        return repository;
    }
}
