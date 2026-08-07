package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEntityKillCoinsEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Entity killedEntity;
    private double coins;
    private boolean isCancelled=false;

    public CustomEntityKillCoinsEvent(Player player, Entity killedEntity, double coins) {
        super(true);
        this.player = player;
        this.killedEntity=killedEntity;
        this.coins=coins;
    }

    @Override
    public boolean isCancelled(){
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0){
        this.isCancelled=arg0;
    }

    public double getCoins(){
        return this.coins;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getKilledEntity() {
        return this.killedEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
