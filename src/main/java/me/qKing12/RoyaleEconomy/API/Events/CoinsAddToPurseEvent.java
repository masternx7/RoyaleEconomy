package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CoinsAddToPurseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String player;
    private double coins;

    public CoinsAddToPurseEvent(String player, double coins) {
        this.player = player;
        this.coins = coins;
    }

    public String getPlayerString() {
        return this.player;
    }

    public double getAddedCoins() {
        return this.coins;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
