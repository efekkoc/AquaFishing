package me.furkankoc.aquafishing.fish;

import org.bukkit.Material;

import java.util.List;

public record FishTemplate(
        String id,
        String name,
        Material material,
        int customModelData,
        FishRarity rarity,
        double minWeight,
        double maxWeight,
        List<String> lore
) {}
