package me.furkankoc.aquafishing.fish;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class FishManager {

    private final AquaFishing plugin;
    private final Map<String, FishTemplate> fishMap = new HashMap<>();
    private final Map<FishRarity, List<FishTemplate>> rarityMap = new EnumMap<>(FishRarity.class);

    private final NamespacedKey keyFishId;
    private final NamespacedKey keyFishWeight;
    private final NamespacedKey keyCatcher;

    public FishManager(AquaFishing plugin) {
        this.plugin = plugin;
        this.keyFishId = new NamespacedKey(plugin, "fish_id");
        this.keyFishWeight = new NamespacedKey(plugin, "fish_weight");
        this.keyCatcher = new NamespacedKey(plugin, "catcher");
        loadFish();
    }

    public void loadFish() {
        fishMap.clear();
        for (FishRarity rarity : FishRarity.values()) {
            rarityMap.put(rarity, new ArrayList<>());
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("fish-types");
        if (section == null) {
            plugin.getLogger().warning("config.yml içinde 'fish-types' bölümü bulunamadı!");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection fishSec = section.getConfigurationSection(id);
            if (fishSec == null) continue;

            String name = fishSec.getString("name", id);
            String matName = fishSec.getString("material", "COD");
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                material = Material.COD;
            }

            int cmd = fishSec.getInt("custom-model-data", 0);
            String rarityStr = fishSec.getString("rarity", "COMMON").toUpperCase(Locale.ENGLISH);
            FishRarity rarity;
            try {
                rarity = FishRarity.valueOf(rarityStr);
            } catch (IllegalArgumentException e) {
                rarity = FishRarity.COMMON;
            }

            double minWeight = fishSec.getDouble("min-weight", 1.0);
            double maxWeight = fishSec.getDouble("max-weight", 5.0);
            List<String> lore = fishSec.getStringList("lore");

            FishTemplate template = new FishTemplate(id, name, material, cmd, rarity, minWeight, maxWeight, lore);
            fishMap.put(id, template);
            rarityMap.get(rarity).add(template);
        }

        plugin.getLogger().log(Level.INFO, "Toplam {0} adet özel balık türü yüklendi.", fishMap.size());
    }

    public CaughtFish rollRandomFish(Player player) {
        return rollRandomFish(player, 1.0);
    }

    public CaughtFish rollRandomFish(Player player, double luckMultiplier) {
        if (fishMap.isEmpty()) {
            return null;
        }

        FishRarity chosenRarity = rollRarity(luckMultiplier);
        List<FishTemplate> pool = rarityMap.get(chosenRarity);
        if (pool == null || pool.isEmpty()) {
            pool = new ArrayList<>(fishMap.values());
        }

        FishTemplate template = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        double rawWeight = template.minWeight() + (template.maxWeight() - template.minWeight()) * ThreadLocalRandom.current().nextDouble();
        double weight = Math.round(rawWeight * 100.0) / 100.0;

        return new CaughtFish(template, weight, player.getUniqueId(), player.getName(), System.currentTimeMillis());
    }

    private FishRarity rollRarity(double luckMultiplier) {
        int totalWeight = 0;
        Map<FishRarity, Integer> adjustedWeights = new EnumMap<>(FishRarity.class);

        for (FishRarity rarity : FishRarity.values()) {
            int w = rarity.getWeight();
            if (rarity != FishRarity.COMMON && luckMultiplier > 1.0) {
                w = (int) Math.round(w * luckMultiplier);
            }
            adjustedWeights.put(rarity, w);
            totalWeight += w;
        }

        int randomPoint = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (FishRarity rarity : FishRarity.values()) {
            current += adjustedWeights.get(rarity);
            if (randomPoint < current) {
                return rarity;
            }
        }
        return FishRarity.COMMON;
    }

    public ItemStack createFishItem(CaughtFish caughtFish) {
        FishTemplate template = caughtFish.template();
        ItemStack item = new ItemStack(template.material());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Custom Display Name
        meta.displayName(MessageUtil.parse(template.rarity().getFormattedTag() + " <white>" + template.name()));

        // Lore
        List<Component> loreComponents = new ArrayList<>();
        for (String line : template.lore()) {
            loreComponents.add(MessageUtil.parse(line));
        }
        loreComponents.add(Component.empty());
        loreComponents.add(MessageUtil.parse("<gray>Ağırlık: <yellow>" + MessageUtil.formatWeight(caughtFish.weight()) + " kg"));
        loreComponents.add(MessageUtil.parse("<gray>Yakalayan: <aqua>" + caughtFish.catcherName()));

        meta.lore(loreComponents);

        if (template.customModelData() > 0) {
            meta.setCustomModelData(template.customModelData());
            try {
                java.lang.reflect.Method setItemModelMethod = meta.getClass().getMethod("setItemModel", NamespacedKey.class);
                setItemModelMethod.invoke(meta, NamespacedKey.fromString("aquafishing:fish_" + template.id()));
            } catch (Throwable ignored) {}
        }

        // Persistent Data Container (PDC)
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyFishId, PersistentDataType.STRING, template.id());
        pdc.set(keyFishWeight, PersistentDataType.DOUBLE, caughtFish.weight());
        pdc.set(keyCatcher, PersistentDataType.STRING, caughtFish.catcherUuid().toString());

        item.setItemMeta(meta);
        return item;
    }

    public FishTemplate getFishTemplate(String id) {
        return fishMap.get(id);
    }

    public Collection<FishTemplate> getAllFish() {
        return Collections.unmodifiableCollection(fishMap.values());
    }
}
