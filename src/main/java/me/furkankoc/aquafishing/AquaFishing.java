package me.furkankoc.aquafishing;

import me.furkankoc.aquafishing.codex.CodexManager;
import me.furkankoc.aquafishing.commands.FishCommand;
import me.furkankoc.aquafishing.contest.ContestManager;
import me.furkankoc.aquafishing.fish.FishManager;
import me.furkankoc.aquafishing.listeners.FishingListener;
import me.furkankoc.aquafishing.listeners.MenuListener;
import me.furkankoc.aquafishing.listeners.PlayerConnectionListener;
import me.furkankoc.aquafishing.minigame.MinigameManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AquaFishing extends JavaPlugin {

    private static AquaFishing instance;

    private FishManager fishManager;
    private ContestManager contestManager;
    private MinigameManager minigameManager;
    private CodexManager codexManager;
    private me.furkankoc.aquafishing.rod.RodManager rodManager;
    private me.furkankoc.aquafishing.pack.PackServer packServer;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize Managers
        this.fishManager = new FishManager(this);
        this.rodManager = new me.furkankoc.aquafishing.rod.RodManager(this);
        this.contestManager = new ContestManager(this);
        this.codexManager = new CodexManager(this);
        this.minigameManager = new MinigameManager(this);
        this.packServer = new me.furkankoc.aquafishing.pack.PackServer(this);
        this.packServer.start();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        // Register Commands
        PluginCommand fishCmd = getCommand("fish");
        if (fishCmd != null) {
            FishCommand cmd = new FishCommand(this);
            fishCmd.setExecutor(cmd);
            fishCmd.setTabCompleter(cmd);
        }

        getLogger().info("AquaFishing v" + getPluginMeta().getVersion() + " basariyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (minigameManager != null) {
            minigameManager.shutdown();
        }
        if (contestManager != null) {
            contestManager.shutdown();
        }
        if (codexManager != null) {
            codexManager.shutdown();
        }
        if (packServer != null) {
            packServer.stop();
        }
        getLogger().info("AquaFishing deaktif edildi.");
    }

    public static AquaFishing getInstance() {
        return instance;
    }

    public me.furkankoc.aquafishing.pack.PackServer getPackServer() {
        return packServer;
    }

    public FishManager getFishManager() {
        return fishManager;
    }

    public ContestManager getContestManager() {
        return contestManager;
    }

    public MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public CodexManager getCodexManager() {
        return codexManager;
    }

    public me.furkankoc.aquafishing.rod.RodManager getRodManager() {
        return rodManager;
    }
}
