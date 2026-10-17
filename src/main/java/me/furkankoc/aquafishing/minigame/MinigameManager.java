package me.furkankoc.aquafishing.minigame;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.fish.FishRarity;
import me.furkankoc.aquafishing.rod.CustomRod;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinigameManager {

    private final AquaFishing plugin;
    private final Map<UUID, FishingSession> sessions = new ConcurrentHashMap<>();
    private BukkitTask tickerTask;

    public MinigameManager(AquaFishing plugin) {
        this.plugin = plugin;
        startTicker();
    }

    private void startTicker() {
        this.tickerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (FishingSession session : sessions.values()) {
                Player player = session.getPlayer();
                if (!player.isOnline() || session.getHook() == null || !session.getHook().isValid()) {
                    cancelSession(player);
                    continue;
                }

                session.tickTimer();

                // 1. İbrenin akıcı salınımı (Her 2 tickte bir adım)
                if (session.getTickCounter() % session.getSpeedTicks() == 0) {
                    session.advanceCursor();

                    // İbre yeşil alana girdiğinde hafif odak sesi
                    if (session.isHit() && session.getTickCounter() % 4 == 0) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.2f, 2.0f);
                    }
                }

                // 2. Suda balık çırpınma partikülleri
                Location hookLoc = session.getHook().getLocation();
                player.getWorld().spawnParticle(Particle.SPLASH, hookLoc, 3, 0.2, 0.1, 0.2, 0.05);

                // 3. Süre dolma kontrolü
                if (session.isEscaped()) {
                    failSession(player, "Zaman doldu! Balık misinadan kurtularak kaçtı.");
                    continue;
                }

                // 4. Görsel HUD render (Title + Actionbar + BossBar)
                renderSmoothHUD(session);
            }
        }, 1L, 1L);
    }

    public void startSession(Player player, org.bukkit.entity.FishHook hook, CaughtFish fish) {
        CustomRod rod = plugin.getRodManager().getEquippedRod(player);
        FishingSession session = new FishingSession(player, hook, fish, rod.powerBonus(), rod.speedMultiplier());

        BossBar.Color barColor = switch (fish.template().rarity()) {
            case LEGENDARY -> BossBar.Color.YELLOW;
            case EPIC -> BossBar.Color.PURPLE;
            case RARE -> BossBar.Color.BLUE;
            default -> BossBar.Color.GREEN;
        };

        BossBar bar = BossBar.bossBar(
                MessageUtil.parse("<gradient:#00c6ff:#0072ff><b>BALIK MÜCADELESİ!</b></gradient> <dark_gray>[" + rod.name() + "<dark_gray>] <gray>| " +
                        fish.template().rarity().getFormattedTag() + " <white>" + fish.template().name()),
                0.0f,
                barColor,
                BossBar.Overlay.PROGRESS
        );

        session.setBossBar(bar);
        player.showBossBar(bar);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.3f);

        sessions.put(player.getUniqueId(), session);
    }

    public boolean handleReel(Player player) {
        FishingSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }

        boolean hit = session.recordClick();

        if (hit) {
            // BAŞARILI YEŞİL VURUŞ!
            if (session.isCaught()) {
                successSession(player, session);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.2f);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.9f, 1.6f);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1.2, 0), 12, 0.3, 0.3, 0.3, 0.1);
            }
        } else {
            // ISKA!
            if (session.isEscaped()) {
                failSession(player, "Çok fazla ıska yaptın! Balık misinayı kopardı.");
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.9f, 0.6f);
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1.2, 0), 8, 0.2, 0.2, 0.2, 0.05);
            }
        }
        return true;
    }

    private void renderSmoothHUD(FishingSession session) {
        Player player = session.getPlayer();
        int len = session.getBarLength();
        int cursor = session.getCursor();
        int gStart = session.getGreenZoneStart();
        int gWidth = session.getGreenZoneWidth();
        int currentHits = session.getCurrentHits();
        int targetHits = session.getTargetHits();
        boolean isGreen = session.isHit();
        CustomRod rod = plugin.getRodManager().getEquippedRod(player);

        // 1. ÜST İBRE SATIRI (Cursor Indicator)
        StringBuilder cursorLine = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i == cursor) {
                if (isGreen) {
                    cursorLine.append("<gradient:#00ff88:#00ffff><b>[▼]</b></gradient>");
                } else {
                    cursorLine.append("<yellow><b>[▼]</b></yellow>");
                }
            } else {
                cursorLine.append(" ");
            }
        }

        // 2. MERKEZ HEDEF BARI (Green Zone Bar)
        StringBuilder barLine = new StringBuilder();
        barLine.append("<dark_gray>[ ");
        for (int i = 0; i < len; i++) {
            boolean inGreen = (i >= gStart && i < gStart + gWidth);
            if (i == cursor) {
                if (inGreen) {
                    barLine.append("<gradient:#ffffff:#ffff00><b>█</b></gradient>");
                } else {
                    barLine.append("<white><b>█</b></white>");
                }
            } else if (inGreen) {
                barLine.append("<green>█</green>");
            } else {
                barLine.append("<red>░</red>");
            }
        }
        barLine.append(" <dark_gray>]");

        // 3. TITLE & SUBTITLE
        String mainTitle;
        if (session.getLastFeedback() != null) {
            mainTitle = session.getLastFeedback();
        } else if (isGreen) {
            mainTitle = "<gradient:#00ff88:#00ffff><b>⚡ ŞİMDİ VUR! (SAĞ TIK) ⚡</b></gradient>";
        } else {
            mainTitle = "<gradient:#ffe259:#ffa751><b>⚡ BALIK MÜCADELESİ ⚡</b></gradient> <dark_gray>| " + rod.name();
        }

        String subTitle = barLine.toString();

        Title title = Title.title(
                MessageUtil.parse(mainTitle),
                MessageUtil.parse(subTitle),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(300), Duration.ZERO)
        );
        player.showTitle(title);

        // 4. ACTIONBAR (İsabet Sayacı & Kalan Haklar)
        StringBuilder hitProgress = new StringBuilder();
        hitProgress.append("<dark_gray>[ ");
        for (int h = 0; h < targetHits; h++) {
            if (h < currentHits) {
                hitProgress.append("<green>▰</green> ");
            } else {
                hitProgress.append("<dark_gray>▱</dark_gray> ");
            }
        }
        hitProgress.append("<dark_gray>]");

        int heartsRemaining = Math.max(0, session.getMaxMisses() - session.getMisses());
        StringBuilder hearts = new StringBuilder();
        for (int hr = 0; hr < session.getMaxMisses(); hr++) {
            if (hr < heartsRemaining) {
                hearts.append("<red>❤</red>");
            } else {
                hearts.append("<dark_gray>🖤</dark_gray>");
            }
        }

        String actionText = "<gold><b>İsabet:</b> " + hitProgress + " <yellow>(" + currentHits + "/" + targetHits + ")</yellow> " +
                "<dark_gray>• <yellow><b>[YEŞİLDE SAĞ TIKLA!]</b></yellow> " +
                "<dark_gray>• <gray>Hak: " + hearts + " " +
                "<dark_gray>• <gray>Süre: <yellow>" + session.getRemainingTimeSeconds() + "s";
        player.sendActionBar(MessageUtil.parse(actionText));

        // 5. BOSSBAR (İlerleme Çubuğu)
        if (session.getBossBar() != null) {
            float progressRatio = (float) currentHits / (float) targetHits;
            session.getBossBar().name(MessageUtil.parse(
                    "<gradient:#ffe259:#ffa751><b>AQUA FISHING</b></gradient> <dark_gray>» " +
                    session.getFish().template().rarity().getFormattedTag() + " <white>" + session.getFish().template().name() +
                    " <dark_gray>• <yellow>İsabet: " + currentHits + "/" + targetHits +
                    " <dark_gray>(" + session.getRemainingTimeSeconds() + "s)"
            ));
            session.getBossBar().progress(Math.min(1.0f, Math.max(0.0f, progressRatio)));
        }
    }

    private void successSession(Player player, FishingSession session) {
        sessions.remove(player.getUniqueId());
        if (session.getBossBar() != null) {
            player.hideBossBar(session.getBossBar());
        }

        CaughtFish fish = session.getFish();
        FishRarity rarity = fish.template().rarity();
        Location loc = player.getLocation();

        switch (rarity) {
            case LEGENDARY -> {
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
                player.playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1.5, 0), 100, 0.8, 1.2, 0.8, 0.2);
                player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 60, 0.5, 3.0, 0.5, 0.15);

                Title dragonTitle = Title.title(
                        MessageUtil.parse("<gradient:#ffe259:#ffa751><b>★ KADİM EFSANE YAKALANDI! ★</b></gradient>"),
                        MessageUtil.parse("<white>" + fish.template().name() + " <gray>(<yellow>" + MessageUtil.formatWeight(fish.weight()) + " kg<gray>)"),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofSeconds(1))
                );
                player.showTitle(dragonTitle);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(MessageUtil.parse("<newline><gold>★ ★ ★ <gradient:#ffe259:#ffa751><b>KADİM BALIK UYANDI!</b></gradient> ★ ★ ★<newline>  <yellow><b>" +
                            player.getName() + "</b> <gray>oltasıyla efsanelere konu olmuş <gradient:#ffe259:#ffa751><b>" + fish.template().name() + "</b></gradient> (" +
                            MessageUtil.formatWeight(fish.weight()) + " kg) yakaladı!<newline><gold>★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★ ★<newline>"));
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.4f);
                }
            }
            case EPIC -> {
                player.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.0f);
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
                player.getWorld().spawnParticle(Particle.WITCH, loc.clone().add(0, 1.5, 0), 50, 0.5, 0.8, 0.5, 0.1);
            }
            case RARE -> {
                player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
                player.getWorld().spawnParticle(Particle.SPLASH, loc.clone().add(0, 1.2, 0), 60, 0.4, 0.8, 0.4, 0.15);
            }
            default -> {
                player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.getWorld().spawnParticle(Particle.SPLASH, loc.clone().add(0, 1.0, 0), 20, 0.3, 0.3, 0.3, 0.05);
            }
        }

        // Balık eşyasını doğrudan oyuncu envanterine teslim et
        ItemStack fishItem = plugin.getFishManager().createFishItem(fish);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(fishItem);
        if (!leftover.isEmpty()) {
            for (ItemStack rem : leftover.values()) {
                player.getWorld().dropItemNaturally(loc, rem);
            }
            player.sendMessage(MessageUtil.parse("<yellow>Envanterin dolu olduğu için yakalanan balık yere düştü!"));
        }
        player.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);

        if (session.getHook() != null) {
            session.getHook().remove();
        }

        player.sendActionBar(MessageUtil.parse("<gradient:#00ff88:#00ffff><b>★ YAKALANDI! ★</b></gradient> <white>" + fish.template().name() + " (" + MessageUtil.formatWeight(fish.weight()) + " kg)"));

        plugin.getContestManager().recordCatch(player, fish);
        plugin.getCodexManager().recordCatch(player, fish);
    }

    private void failSession(Player player, String reason) {
        FishingSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.getBossBar() != null) {
            player.hideBossBar(session.getBossBar());
        }

        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        player.playSound(loc, Sound.BLOCK_CHAIN_BREAK, 1.0f, 1.0f);
        player.spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 25, 0.3, 0.4, 0.3, 0.05);

        player.sendActionBar(MessageUtil.parse("<red><b>KAÇTI!</b> <gray>" + reason));

        if (session != null && session.getHook() != null) {
            session.getHook().remove();
        }
    }

    public void cancelSession(Player player) {
        FishingSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.getBossBar() != null) {
            player.hideBossBar(session.getBossBar());
        }
    }

    public boolean hasActiveSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void shutdown() {
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
        for (FishingSession session : sessions.values()) {
            if (session.getBossBar() != null) {
                session.getPlayer().hideBossBar(session.getBossBar());
            }
        }
        sessions.clear();
    }
}
