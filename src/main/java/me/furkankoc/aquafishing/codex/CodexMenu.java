package me.furkankoc.aquafishing.codex;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.fish.FishTemplate;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CodexMenu implements InventoryHolder {

    private final AquaFishing plugin;
    private final Player player;
    private final Inventory inventory;

    public CodexMenu(AquaFishing plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtil.parse("<gradient:#00c6ff:#0072ff><b>Balık Defteri</b></gradient> <dark_gray>| Koleksiyon"));
        buildMenu();
    }

    private void buildMenu() {
        PlayerCodex codex = plugin.getCodexManager().getCodex(player.getUniqueId());
        Collection<FishTemplate> allFish = plugin.getFishManager().getAllFish();

        // Üst ve yan kenarlıklar (Dekoratif cam paneller)
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        int slot = 0;
        int discoveredCount = 0;

        for (FishTemplate fish : allFish) {
            if (slot >= 45) break;

            boolean isDiscovered = codex.hasDiscovered(fish.id());
            if (isDiscovered) {
                discoveredCount++;
                PlayerFishProgress progress = codex.getProgress(fish.id());

                ItemStack item = new ItemStack(fish.material());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageUtil.parse(fish.rarity().getFormattedTag() + " <white>" + fish.name()));

                    List<Component> lore = new ArrayList<>();
                    for (String line : fish.lore()) {
                        lore.add(MessageUtil.parse(line));
                    }
                    lore.add(Component.empty());
                    lore.add(MessageUtil.parse("<green>✔ Bu balık defterinde kayıtlı!"));
                    lore.add(MessageUtil.parse("<gray>Kişisel Rekor: <yellow>" + MessageUtil.formatWeight(progress.getMaxWeight()) + " kg"));
                    lore.add(MessageUtil.parse("<gray>Toplam Yakalanma: <aqua>" + progress.getCatchCount() + " adet"));

                    if (fish.customModelData() > 0) {
                        meta.setCustomModelData(fish.customModelData());
                        try {
                            java.lang.reflect.Method setItemModelMethod = meta.getClass().getMethod("setItemModel", org.bukkit.NamespacedKey.class);
                            setItemModelMethod.invoke(meta, org.bukkit.NamespacedKey.fromString("aquafishing:fish_" + fish.id()));
                        } catch (Throwable ignored) {}
                    }

                    meta.lore(lore);
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot, item);
            } else {
                // Keşfedilmemiş balık
                ItemStack unknown = new ItemStack(Material.GRAY_DYE);
                ItemMeta meta = unknown.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageUtil.parse("<gray>??? <dark_gray>(Bilinmeyen Balık)"));
                    List<Component> lore = new ArrayList<>();
                    lore.add(MessageUtil.parse(fish.rarity().getFormattedTag()));
                    lore.add(MessageUtil.parse("<red>✘ Henüz yakalanmadı!"));
                    lore.add(MessageUtil.parse("<dark_gray>Sularda şansını dene ve bu türü keşfet."));
                    meta.lore(lore);
                    unknown.setItemMeta(meta);
                }
                inventory.setItem(slot, unknown);
            }

            slot++;
        }

        // İlerleme Yıldızı (Slot 49)
        int totalFish = allFish.size();
        int percent = (totalFish > 0) ? (discoveredCount * 100 / totalFish) : 0;

        ItemStack progressItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta pMeta = progressItem.getItemMeta();
        if (pMeta != null) {
            pMeta.displayName(MessageUtil.parse("<gradient:#ffe259:#ffa751><b>Koleksiyon İlerlemesi</b></gradient>"));
            List<Component> pLore = new ArrayList<>();
            pLore.add(MessageUtil.parse("<gray>Keşfedilen: <yellow>" + discoveredCount + " <gray>/ <white>" + totalFish));
            pLore.add(MessageUtil.parse("<gray>Tamamlanma Oranı: <green>%" + percent));
            if (percent >= 100) {
                pLore.add(Component.empty());
                pLore.add(MessageUtil.parse("<gold>★ TEBRİKLER! Tüm balıkları keşfettin! ★"));
            }
            pMeta.lore(pLore);
            progressItem.setItemMeta(pMeta);
        }
        inventory.setItem(49, progressItem);

        // Kapat Butonu (Slot 53)
        ItemStack closeItem = createItem(Material.BARRIER, "<red><b>Kapat</b>", List.of("<gray>Menüden çıkış yap."));
        inventory.setItem(53, closeItem);
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
