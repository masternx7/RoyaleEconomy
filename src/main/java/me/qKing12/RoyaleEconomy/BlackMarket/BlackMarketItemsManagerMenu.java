package me.qKing12.RoyaleEconomy.BlackMarket;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.Shops.*;
import me.qKing12.RoyaleEconomy.utils.PlayerMessageHandler;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class BlackMarketItemsManagerMenu {
    private ItemStack newItem;
    private Inventory inventory;
    private ItemStack background;

    int pages;
    int page;

    public BlackMarketItemsManagerMenu(Player p, int page){
        inventory = Bukkit.createInventory(null, 54, Utils.chat("&5&lBlack Market | Item Editor"));
        this.page=page;
        background = RoyaleEconomy.itemConstructor.getItemFromMaterial("160:15");
        ItemMeta meta=background.getItemMeta();
        meta.setDisplayName(" ");
        background.setItemMeta(meta);
        for(int i=1;i<8;i++) {
            inventory.setItem(i, background);
            inventory.setItem(i+45, background);
        }
        for(int i=0;i<54;i+=9) {
            inventory.setItem(i, background);
            inventory.setItem(i+8, background);
        }

        inventory.setItem(46, background);
        if(page==0)
            inventory.setItem(47, background);
        else
            inventory.setItem(47, RoyaleEconomy.staticValues.previousPageShopItem);

        Iterator<BlackMarketItem> blackItem = BlackMarketItem.blackMarketItems.iterator();

        pages=BlackMarketItem.blackMarketItems.size()/28;
        if(BlackMarketItem.blackMarketItems.size()%28!=0)
            pages++;

        for(int i=0;i<page*28;i++)
            blackItem.next();

        int index=page*28;
        while(inventory.firstEmpty()!=-1 && blackItem.hasNext()){
            ItemStack stack = blackItem.next().itemStack;
            ItemStack toDisplay;
            if(stack == null)
                toDisplay = new ItemStack(Material.BARRIER);
            else
                toDisplay = stack.clone();
            ArrayList<String> lore=new ArrayList<>();
            if(toDisplay.getItemMeta().getLore()!=null)
                lore=(ArrayList<String>)toDisplay.getItemMeta().getLore();
            lore.add(Utils.chat("&8&m-----------------"));
            lore.add(Utils.chat("&bLeft/Right Click&f to edit item"));
            meta = toDisplay.getItemMeta();
            meta.setLore(lore);
            toDisplay.setItemMeta(meta);
            NBTItem nbt = new NBTItem(toDisplay);
            nbt.setInteger("Index", index);

            inventory.setItem(inventory.firstEmpty(), nbt.getItem());
            index++;
        }

        if(inventory.firstEmpty()!=-1){
            ArrayList<String> lore = new ArrayList<>();
            lore.add(Utils.chat("&fClick to create a"));
            lore.add(Utils.chat("&fnew black market item!"));
            newItem = RoyaleEconomy.itemConstructor.getItem("351:8", Utils.chat("&aCreate Item"), lore);
            inventory.addItem(newItem);
        }
        else
            inventory.setItem(51, RoyaleEconomy.staticValues.nextPageShopItem);

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getCurrentItem().equals(background))
                    return;
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();

                    if(e.getCurrentItem().equals(RoyaleEconomy.staticValues.previousPageShopItem)){
                        new BlackMarketItemsManagerMenu(p, page-1);
                    }
                    else if(e.getCurrentItem().equals(RoyaleEconomy.staticValues.nextPageShopItem)){
                        new BlackMarketItemsManagerMenu(p, page+1);
                    }
                    else if(e.getCurrentItem().equals(newItem)){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the new item of the &ablack market&f!"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("It can only be one alphanumerical word."));
                        PlayerMessageHandler.messageSend(p, Utils.chat("&c&lATTENTION! &fThis is not the material."));
                        PlayerMessageHandler.messageSend(p, Utils.chat("This is just a name for config use."));
                        new ChatListener(p, (reply) ->{
                            reply = reply.replaceAll("[^A-Za-z0-9]+", "");

                                if(BlackMarketItem.getItemByName(reply)==null) {
                                    new BlackMarketItem(reply);
                                }else
                                    p.sendMessage(Utils.chat("&cItem already exists!"));
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new BlackMarketItemsManagerMenu(p, page));
                        }, false);
                    }
                    else{
                        new BlackMarketItemEditor(p, BlackMarketItem.blackMarketItems.get(new NBTItem(e.getCurrentItem()).getInteger("Index")), page);
                    }

                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                HandlerList.unregisterAll(this);
            }
        }

    }
}
