package me.furkankoc.aquafishing.contest;

import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.api.events.ContestEndEvent;
import me.furkankoc.aquafishing.api.events.ContestStartEvent;
import me.furkankoc.aquafishing.api.events.FishCaughtEvent;
import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContestManager {

    private final AquaFishing plugin;
    private ContestState state = ContestState.INACTIVE;

    private long totalDurationSeconds;
    private long remainingSeconds;

    private final Map<UUID, ContestEntry> entries = new ConcurrentHashMap<>();
    private ContestEntry currentLeader = null;

    private BukkitTask timerTask = null;
    private BukkitTask autoContestTask = null;
    private BossBar bossBar = null;

    public ContestManager(AquaFishing plugin) {
        this.plugin = plugin;
        startAutoContestScheduler();
    }

    public synchronized boolean startContest(long durationSeconds) {
        if (state == ContestState.ACTIVE) {
            return false;
        }

        ContestStartEvent startEvent = new ContestStartEvent(durationSeconds);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            return false;
        }

        this.state = ContestState.ACTIVE;
        this.totalDurationSeconds = startEvent.getDurationSeconds();
        this.remainingSeconds = this.totalDurationSeconds;
        this.entries.clear();
        this.currentLeader = null;

        // Broadcast start message
        String startMsg = plugin.getConfig().getString("messages.contest-start", "");
        startMsg = startMsg.replace("{duration}", MessageUtil.formatDuration(this.remainingSeconds));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(MessageUtil.parse(startMsg));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        // Initialize BossBar
        setupBossBar();

        // Start repeating 1-second countdown task
        this.timerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);

        return true;
    }

    public synchronized void stopContest() {
        if (state == ContestState.INACTIVE) {
            return;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        this.state = ContestState.INACTIVE;

        // Remove BossBar
        if (bossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
            bossBar = null;
        }

        // Calculate Winners
        List<ContestEntry> sortedEntries = getSortedLeaderboard();

        ContestEndEvent endEvent = new ContestEndEvent(sortedEntries);
        Bukkit.getPluginManager().callEvent(endEvent);

        // Distribute Rewards & Announce
        announceResults(sortedEntries);
    }

    private void tick() {
        remainingSeconds--;

        if (remainingSeconds <= 0) {
            stopContest();
            return;
        }

        updateBossBar();

        // Sound reminder on last 5 seconds
        if (remainingSeconds <= 5) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            }
        }
    }

    public boolean recordCatch(Player player, CaughtFish fish) {
        if (state != ContestState.ACTIVE) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        ContestEntry currentEntry = entries.get(uuid);
        boolean isBetter = (currentEntry == null || fish.weight() > currentEntry.bestFish().weight());

        if (isBetter) {
            ContestEntry newEntry = new ContestEntry(uuid, player.getName(), fish);
            entries.put(uuid, newEntry);

            boolean isNewLeader = false;
            if (currentLeader == null || fish.weight() > currentLeader.bestFish().weight()) {
                currentLeader = newEntry;
                isNewLeader = true;
            }

            FishCaughtEvent event = new FishCaughtEvent(player, fish, isNewLeader);
            Bukkit.getPluginManager().callEvent(event);

            if (isNewLeader) {
                String prefix = plugin.getConfig().getString("messages.prefix", "");
                String newRecordMsg = plugin.getConfig().getString("messages.new-first-place", "")
                        .replace("{prefix}", prefix)
                        .replace("{player}", player.getName())
                        .replace("{fish}", fish.template().name())
                        .replace("{weight}", MessageUtil.formatWeight(fish.weight()));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(MessageUtil.parse(newRecordMsg));
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            }

            updateBossBar();
            return true;
        }

        return false;
    }

    private void setupBossBar() {
        if (!plugin.getConfig().getBoolean("settings.bossbar.enabled", true)) {
            return;
        }

        String colorName = plugin.getConfig().getString("settings.bossbar.color", "BLUE").toUpperCase(Locale.ENGLISH);
        BossBar.Color barColor;
        try {
            barColor = BossBar.Color.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            barColor = BossBar.Color.BLUE;
        }

        bossBar = BossBar.bossBar(
                getBossBarTitle(),
                1.0f,
                barColor,
                BossBar.Overlay.PROGRESS
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(bossBar);
        }
    }

    private void updateBossBar() {
        if (bossBar == null) return;

        float progress = Math.max(0.0f, Math.min(1.0f, (float) remainingSeconds / totalDurationSeconds));
        bossBar.progress(progress);
        bossBar.name(getBossBarTitle());
    }

    private Component getBossBarTitle() {
        String template = plugin.getConfig().getString("settings.bossbar.title", "");
        String leaderName = (currentLeader != null) ? currentLeader.playerName() : "Yok";
        String leaderWeight = (currentLeader != null) ? MessageUtil.formatWeight(currentLeader.bestFish().weight()) : "0.00";

        String title = template
                .replace("{time}", MessageUtil.formatDuration(remainingSeconds))
                .replace("{leader}", leaderName)
                .replace("{weight}", leaderWeight);

        return MessageUtil.parse(title);
    }

    private void announceResults(List<ContestEntry> sortedEntries) {
        String endMsgTemplate = plugin.getConfig().getString("messages.contest-end", "");
        StringBuilder winnersSb = new StringBuilder();

        if (sortedEntries.isEmpty()) {
            winnersSb.append(plugin.getConfig().getString("messages.contest-no-winners", "  <red>Kimse balık tutamadı!"));
        } else {
            String winnerFormat = plugin.getConfig().getString("messages.winner-format", "");
            int rank = 1;
            for (ContestEntry entry : sortedEntries) {
                if (rank > 3) break;
                String line = winnerFormat
                        .replace("{rank}", String.valueOf(rank))
                        .replace("{player}", entry.playerName())
                        .replace("{fish}", entry.bestFish().template().name())
                        .replace("{weight}", MessageUtil.formatWeight(entry.bestFish().weight()));
                winnersSb.append(line).append("<newline>");

                // Give rewards
                giveReward(entry.playerName(), rank);
                rank++;
            }
        }

        String fullMsg = endMsgTemplate.replace("{winners}", winnersSb.toString().stripTrailing());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(MessageUtil.parse(fullMsg));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void giveReward(String playerName, int rank) {
        ConfigurationSection rewardSec = plugin.getConfig().getConfigurationSection("rewards." + rank);
        if (rewardSec == null) return;

        List<String> commands = rewardSec.getStringList("commands");
        for (String cmd : commands) {
            String finalCmd = cmd.replace("{player}", playerName);
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd));
        }
    }

    public List<ContestEntry> getSortedLeaderboard() {
        List<ContestEntry> list = new ArrayList<>(entries.values());
        Collections.sort(list);
        return list;
    }

    public void startAutoContestScheduler() {
        if (!plugin.getConfig().getBoolean("settings.auto-contest.enabled", true)) {
            return;
        }

        long intervalSec = plugin.getConfig().getLong("settings.auto-contest.interval-seconds", 3600);
        int minPlayers = plugin.getConfig().getInt("settings.auto-contest.min-players", 1);
        long defaultDuration = plugin.getConfig().getLong("settings.default-duration-seconds", 300);

        autoContestTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state == ContestState.INACTIVE && Bukkit.getOnlinePlayers().size() >= minPlayers) {
                startContest(defaultDuration);
            }
        }, intervalSec * 20L, intervalSec * 20L);
    }

    public void onPlayerJoin(Player player) {
        if (state == ContestState.ACTIVE && bossBar != null) {
            player.showBossBar(bossBar);
        }
    }

    public void onPlayerQuit(Player player) {
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void shutdown() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (autoContestTask != null) {
            autoContestTask.cancel();
            autoContestTask = null;
        }
        if (bossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
            bossBar = null;
        }
        state = ContestState.INACTIVE;
        entries.clear();
    }

    public ContestState getState() {
        return state;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
