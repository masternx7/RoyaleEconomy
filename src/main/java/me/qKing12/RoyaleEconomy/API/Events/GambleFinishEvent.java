package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GambleFinishEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private double coins;
    private boolean win;

    public GambleFinishEvent(Player player, double coins, boolean win) {
        this.player = player;
        this.coins = coins;
        this.win=win;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getCoins() {
        return this.coins;
    }

    public boolean isWin() {return this.win;}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

