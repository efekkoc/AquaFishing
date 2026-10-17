package me.furkankoc.aquafishing.contest;

import me.furkankoc.aquafishing.fish.CaughtFish;

import java.util.Objects;
import java.util.UUID;

public record ContestEntry(
        UUID playerUuid,
        String playerName,
        CaughtFish bestFish
) implements Comparable<ContestEntry> {

    @Override
    public int compareTo(ContestEntry other) {
        // En yüksek ağırlık en üstte sıralanır
        return Double.compare(other.bestFish().weight(), this.bestFish().weight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContestEntry entry)) return false;
        return Objects.equals(playerUuid, entry.playerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(playerUuid);
    }
}
