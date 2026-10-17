package me.furkankoc.aquafishing.database;

import me.furkankoc.aquafishing.codex.PlayerCodex;

import java.util.Collection;
import java.util.UUID;

/**
 * Data Access Object (DAO) / Repository interface for persisting player codex discoveries.
 * Decouples domain logic from the underlying storage mechanism (YAML, SQLite, MySQL).
 */
public interface CodexRepository {

    /**
     * Initializes the repository (e.g. creating tables or directories).
     */
    void init();

    /**
     * Loads a player's codex from persistent storage.
     *
     * @param uuid The unique ID of the player
     * @return The populated PlayerCodex instance
     */
    PlayerCodex load(UUID uuid);

    /**
     * Persists a player's codex to storage.
     *
     * @param codex The PlayerCodex to save
     */
    void save(PlayerCodex codex);

    /**
     * Batch saves all cached codices (typically called on server shutdown).
     *
     * @param codices Collection of codices to persist
     */
    void saveAll(Collection<PlayerCodex> codices);

    /**
     * Closes any open connections, thread pools, or streams.
     */
    void close();
}
