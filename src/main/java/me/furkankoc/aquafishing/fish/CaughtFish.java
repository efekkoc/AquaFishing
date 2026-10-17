package me.furkankoc.aquafishing.fish;

import java.util.UUID;

public record CaughtFish(
        FishTemplate template,
        double weight,
        UUID catcherUuid,
        String catcherName,
        long caughtTimeMillis
) {}
