package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SharedBankCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String sharedBank;
    private boolean isCancelled=false;

    public SharedBankCreateEvent(Player player, String sharedBank) {
        super(true);
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

    public String getSharedBank() {
        return this.getSharedBank();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
