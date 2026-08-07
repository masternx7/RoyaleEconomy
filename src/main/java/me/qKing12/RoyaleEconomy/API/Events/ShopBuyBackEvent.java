package me.qKing12.RoyaleEconomy.API.Events;

import me.qKing12.RoyaleEconomy.Shops.Shop;
import me.qKing12.RoyaleEconomy.Shops.ShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ShopBuyBackEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private ShopMenu.ShopHistoryItem shopHistoryItem;
    private Shop shop;
    private boolean isCancelled=false;

    public ShopBuyBackEvent(Player player, ShopMenu.ShopHistoryItem shopHistoryItem, Shop shop) {
        this.player = player;
        this.shopHistoryItem=shopHistoryItem;
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

    public ShopMenu.ShopHistoryItem getShopHistoryItem() {
        return shopHistoryItem;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
