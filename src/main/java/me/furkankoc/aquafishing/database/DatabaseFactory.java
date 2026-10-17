package me.furkankoc.aquafishing.database;

import org.bukkit.plugin.Plugin;

public final class DatabaseFactory {

    private DatabaseFactory() {}

    public static CodexRepository createRepository(Plugin plugin, String storageType) {
        CodexRepository repository;

        if ("YAML".equalsIgnoreCase(storageType)) {
            repository = new YamlCodexRepository(plugin);
        } else {
            // Default to SQLite
            repository = new SqliteCodexRepository(plugin);
        }

        repository.init();
        return repository;
    }
}
