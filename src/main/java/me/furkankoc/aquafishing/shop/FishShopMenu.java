package me.furkankoc.aquafishing.shop;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.codex.PlayerCodex;
import me.furkankoc.aquafishing.fish.FishTemplate;
import me.furkankoc.aquafishing.rod.CustomRod;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FishShopMenu implements InventoryHolder {

    private final AquaFishing plugin;
    private final Player player;
    private final Inventory inventory;

    private final Map<Integer, CustomRod> slotToRod = new HashMap<>();
    private final int SELL_SLOT = 31;
    private final int CLOSE_SLOT = 49;

    public FishShopMenu(AquaFishing plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtil.parse("<gradient:#ffe259:#ffa751><b>Balıkçı Pazarı & Olta Geliştirme</b></gradient>"));
        buildMenu();
    }

    private void buildMenu() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || (i >= 36 && i < 45) || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        PlayerCodex codex = plugin.getCodexManager().getCodex(player.getUniqueId());
        int playerCatches = codex.getTotalCatches();
        int playerGold = countGold(player);

        // 1. Kademeli 10 Olta Vitrini (2 Satır x 5 Sütun: Slot 11-15 ve 20-24)
        int[] rodSlots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
        int index = 0;

        List<CustomRod> sortedRods = plugin.getRodManager().getAllRods().stream()
                .sorted(java.util.Comparator.comparingInt(CustomRod::tier))
                .toList();

        for (CustomRod rod : sortedRods) {
            if (index >= rodSlots.length) break;

            int targetSlot = rodSlots[index++];
            slotToRod.put(targetSlot, rod);

            boolean isOwned = hasRodInInventory(player, rod.id());
            boolean hasPrevRod = (rod.previousRodId() == null || hasRodInInventory(player, rod.previousRodId()));
            boolean hasCatches = (playerCatches >= rod.requiredCatches());
            boolean hasGold = (playerGold >= rod.priceGold());
            boolean canUpgrade = (rod.previousRodId() == null) ? !isOwned : (hasPrevRod && hasCatches && hasGold);

            ItemStack rodItem = plugin.getRodManager().createRodItem(rod);
            ItemMeta meta = rodItem.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.parse(rod.name()));
                List<Component> lore = new ArrayList<>();

                String tierTag = switch (rod.tier()) {
                    case 1 -> "<gray>Kademe I (Çırak)</gray>";
                    case 2 -> "<green>Kademe II (Bambu)</green>";
                    case 3 -> "<aqua>Kademe III (Demir)</aqua>";
                    case 4 -> "<red>Kademe IV (Kızıltaş)</red>";
                    case 5 -> "<yellow>Kademe V (Altın)</yellow>";
                    case 6 -> "<light_purple>Kademe VI (Ametist)</light_purple>";
                    case 7 -> "<gradient:#00c6ff:#0072ff><b>Kademe VII (Prizmarin)</b></gradient>";
                    case 8 -> "<gradient:#00ffff:#7000ff><b>Kademe VIII (Elmas)</b></gradient>";
                    case 9 -> "<gradient:#ff4500:#8b0000><b>Kademe IX (Netherite)</b></gradient>";
                    case 10 -> "<gradient:#ffe259:#ffa751><b>Kademe X (KADİM EJDER)</b></gradient>";
                    default -> "<gray>Kademe " + rod.tier() + "</gray>";
                };
                lore.add(MessageUtil.parse(tierTag));
                lore.add(Component.empty());

                for (String l : rod.lore()) lore.add(MessageUtil.parse(l));

                lore.add(Component.empty());
                lore.add(MessageUtil.parse("<dark_gray>━━━━━━━━ Nitelikler ━━━━━━━━"));
                lore.add(MessageUtil.parse("<gray>Güç (Yeşil Alan): <green>+" + rod.powerBonus() + " Genişlik"));
                lore.add(MessageUtil.parse("<gray>Sarma Hızı: <aqua>x" + rod.speedMultiplier() + " Kontrol"));
                lore.add(MessageUtil.parse("<gray>Vuruş Hızı: <gold>+" + (rod.biteSpeedReductionTicks() / 20) + "s Hızlı Vurur"));
                lore.add(MessageUtil.parse("<gray>Efsanevi Şansı: <yellow>x" + rod.luckMultiplier()));

                if (rod.previousRodId() != null) {
                    lore.add(MessageUtil.parse("<dark_gray>━━━━━━━━ Gereksinimler ━━━━━━━━"));
                    CustomRod prev = plugin.getRodManager().getRod(rod.previousRodId());
                    String prevName = (prev != null) ? prev.name() : rod.previousRodId();
                    lore.add(MessageUtil.parse("  " + (hasPrevRod ? "<green>✔" : "<red>✘") + " <gray>Önceki Olta: " + prevName));
                    lore.add(MessageUtil.parse("  " + (hasCatches ? "<green>✔" : "<red>✘") + " <gray>Gerekli Balık Avı: <white>" + playerCatches + "/" + rod.requiredCatches()));
                    lore.add(MessageUtil.parse("  " + (hasGold ? "<green>✔" : "<red>✘") + " <gray>Maliyet: <gold>" + rod.priceGold() + " Altın Külçesi <dark_gray>(Sende: " + playerGold + ")"));
                }

                lore.add(Component.empty());
                if (isOwned) {
                    lore.add(MessageUtil.parse("<green><b>✔ Envanterinde Mevcut</b>"));
                } else if (canUpgrade) {
                    lore.add(MessageUtil.parse(rod.tier() == 1 ? "<green><b>▶ Ücretsiz Almak İçin Tıkla!</b>" : "<green><b>▶ Yükseltmek İçin Tıkla!</b>"));
                } else {
                    lore.add(MessageUtil.parse("<red><b>✘ Gereksinimler Karşılanmadı</b>"));
                }

                meta.lore(lore);
                rodItem.setItemMeta(meta);
            }
            inventory.setItem(targetSlot, rodItem);
        }

        // 2. Balıkları Sat Butonu (Slot 31)
        ItemStack sellBtn = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta sMeta = sellBtn.getItemMeta();
        if (sMeta != null) {
            sMeta.displayName(MessageUtil.parse("<gradient:#00ff88:#00ffff><b>TÜM TURNUVA BALIKLARINI SAT</b></gradient>"));
            List<Component> sLore = new ArrayList<>();
            sLore.add(MessageUtil.parse("<gray>Envanterinde tuttuğun tüm balıkları altına dönüştürür."));
            sLore.add(Component.empty());
            sLore.add(MessageUtil.parse("<dark_gray>Birim Fiyatlar:"));
            sLore.add(MessageUtil.parse("  <gray>Yaygın: <gold>2 Altın"));
            sLore.add(MessageUtil.parse("  <aqua>Nadir: <gold>8 Altın"));
            sLore.add(MessageUtil.parse("  <light_purple>Epik: <gold>24 Altın"));
            sLore.add(MessageUtil.parse("  <yellow>Efsanevi: <gold>64 Altın"));
            sLore.add(Component.empty());
            sLore.add(MessageUtil.parse("<green><b>▶ Balıkları Satmak İçin Tıkla!</b>"));
            sMeta.lore(sLore);
            sellBtn.setItemMeta(sMeta);
        }
        inventory.setItem(SELL_SLOT, sellBtn);

        // 3. Kapat Butonu (Slot 49)
        ItemStack close = createItem(Material.BARRIER, "<red><b>Kapat</b>", List.of("<gray>Pazardan ayrıl."));
        inventory.setItem(CLOSE_SLOT, close);
    }

    public void handleClick(int slot) {
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (slot == SELL_SLOT) {
            sellAllFish();
            return;
        }

        CustomRod rod = slotToRod.get(slot);
        if (rod != null) {
            buyRod(rod);
        }
    }

    public void sellAllFishFromCommand() {
        sellAllFish(false);
    }

    private void sellAllFish() {
        sellAllFish(true);
    }

    public void sellAllFish(boolean refreshMenu) {
        NamespacedKey fishKey = new NamespacedKey(plugin, "fish_id");
        int totalGold = 0;
        int fishSold = 0;

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(fishKey, PersistentDataType.STRING)) {
                String fishId = pdc.get(fishKey, PersistentDataType.STRING);
                FishTemplate template = plugin.getFishManager().getAllFish().stream()
                        .filter(f -> f.id().equals(fishId))
                        .findFirst()
                        .orElse(null);

                int pricePerItem = 2;
                if (template != null) {
                    pricePerItem = switch (template.rarity()) {
                        case RARE -> 8;
                        case EPIC -> 24;
                        case LEGENDARY -> 64;
                        default -> 2;
                    };
                }

                int amount = item.getAmount();
                totalGold += (pricePerItem * amount);
                fishSold += amount;
                player.getInventory().setItem(i, null);
            }
        }

        if (fishSold == 0) {
            player.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.prefix", "") + "<red>Envanterinde satılacak turnuva balığı bulunamadı!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        giveGold(player, totalGold);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.prefix", "") +
                "<green><b>SATIŞ YAPILDI!</b> <gray>Toplam <white>" + fishSold + " adet <gray>balık satıldı ve <gold><b>" +
                totalGold + " Altın Külçesi</b> <gray>kazandın!"));

        if (refreshMenu) {
            buildMenu();
        }
    }

    private void buyRod(CustomRod rod) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        if (hasRodInInventory(player, rod.id())) {
            player.sendMessage(MessageUtil.parse(prefix + "<yellow>Bu oltaya zaten envanterinde sahipsin!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        PlayerCodex codex = plugin.getCodexManager().getCodex(player.getUniqueId());
        int playerCatches = codex.getTotalCatches();

        // 1. Av Gereksinimi
        if (playerCatches < rod.requiredCatches()) {
            player.sendMessage(MessageUtil.parse(prefix +
                    "<red>Yetersiz balık avı! Bu kademe için en az <yellow>" + rod.requiredCatches() + " balık <red>yakalamış olmalısın (Senin: " + playerCatches + ")."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // 2. Bir Önceki Olta Gereksinimi (Kümülatif yükseltme)
        if (rod.previousRodId() != null) {
            if (!hasRodInInventory(player, rod.previousRodId())) {
                CustomRod prev = plugin.getRodManager().getRod(rod.previousRodId());
                String prevName = (prev != null) ? prev.name() : rod.previousRodId();
                player.sendMessage(MessageUtil.parse(prefix +
                        "<red>Önce bir önceki kademe olan " + prevName + " <red>oltasına sahip olmalı ve envanterinde taşımalısın!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // 3. Altın Gereksinimi
        int playerGold = countGold(player);
        if (playerGold < rod.priceGold()) {
            player.sendMessage(MessageUtil.parse(prefix +
                    "<red>Yetersiz altın! Bu yükseltme için <gold>" + rod.priceGold() + " Altın Külçesi <red>gerekiyor (Sende: " + playerGold + ")."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Başarılı Yükseltme!
        if (rod.priceGold() > 0) {
            removeGold(player, rod.priceGold());
        }
        if (rod.previousRodId() != null) {
            removeRodFromInventory(player, rod.previousRodId());
        }

        ItemStack newRodItem = plugin.getRodManager().createRodItem(rod);
        player.getInventory().addItem(newRodItem);

        // Efektler
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.1);

        player.sendMessage(MessageUtil.parse(prefix +
                "<gold><b>TEBRİKLER!</b> <white>" + rod.name() + " <green>başarıyla üretildi ve envanterine eklendi!"));

        buildMenu();
    }

    private boolean hasRodInInventory(Player p, String rodId) {
        NamespacedKey rodKey = new NamespacedKey(plugin, "rod_id");
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.FISHING_ROD && item.hasItemMeta()) {
                String id = item.getItemMeta().getPersistentDataContainer().get(rodKey, PersistentDataType.STRING);
                if (rodId.equals(id)) return true;
            }
        }
        return false;
    }

    private void removeRodFromInventory(Player p, String rodId) {
        NamespacedKey rodKey = new NamespacedKey(plugin, "rod_id");
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.FISHING_ROD && item.hasItemMeta()) {
                String id = item.getItemMeta().getPersistentDataContainer().get(rodKey, PersistentDataType.STRING);
                if (rodId.equals(id)) {
                    p.getInventory().setItem(i, null);
                    return;
                }
            }
        }
    }

    private int countGold(Player p) {
        int count = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GOLD_INGOT) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeGold(Player p, int amount) {
        int remaining = amount;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.GOLD_INGOT) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    p.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
    }

    private void giveGold(Player p, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(64, remaining);
            p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, stack));
            remaining -= stack;
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtil.parse(name));
            if (lore != null) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) compLore.add(MessageUtil.parse(l));
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
