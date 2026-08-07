package me.qKing12.RoyaleEconomy.API.Events;

import me.qKing12.RoyaleEconomy.MultiCurrencyShops.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopBuyMultiCurrencyEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private double coinsPaid;
    private Shop.ShopItem shopItem;
    private Shop shop;
    private boolean isCancelled=false;
    private int itemStackAmount;

    public ShopBuyMultiCurrencyEvent(Player player, double paid, int itemStackAmount, Shop.ShopItem shopItem, Shop shop) {
        super(true);
        this.player = player;
        this.coinsPaid =  paid;
        this.shopItem=shopItem;
        this.shop=shop;
        this.itemStackAmount=itemStackAmount;
    }

    public Shop getShop() {
        return shop;
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

    public double getPaid() {
        return coinsPaid;
    }

    public int getItemStackAmount() {
        return itemStackAmount;
    }

    public Shop.ShopItem getShopItem(){
        return shopItem;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
