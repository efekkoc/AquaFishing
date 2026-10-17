package me.furkankoc.aquafishing.codex;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CodexManager {

    private final AquaFishing plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerCodex> cache = new ConcurrentHashMap<>();

    public CodexManager(AquaFishing plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerCodex getCodex(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromDisk);
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

    private PlayerCodex loadFromDisk(UUID uuid) {
        PlayerCodex codex = new PlayerCodex(uuid);
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return codex;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = yaml.getConfigurationSection("discoveries");
        if (sec == null) return codex;

        for (String fishId : sec.getKeys(false)) {
            int count = sec.getInt(fishId + ".count", 1);
            double maxWeight = sec.getDouble(fishId + ".max-weight", 1.0);
            long firstTime = sec.getLong(fishId + ".first-time", System.currentTimeMillis());

            PlayerFishProgress progress = new PlayerFishProgress(count, maxWeight, firstTime);
            codex.addLoadedProgress(fishId, progress);
        }

        return codex;
    }

    private void saveToDiskAsync(PlayerCodex codex) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveToDisk(codex));
    }

    public void saveToDisk(PlayerCodex codex) {
        File file = new File(dataFolder, codex.getPlayerUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<String, PlayerFishProgress> entry : codex.getDiscoveries().entrySet()) {
            String path = "discoveries." + entry.getKey();
            yaml.set(path + ".count", entry.getValue().getCatchCount());
            yaml.set(path + ".max-weight", entry.getValue().getMaxWeight());
            yaml.set(path + ".first-time", entry.getValue().getFirstDiscoveredMillis());
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Oyuncu defter verisi kaydedilemedi: " + codex.getPlayerUuid(), e);
        }
    }

    public void saveAll() {
        for (PlayerCodex codex : cache.values()) {
            saveToDisk(codex);
        }
    }
}
