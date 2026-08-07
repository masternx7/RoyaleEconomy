package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RandomCoinsTalismanTrigger extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private double coins;
    private boolean isCancelled=false;

    public RandomCoinsTalismanTrigger(Player player, double coins) {
        this.player = player;
        this.coins = coins;
    }

    @Override
    public boolean isCancelled(){
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0){
        this.isCancelled=arg0;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getCoins() {
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
