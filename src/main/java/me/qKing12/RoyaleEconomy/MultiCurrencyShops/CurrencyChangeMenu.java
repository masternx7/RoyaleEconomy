package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

import me.qKing12.RoyaleEconomy.MultiCurrency.internal.Currency;
import me.qKing12.RoyaleEconomy.MultiCurrency.internal.MultiCurrencyHandler;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;


public class CurrencyChangeMenu {
    private Inventory inventory;
    private Shop.ShopItem shopItem;
    private int page;
    private Shop shop;
    private int index;


    public CurrencyChangeMenu(Player p, Shop.ShopItem shopItem, Shop shop, int page, int index){
        this.shopItem=shopItem;
        this.shop=shop;
        this.page=page;
        this.index=index;
        inventory = Bukkit.createInventory(null, 54, Utils.chat("&8Change Currency"));

        ItemStack defaultCurrency = MultiCurrencyHandler.getDefaultCoinsIcon().clone();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(Utils.chat("&fThe default currency"));
        lore.add(Utils.chat("&fthat is used in regular shops."));
        lore.add("");
        lore.add(Utils.chat("&bClick to select!"));
        ItemMeta meta = defaultCurrency.getItemMeta();
        meta.setDisplayName(MultiCurrencyHandler.getDefaultCoinsColor()+MultiCurrencyHandler.getDefaultCoinsName());
        meta.setLore(lore);
        defaultCurrency.setItemMeta(meta);
        inventory.addItem(defaultCurrency);

        for(Currency currency : MultiCurrencyHandler.getCurrencies()){
            ItemStack item = currency.getIcon().clone();
            lore.clear();
            meta=item.getItemMeta();
            lore.add(Utils.chat("&fCurrency Value: "+MultiCurrencyHandler.getDefaultCoinsColor()+currency.getCoinsReference()+" "+MultiCurrencyHandler.getDefaultCoinsName()));
            lore.add("");
            lore.add(Utils.chat("&bClick to select!"));
            meta.setLore(lore);
            meta.setDisplayName(currency.getColor()+currency.getCurrencyName());
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();
                    if(e.getSlot()==0){
                        shopItem.changeCurrency(null);
                        shop.saveConfig();
                    }
                    else {
                        try{
                            shopItem.changeCurrency(MultiCurrencyHandler.getCurrencies().get(e.getSlot()-1));
                            shop.saveConfig();
                        }catch(Exception x){
                            x.printStackTrace();
                            p.sendMessage(Utils.chat("&cThere was an error setting the currency"));
                            p.closeInventory();
                            return;
                        }
                    }

                    PlayerMessageHandler.messageSend(p, Utils.chat("&aCurrency updated!"));

                    new ShopItemEditMenu(p, shop, page, index);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                HandlerList.unregisterAll(this);
            }
        }

    }
}
