package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PiggyBankClickEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private boolean sharedBank;
    private boolean isCancelled=false;

    public PiggyBankClickEvent(Player player, boolean sharedBank) {
        this.player = player;
        this.sharedBank = sharedBank;
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

    public boolean isSharedBank() {
        return this.sharedBank;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
