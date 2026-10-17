package me.furkankoc.aquafishing.api.events;

import me.furkankoc.aquafishing.fish.CaughtFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FishCaughtEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final CaughtFish caughtFish;
    private final boolean newLeader;

    public FishCaughtEvent(Player player, CaughtFish caughtFish, boolean newLeader) {
        this.player = player;
        this.caughtFish = caughtFish;
        this.newLeader = newLeader;
    }

    public Player getPlayer() {
        return player;
    }

    public CaughtFish getCaughtFish() {
        return caughtFish;
    }

    public boolean isNewLeader() {
        return newLeader;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
