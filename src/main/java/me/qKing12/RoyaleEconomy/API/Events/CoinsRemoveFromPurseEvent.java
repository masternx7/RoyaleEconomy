package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CoinsRemoveFromPurseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String player;
    private double coins;

    public CoinsRemoveFromPurseEvent(String player, double coins) {
        this.player = player;
        this.coins = coins;
    }

    public String getPlayer() {
        return this.player;
    }

    public double getRemovedCoins() {
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
