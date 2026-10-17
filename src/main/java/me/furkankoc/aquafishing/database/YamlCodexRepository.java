package me.furkankoc.aquafishing.database;

import me.furkankoc.aquafishing.codex.PlayerCodex;
import me.furkankoc.aquafishing.codex.PlayerFishProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class YamlCodexRepository implements CodexRepository {

    private final Plugin plugin;
    private final File dataFolder;

    public YamlCodexRepository(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
    }

    @Override
    public void init() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public PlayerCodex load(UUID uuid) {
        PlayerCodex codex = new PlayerCodex(uuid);
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return codex;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = yaml.getConfigurationSection("discoveries");
        if (sec == null) {
            return codex;
        }

        for (String fishId : sec.getKeys(false)) {
            int count = sec.getInt(fishId + ".count", 1);
            double maxWeight = sec.getDouble(fishId + ".max-weight", 1.0);
            long firstTime = sec.getLong(fishId + ".first-time", System.currentTimeMillis());

            PlayerFishProgress progress = new PlayerFishProgress(count, maxWeight, firstTime);
            codex.addLoadedProgress(fishId, progress);
        }

        return codex;
    }

    @Override
    public void save(PlayerCodex codex) {
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
            plugin.getLogger().log(Level.SEVERE, "Oyuncu defter verisi YAML'a kaydedilemedi: " + codex.getPlayerUuid(), e);
        }
    }

    @Override
    public void saveAll(Collection<PlayerCodex> codices) {
        for (PlayerCodex codex : codices) {
            save(codex);
        }
    }

    @Override
    public void close() {
        // No connections to close for YAML
    }
}
