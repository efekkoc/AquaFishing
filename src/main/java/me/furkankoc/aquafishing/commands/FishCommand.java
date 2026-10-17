package me.furkankoc.aquafishing.commands;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.contest.ContestEntry;
import me.furkankoc.aquafishing.contest.ContestState;
import me.furkankoc.aquafishing.util.MessageUtil;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.fish.FishTemplate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FishCommand implements CommandExecutor, TabCompleter {

    private final AquaFishing plugin;

    public FishCommand(AquaFishing plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start" -> {
                if (!sender.hasPermission("aquafishing.admin")) {
                    sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.no-permission", "")));
                    return true;
                }

                if (plugin.getContestManager().getState() == ContestState.ACTIVE) {
                    String alreadyMsg = plugin.getConfig().getString("messages.contest-already-active", "")
                            .replace("{prefix}", prefix)
                            .replace("{time}", MessageUtil.formatDuration(plugin.getContestManager().getRemainingSeconds()));
                    sender.sendMessage(MessageUtil.parse(alreadyMsg));
                    return true;
                }

                long duration = plugin.getConfig().getLong("settings.default-duration-seconds", 300);
                if (args.length > 1) {
                    try {
                        duration = Long.parseLong(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageUtil.parse(prefix + "<red>Geçersiz süre! Örnek: /fish start 300"));
                        return true;
                    }
                }

                boolean started = plugin.getContestManager().startContest(duration);
                if (started) {
                    sender.sendMessage(MessageUtil.parse(prefix + "<green>Turnuva " + MessageUtil.formatDuration(duration) + " süreliğine başlatıldı!"));
                }
            }

            case "stop" -> {
                if (!sender.hasPermission("aquafishing.admin")) {
                    sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.no-permission", "")));
                    return true;
                }

                if (plugin.getContestManager().getState() == ContestState.INACTIVE) {
                    sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.contest-not-active", "")));
                    return true;
                }

                plugin.getContestManager().stopContest();
                sender.sendMessage(MessageUtil.parse(prefix + "<yellow>Turnuva yetkili tarafından sonlandırıldı."));
            }

            case "top" -> {
                List<ContestEntry> leaderboard = plugin.getContestManager().getSortedLeaderboard();
                if (leaderboard.isEmpty()) {
                    sender.sendMessage(MessageUtil.parse(prefix + "<gray>Şu anda sıralamada kayıtlı balık yok."));
                    return true;
                }

                sender.sendMessage(MessageUtil.parse("<gold>━━━━━━━━━━ <gradient:#00c6ff:#0072ff><b>LİDER TABLOSU</b></gradient> <gold>━━━━━━━━━━"));
                int rank = 1;
                for (ContestEntry entry : leaderboard) {
                    if (rank > 5) break;
                    String line = "  <yellow>#" + rank + " <white>" + entry.playerName() + " <dark_gray>- <aqua>" +
                            entry.bestFish().template().name() + " <gray>(<yellow>" +
                            MessageUtil.formatWeight(entry.bestFish().weight()) + " kg<gray>)";
                    sender.sendMessage(MessageUtil.parse(line));
                    rank++;
                }
                sender.sendMessage(MessageUtil.parse("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            }

            case "book", "menu", "defter" -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) {
                    sender.sendMessage(MessageUtil.parse(prefix + "<red>Bu komutu sadece oyuncular kullanabilir!"));
                    return true;
                }
                new me.furkankoc.aquafishing.codex.CodexMenu(plugin, player).open();
            }

            case "shop", "market" -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) {
                    sender.sendMessage(MessageUtil.parse(prefix + "<red>Bu komutu sadece oyuncular kullanabilir!"));
                    return true;
                }
                new me.furkankoc.aquafishing.shop.FishShopMenu(plugin, player).open();
            }

            case "sell", "sat" -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) {
                    sender.sendMessage(MessageUtil.parse(prefix + "<red>Bu komutu sadece oyuncular kullanabilir!"));
                    return true;
                }
                new me.furkankoc.aquafishing.shop.FishShopMenu(plugin, player).sellAllFishFromCommand();
            }

            case "give" -> {
                if (!sender.hasPermission("aquafishing.admin")) {
                    sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.no-permission", "")));
                    return true;
                }
                if (args.length >= 3 && args[1].equalsIgnoreCase("rod")) {
                    String rodId = args[2].toLowerCase();
                    org.bukkit.entity.Player target = (args.length >= 4) ? org.bukkit.Bukkit.getPlayer(args[3]) : (sender instanceof org.bukkit.entity.Player p ? p : null);
                    if (target == null) {
                        sender.sendMessage(MessageUtil.parse(prefix + "<red>Oyuncu bulunamadı!"));
                        return true;
                    }
                    me.furkankoc.aquafishing.rod.CustomRod rod = plugin.getRodManager().getRod(rodId);
                    if (rod == null) {
                        sender.sendMessage(MessageUtil.parse(prefix + "<red>Böyle bir olta yok! Örnekler: cirak, bambu, demir, kiziltas, altin, ametist, prizmarin, elmas, netherite, melon_kadim"));
                        return true;
                    }
                    target.getInventory().addItem(plugin.getRodManager().createRodItem(rod));
                    sender.sendMessage(MessageUtil.parse(prefix + "<green>" + target.getName() + " adlı oyuncuya " + rod.name() + " verildi!"));
                    return true;
                }
                if (args.length >= 3 && args[1].equalsIgnoreCase("fish")) {
                    String fishId = args[2].toLowerCase();
                    org.bukkit.entity.Player target = (args.length >= 4) ? org.bukkit.Bukkit.getPlayer(args[3]) : (sender instanceof org.bukkit.entity.Player p ? p : null);
                    if (target == null) {
                        sender.sendMessage(MessageUtil.parse(prefix + "<red>Oyuncu bulunamadı!"));
                        return true;
                    }
                    FishTemplate template = plugin.getFishManager().getFishTemplate(fishId);
                    if (template == null) {
                        sender.sendMessage(MessageUtil.parse(prefix + "<red>Böyle bir balık türü bulunamadı! Örneğin: alabalik, kadim_ejder"));
                        return true;
                    }
                    double weight = template.minWeight() + (template.maxWeight() - template.minWeight()) / 2.0;
                    CaughtFish caught = new CaughtFish(template, Math.round(weight * 100.0) / 100.0, target.getUniqueId(), target.getName(), System.currentTimeMillis());
                    ItemStack fishItem = plugin.getFishManager().createFishItem(caught);
                    target.getInventory().addItem(fishItem);
                    sender.sendMessage(MessageUtil.parse(prefix + "<green>" + target.getName() + " adlı oyuncuya " + template.name() + " verildi!"));
                    return true;
                }
                sender.sendMessage(MessageUtil.parse(prefix + "<yellow>Kullanım: /fish give rod <rodId> [oyuncu] veya /fish give fish <fishId> [oyuncu]"));
            }

            case "reload" -> {
                if (!sender.hasPermission("aquafishing.admin")) {
                    sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.no-permission", "")));
                    return true;
                }

                plugin.reloadConfig();
                plugin.getFishManager().loadFish();
                plugin.getRodManager().loadRods();
                sender.sendMessage(MessageUtil.parse(plugin.getConfig().getString("messages.reload-success", "")));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.parse("<gold>━━━━━━━━━━ <gradient:#00c6ff:#0072ff><b>AquaFishing Komutları</b></gradient> <gold>━━━━━━━━━━"));
        sender.sendMessage(MessageUtil.parse("  <yellow>/fish sell <gray>- Envanterindeki tüm turnuva balıklarını altına çevirir."));
        sender.sendMessage(MessageUtil.parse("  <yellow>/fish shop <gray>- Balıkçı pazarını ve olta geliştirme dükkanını açar."));
        sender.sendMessage(MessageUtil.parse("  <yellow>/fish book <gray>- Balık defterini (koleksiyon GUI) açar."));
        sender.sendMessage(MessageUtil.parse("  <yellow>/fish top <gray>- Anlık balıkçılık lider tablosunu görüntüler."));
        sender.sendMessage(MessageUtil.parse("  <yellow>/fish help <gray>- Bu yardım menüsünü görüntüler."));
        if (sender.hasPermission("aquafishing.admin")) {
            sender.sendMessage(MessageUtil.parse("  <aqua>/fish start [saniye] <gray>- Turnuvayı başlatır."));
            sender.sendMessage(MessageUtil.parse("  <aqua>/fish stop <gray>- Devam eden turnuvayı bitirir."));
            sender.sendMessage(MessageUtil.parse("  <aqua>/fish give rod <id> <gray>- Özel olta verir."));
            sender.sendMessage(MessageUtil.parse("  <aqua>/fish give fish <id> <gray>- Özel balık verir."));
            sender.sendMessage(MessageUtil.parse("  <aqua>/fish reload <gray>- Konfigürasyonları yeniler."));
        }
        sender.sendMessage(MessageUtil.parse("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("sell");
            completions.add("shop");
            completions.add("book");
            completions.add("top");
            completions.add("help");
            if (sender.hasPermission("aquafishing.admin")) {
                completions.add("start");
                completions.add("stop");
                completions.add("give");
                completions.add("reload");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("aquafishing.admin")) {
            return Arrays.asList("rod", "fish");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("rod") && sender.hasPermission("aquafishing.admin")) {
            List<String> rodIds = new ArrayList<>();
            for (me.furkankoc.aquafishing.rod.CustomRod r : plugin.getRodManager().getAllRods()) {
                rodIds.add(r.id());
            }
            return filter(rodIds, args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("fish") && sender.hasPermission("aquafishing.admin")) {
            List<String> fishIds = new ArrayList<>();
            for (FishTemplate ft : plugin.getFishManager().getAllFish()) {
                fishIds.add(ft.id());
            }
            return filter(fishIds, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start") && sender.hasPermission("aquafishing.admin")) {
            return filter(Arrays.asList("60", "120", "180", "300", "600"), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
