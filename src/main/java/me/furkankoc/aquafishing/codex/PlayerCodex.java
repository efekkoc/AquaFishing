package me.furkankoc.aquafishing.codex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCodex {

    private final UUID playerUuid;
    private final Map<String, PlayerFishProgress> discoveries = new HashMap<>();

    public PlayerCodex(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public boolean recordCatch(String fishId, double weight) {
        PlayerFishProgress progress = discoveries.get(fishId);
        boolean isFirstDiscovery = (progress == null);

        if (isFirstDiscovery) {
            progress = new PlayerFishProgress(1, weight, System.currentTimeMillis());
            discoveries.put(fishId, progress);
        } else {
            progress.recordCatch(weight);
        }

        return isFirstDiscovery;
    }

    public boolean hasDiscovered(String fishId) {
        return discoveries.containsKey(fishId);
    }

    public void addLoadedProgress(String fishId, PlayerFishProgress progress) {
        discoveries.put(fishId, progress);
    }

    public PlayerFishProgress getProgress(String fishId) {
        return discoveries.get(fishId);
    }

    public Map<String, PlayerFishProgress> getDiscoveries() {
        return Collections.unmodifiableMap(discoveries);
    }

    public int getTotalCatches() {
        int total = 0;
        for (PlayerFishProgress p : discoveries.values()) {
            total += p.getCatchCount();
        }
        return total;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }
}
