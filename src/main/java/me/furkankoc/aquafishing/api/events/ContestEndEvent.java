package me.furkankoc.aquafishing.api.events;

import me.furkankoc.aquafishing.contest.ContestEntry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ContestEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final List<ContestEntry> winners;

    public ContestEndEvent(List<ContestEntry> winners) {
        this.winners = Collections.unmodifiableList(winners);
    }

    public List<ContestEntry> getWinners() {
        return winners;
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
