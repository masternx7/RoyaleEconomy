package me.qKing12.RoyaleEconomy.API.Events;

import me.qKing12.RoyaleEconomy.Shops.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ShopSellEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private double coinsGot;
    private ItemStack itemSold;
    private Shop shop;
    private boolean isCancelled=false;

    public ShopSellEvent(Player player, double paid, ItemStack itemSold, Shop shop) {
        this.player = player;
        this.coinsGot =  paid;
        this.itemSold=itemSold;
        this.shop=shop;
    }

    public Shop getShop() {
        return shop;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getCoins() {
        return coinsGot;
    }

    public void setCoinsGot(double coinsGot) {
        this.coinsGot = coinsGot;
    }

    public ItemStack getItemSold() {
        return itemSold;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
