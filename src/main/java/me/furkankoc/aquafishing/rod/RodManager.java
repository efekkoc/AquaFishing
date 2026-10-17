package me.furkankoc.aquafishing.rod;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RodManager {

    private final AquaFishing plugin;
    private final Map<String, CustomRod> rods = new LinkedHashMap<>();

    private final NamespacedKey keyRodId;
    private final NamespacedKey keyTier;
    private final NamespacedKey keyPower;
    private final NamespacedKey keySpeed;
    private final NamespacedKey keyLuck;
    private final NamespacedKey keyBite;

    private BukkitTask auraTask;

    public RodManager(AquaFishing plugin) {
        this.plugin = plugin;
        this.keyRodId = new NamespacedKey(plugin, "rod_id");
        this.keyTier = new NamespacedKey(plugin, "rod_tier");
        this.keyPower = new NamespacedKey(plugin, "rod_power");
        this.keySpeed = new NamespacedKey(plugin, "rod_speed");
        this.keyLuck = new NamespacedKey(plugin, "rod_luck");
        this.keyBite = new NamespacedKey(plugin, "rod_bite");
        loadRods();
        startAuraTask();
    }

    public void loadRods() {
        rods.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("custom-rods");
        if (sec == null) {
            loadDefaultRods();
            return;
        }

        for (String id : sec.getKeys(false)) {
            ConfigurationSection rodSec = sec.getConfigurationSection(id);
            if (rodSec == null) continue;

            String name = rodSec.getString("name", id);
            int tier = rodSec.getInt("tier", 1);
            String prevId = rodSec.getString("previous-rod", null);
            int cmd = rodSec.getInt("custom-model-data", tier * 1000);
            int power = rodSec.getInt("power", 0);
            double speed = rodSec.getDouble("speed", 1.0);
            double luck = rodSec.getDouble("luck", 1.0);
            int bite = rodSec.getInt("bite-reduction", 0);
            int price = rodSec.getInt("price-gold", 10);
            int reqCatches = rodSec.getInt("required-catches", 0);
            List<String> lore = rodSec.getStringList("lore");

            rods.put(id, new CustomRod(id, name, tier, prevId, cmd, power, speed, luck, bite, price, reqCatches, lore));
        }
    }

    private void loadDefaultRods() {
        rods.put("cirak", new CustomRod("cirak", "<gray>Çırak Tahta Oltası", 1, null, 1000, 1, 1.0, 1.0, 0, 0, 0,
                List.of("<gray>Yeni başlayan balıkçılar için meşe ağacı olta.")));
        rods.put("bambu", new CustomRod("bambu", "<green>Bambu Nehir Oltası", 2, "cirak", 2000, 2, 1.15, 1.2, 30, 16, 5,
                List.of("<green>Hafif ve esnek bambu gövde ile çevik sarma.")));
        rods.put("demir", new CustomRod("demir", "<aqua>Takviyeli Demir Olta", 3, "bambu", 3000, 3, 1.3, 1.4, 60, 48, 15,
                List.of("<aqua>Çelik alaşımlı halkalar ve sağlam metalik makara.")));
        rods.put("kiziltas", new CustomRod("kiziltas", "<red>Kızıltaş Mekanik Olta", 4, "demir", 4000, 4, 1.5, 1.7, 90, 120, 30,
                List.of("<red>Dişli çarklar ve kızıltaş mekanizmasıyla hızlı sarma.")));
        rods.put("altin", new CustomRod("altin", "<yellow>Saf Altın Bereket Oltası", 5, "kiziltas", 5000, 5, 1.7, 2.1, 120, 256, 50,
                List.of("<yellow>Parıldayan altın kaplama ile nadir balık şansı.")));
        rods.put("ametist", new CustomRod("ametist", "<light_purple>Derin Deniz Ametist Oltası", 6, "altin", 6000, 6, 1.9, 2.5, 150, 450, 75,
                List.of("<light_purple>Mor kristal gövdesi derinlerdeki gizemli balıkları çeker.")));
        rods.put("prizmarin", new CustomRod("prizmarin", "<gradient:#00c6ff:#0072ff><b>Prizmarin Okyanus Oltası</b></gradient>", 7, "ametist", 7000, 7, 2.1, 3.0, 180, 750, 110,
                List.of("<aqua>Tapınak prizmarini ve parlayan deniz feneri ucu.")));
        rods.put("elmas", new CustomRod("elmas", "<gradient:#00ffff:#7000ff><b>Usta Elmas Olta</b></gradient>", 8, "prizmarin", 8000, 8, 2.3, 3.6, 210, 1200, 160,
                List.of("<gradient:#00ffff:#7000ff>Kırılmaz elmas kaplama ve dev yırtıcı direnci.</gradient>")));
        rods.put("netherite", new CustomRod("netherite", "<gradient:#ff4500:#8b0000><b>Netherite Magma Oltası</b></gradient>", 9, "elmas", 9000, 9, 2.6, 4.3, 240, 2000, 220,
                List.of("<gradient:#ff4500:#8b0000>Lavlara dayanıklı antik zırh ve alev saçan misina.</gradient>")));
        rods.put("melon_kadim", new CustomRod("melon_kadim", "<gradient:#ffe259:#ffa751><b>Melon Kadim Ejder Oltası</b></gradient>", 10, "netherite", 10000, 11, 3.0, 5.5, 270, 3500, 300,
                List.of("<gradient:#ffe259:#ffa751>★ EFSANEVİ MELON KADİM EJDER OLTASI ★</gradient>",
                        "<yellow>Devasa yeşil bar (%75!) ve anında ısırma gücü!",
                        "<dark_purple>Kademe X - Mitolojik Zirve Oltası")));
    }

    public ItemStack createRodItem(CustomRod rod) {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(MessageUtil.parse(rod.name()));

        List<Component> lore = new ArrayList<>();
        // Görsel Kademe Çubuğu
        String bar = switch (rod.tier()) {
            case 1 -> "<gray>[ <white>▰▱▱▱▱▱▱▱▱▱</white> ] Kademe I";
            case 2 -> "<green>[ <white>▰▰▱▱▱▱▱▱▱▱</white> ] Kademe II";
            case 3 -> "<aqua>[ <white>▰▰▰▱▱▱▱▱▱▱</white> ] Kademe III";
            case 4 -> "<red>[ <white>▰▰▰▰▱▱▱▱▱▱</white> ] Kademe IV";
            case 5 -> "<yellow>[ <white>▰▰▰▰▰▱▱▱▱▱</white> ] Kademe V";
            case 6 -> "<light_purple>[ <white>▰▰▰▰▰▰▱▱▱▱</white> ] Kademe VI";
            case 7 -> "<gradient:#00c6ff:#0072ff>[ ▰▰▰▰▰▰▰▱▱▱ ] Kademe VII</gradient>";
            case 8 -> "<gradient:#00ffff:#7000ff>[ ▰▰▰▰▰▰▰▰▱▱ ] Kademe VIII</gradient>";
            case 9 -> "<gradient:#ff4500:#8b0000>[ ▰▰▰▰▰▰▰▰▰▱ ] Kademe IX</gradient>";
            case 10 -> "<gradient:#ffe259:#ffa751><b>[ ▰▰▰▰▰▰▰▰▰▰ ] Kademe X (KADİM)</b></gradient>";
            default -> "<gray>[ ▰▱▱▱▱▱▱▱▱▱ ]";
        };
        lore.add(MessageUtil.parse(bar));
        lore.add(Component.empty());

        for (String line : rod.lore()) {
            lore.add(MessageUtil.parse(line));
        }

        lore.add(Component.empty());
        lore.add(MessageUtil.parse("<dark_gray>━━━━━━━━ Nitelikler ━━━━━━━━"));
        lore.add(MessageUtil.parse("<gray>Güç (Yeşil Alan): <green>+" + rod.powerBonus() + " Genişlik"));
        lore.add(MessageUtil.parse("<gray>Sarma Hızı: <aqua>x" + rod.speedMultiplier() + " Kontrol"));
        lore.add(MessageUtil.parse("<gray>Vuruş Hızı: <gold>+" + (rod.biteSpeedReductionTicks() / 20) + "s Hızlı Vurur"));
        lore.add(MessageUtil.parse("<gray>Efsanevi Şansı: <yellow>x" + rod.luckMultiplier()));
        lore.add(MessageUtil.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        meta.lore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, Math.max(1, rod.tier()), true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (rod.customModelData() > 0) {
            meta.setCustomModelData(rod.customModelData());
            try {
                java.lang.reflect.Method setItemModelMethod = meta.getClass().getMethod("setItemModel", NamespacedKey.class);
                String modelName = "aquafishing:rod_" + rod.tier();
                setItemModelMethod.invoke(meta, NamespacedKey.fromString(modelName));
            } catch (Throwable ignored) {
                // Pre-1.21.4 or method not found, customModelData handles it
            }
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyRodId, PersistentDataType.STRING, rod.id());
        pdc.set(keyTier, PersistentDataType.INTEGER, rod.tier());
        pdc.set(keyPower, PersistentDataType.INTEGER, rod.powerBonus());
        pdc.set(keySpeed, PersistentDataType.DOUBLE, rod.speedMultiplier());
        pdc.set(keyLuck, PersistentDataType.DOUBLE, rod.luckMultiplier());
        pdc.set(keyBite, PersistentDataType.INTEGER, rod.biteSpeedReductionTicks());

        item.setItemMeta(meta);
        return item;
    }

    private void startAuraTask() {
        this.auraTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                CustomRod rod = getEquippedRod(player);
                if (rod == null || rod.tier() <= 4) continue;

                // Ayak tabanında çok hafif, zarif zemin aurası (ekranı ve görüşü kesinlikle kapatmaz)
                Location loc = player.getLocation().add(0, 0.02, 0);

                if (rod.tier() == 10) {
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0.15, 0.0, 0.15, 0.001);
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.12, 0.0, 0.12, 0.001);
                } else if (rod.tier() >= 8) {
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.1, 0.0, 0.1, 0.001);
                } else if (rod.tier() >= 6) {
                    player.getWorld().spawnParticle(Particle.WAX_ON, loc, 1, 0.1, 0.0, 0.1, 0.001);
                }
            }
        }, 40L, 40L);
    }

    public CustomRod getEquippedRod(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (isCustomRod(main)) {
            String id = main.getItemMeta().getPersistentDataContainer().get(keyRodId, PersistentDataType.STRING);
            return rods.getOrDefault(id, getFallbackRod());
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (isCustomRod(off)) {
            String id = off.getItemMeta().getPersistentDataContainer().get(keyRodId, PersistentDataType.STRING);
            return rods.getOrDefault(id, getFallbackRod());
        }

        return getFallbackRod();
    }

    public boolean isCustomRod(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(keyRodId, PersistentDataType.STRING);
    }

    private CustomRod getFallbackRod() {
        return rods.getOrDefault("cirak", new CustomRod("cirak", "<gray>Çırak Tahta Oltası", 1, null, 1000, 1, 1.0, 1.0, 0, 0, 0, Collections.emptyList()));
    }

    public Collection<CustomRod> getAllRods() {
        return rods.values();
    }

    public CustomRod getRod(String id) {
        return rods.get(id);
    }

    public void shutdown() {
        if (auraTask != null) {
            auraTask.cancel();
            auraTask = null;
        }
    }
}
