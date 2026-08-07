package me.qKing12.RoyaleEconomy.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreSharedBankDepositEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final double coins;
    private final String bankId;
    private boolean isCancelled=false;

    public PreSharedBankDepositEvent(String bankId, Player player, double coins) {
        this.player = player;
        this.coins = coins;
        this.bankId = bankId;
    }

    public PreSharedBankDepositEvent(String bankId, Player player, double coins, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.coins = coins;
        this.bankId = bankId;
    }

    public String getBankId() {
        return bankId;
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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
