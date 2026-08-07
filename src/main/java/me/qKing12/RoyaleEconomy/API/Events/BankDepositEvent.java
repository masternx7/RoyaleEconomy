package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankDepositEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private double coins;

    public BankDepositEvent(Player player, double coins) {
        super(true);
        this.player = player;
        this.coins = coins;
    }

    public Player getPlayer() {
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
