package me.qKing12.RoyaleEconomy.MultiCurrencyShops;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.qKing12.RoyaleEconomy.InputGUIs.ChatListener;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
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



public class ShopEditorMenu  {
    private Inventory inventory;
    private Shop shop;
    private boolean modifications=false;
    int pages;

    private ItemStack newItem;

    int page;

    private int previousPageItemSlot = -1;
    private int nextPageItemSlot = -1;

    public ShopEditorMenu(Player p, Shop shop, int page){
        inventory = Bukkit.createInventory(null, 54, Utils.chat(shop.getTitle()));
        this.page=page;
        ItemStack background = shop.getBackgroundGlass().clone();
        this.shop=shop;
        for(int i=1;i<8;i++)
            inventory.setItem(i, background);
        for(int i=0;i<54;i+=9) {
            inventory.setItem(i, background);
            inventory.setItem(i+8, background);
        }
        inventory.setItem(49, RoyaleEconomy.staticValues.goBackShopEdit);
        inventory.setItem(50, RoyaleEconomy.staticValues.colorChange);
        inventory.setItem(0, RoyaleEconomy.staticValues.renameShop);
        inventory.setItem(8, RoyaleEconomy.staticValues.shopCloseCommands);
        inventory.setItem(48, RoyaleEconomy.staticValues.deleteShop);

        inventory.setItem(46, background);
        if(page==0)
            inventory.setItem(47, background);
        else {
            previousPageItemSlot = 47;
            inventory.setItem(47, RoyaleEconomy.staticValues.previousPageShopItem);
        }
        inventory.setItem(51, background);
        inventory.setItem(52, background);

        Iterator<Shop.ShopItem> shopItem = shop.getItems().iterator();

        pages=shop.getItems().size()/28;
        if(shop.getItems().size()%28!=0)
            pages++;

        for(int i=0;i<page*28;i++)
            shopItem.next();

        int index=page*28;
        while(inventory.firstEmpty()!=-1 && shopItem.hasNext()){
            ItemStack toDisplay = shopItem.next().getItemStack().clone();
            ArrayList<String> lore=new ArrayList<>();
            if(toDisplay.getItemMeta().getLore()!=null)
                lore=(ArrayList<String>)toDisplay.getItemMeta().getLore();
            lore.add(Utils.chat("&8&m-----------------"));
            lore.add(Utils.chat("&bLeft/Right Click&f to edit item"));
            lore.add(Utils.chat("&bLeft + Shift &fto move the item"));
            lore.add(Utils.chat("&fto the left"));
            lore.add(Utils.chat("&bRight + Shift &fto move the item"));
            lore.add(Utils.chat("&fto the right"));
            ItemMeta meta = toDisplay.getItemMeta();
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
            lore.add(Utils.chat("&fnew shop item!"));
            newItem = RoyaleEconomy.itemConstructor.getItem("351:8", Utils.chat("&aCreate Item"), lore);
            inventory.addItem(newItem);
        }
        else {
            nextPageItemSlot = 51;
            inventory.setItem(51, RoyaleEconomy.staticValues.nextPageShopItem);
        }

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
                boolean isNewItemClicked = false;
                try{
                    isNewItemClicked = newItem.getItemMeta().getLore().equals(e.getCurrentItem().getItemMeta().getLore());
                }catch(Exception x){}

                e.setCancelled(true);
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();

                    if(e.getSlot()==49){
                        new ShopManagerMenu(p);
                    }
                    else if(e.getSlot()==previousPageItemSlot){
                        new ShopEditorMenu(p, shop, page-1);
                    }
                    else if(e.getSlot()==nextPageItemSlot){
                        new ShopEditorMenu(p, shop, page+1);
                    }
                    else if(e.getSlot()==8){
                        new ShopCloseCommandsMenu(p, shop, page);
                    }
                    else if(e.getSlot()==0){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the new name of the &ashop&f!"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("It can only be one alphanumerical word."));
                        new ChatListener(p, (reply) ->{
                            reply = reply.replaceAll("[^A-Za-z0-9]+", "");

                                File newFile = new File(RoyaleEconomy.plugin.getDataFolder(), "/shopsMultiCurrency/"+reply+".yml");
                                if(newFile.exists())
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&cThat name is already used!"));
                                else {
                                    shop.renameShop(newFile);
                                    PlayerMessageHandler.messageSend(p, Utils.chat("&aName of the shop changed!"));
                                }
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new ShopEditorMenu(p, shop, page));

                        }, false);
                    }
                    else if(e.getSlot()==48){
                        if(e.getClick().equals(ClickType.DOUBLE_CLICK)){
                            shop.deleteShop();
                            p.closeInventory();
                            PlayerMessageHandler.messageSend(p, Utils.chat("&aShop deleted successfully!"));
                            new ShopManagerMenu(p);
                        }
                    }
                    else if(e.getSlot()==50){
                        new ColorChangeMenu(p, shop, page);
                    }
                    else if(isNewItemClicked){
                        p.closeInventory();
                        PlayerMessageHandler.messageSend(p, Utils.chat("&7&m-------------------------"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("Please enter the new item of the &ashop&f!"));
                        PlayerMessageHandler.messageSend(p, Utils.chat("It can only be one alphanumerical word."));
                        PlayerMessageHandler.messageSend(p, Utils.chat("&c&lATTENTION! &fThis is not the material."));
                        PlayerMessageHandler.messageSend(p, Utils.chat("This is just a name for config use."));
                        new ChatListener(p, (reply) ->{
                            reply = reply.replaceAll("[^A-Za-z0-9]+", "");

                                shop.createShopItem(reply, p);
                                RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> new ShopEditorMenu(p, shop, page));

                        }, false);
                    }
                    else if(e.getClick().equals(ClickType.SHIFT_LEFT)){
                        if(e.getCurrentItem().equals(shop.getBackgroundGlass()))
                            return;
                        int constant;
                        if(e.getSlot()%9==1)
                            constant=3;
                        else
                            constant=1;
                        if(e.getSlot()!=10){
                            modifications=true;
                            int index1;
                            ItemStack aux = e.getCurrentItem();
                            NBTItem nbt = new NBTItem(aux);
                            index1=nbt.getInteger("Index");
                            nbt.setInteger("Index", index1-1);
                            ItemStack aux2 = inventory.getItem(e.getSlot()-constant).clone();
                            inventory.setItem(e.getSlot()-constant, nbt.getItem());
                            nbt=new NBTItem(aux2);
                            nbt.setInteger("Index", index1);
                            inventory.setItem(e.getSlot(), nbt.getItem());
                            shop.rotateItems(index1-1, index1);
                        }
                        else if(page!=0){
                            modifications=true;
                            int index = new NBTItem(e.getCurrentItem()).getInteger("Index");
                            ItemStack toDisplay = shop.getItems().get(index-1).getItemStack();
                            ArrayList<String> lore=new ArrayList<>();
                            if(toDisplay.getItemMeta().getLore()!=null)
                                lore=(ArrayList<String>)toDisplay.getItemMeta().getLore();
                            lore.add(Utils.chat("&8&m-----------------"));
                            lore.add(Utils.chat("&bLeft/Right Click&f to edit item"));
                            lore.add(Utils.chat("&bLeft + Shift &fto move the item"));
                            lore.add(Utils.chat("&fto the left"));
                            lore.add(Utils.chat("&bRight + Shift &fto move the item"));
                            lore.add(Utils.chat("&fto the right"));
                            ItemMeta meta = toDisplay.getItemMeta();
                            meta.setLore(lore);
                            toDisplay.setItemMeta(meta);
                            NBTItem nbt=new NBTItem(toDisplay);
                            nbt.setInteger("Index", index);
                            inventory.setItem(e.getSlot(), nbt.getItem());
                            shop.rotateItems(index-1, index);
                        }
                    }
                    else if(e.getClick().equals(ClickType.SHIFT_RIGHT)){
                        if(e.getCurrentItem().equals(shop.getBackgroundGlass()))
                            return;
                        int constant;
                        if(e.getSlot()%9==7)
                            constant=3;
                        else
                            constant=1;
                        if(e.getSlot()!=43){
                            modifications=true;
                            int index1;
                            ItemStack aux = e.getCurrentItem();
                            NBTItem nbt = new NBTItem(aux);
                            index1=nbt.getInteger("Index");
                            if(index1==shop.getItems().size()-1)
                                return;
                            nbt.setInteger("Index", index1+1);
                            ItemStack aux2 = inventory.getItem(e.getSlot()+constant).clone();
                            inventory.setItem(e.getSlot()+constant, nbt.getItem());
                            nbt=new NBTItem(aux2);
                            nbt.setInteger("Index", index1);
                            inventory.setItem(e.getSlot(), nbt.getItem());
                            shop.rotateItems(index1+1, index1);
                        }
                        else if(page!=pages-1){
                            modifications=true;
                            int index = new NBTItem(e.getCurrentItem()).getInteger("Index");
                            ItemStack toDisplay = shop.getItems().get(index+1).getItemStack();
                            ArrayList<String> lore=new ArrayList<>();
                            if(toDisplay.getItemMeta().getLore()!=null)
                                lore=(ArrayList<String>)toDisplay.getItemMeta().getLore();
                            lore.add(Utils.chat("&8&m-----------------"));
                            lore.add(Utils.chat("&bLeft/Right Click&f to edit item"));
                            lore.add(Utils.chat("&bLeft + Shift &fto move the item"));
                            lore.add(Utils.chat("&fto the left"));
                            lore.add(Utils.chat("&bRight + Shift &fto move the item"));
                            lore.add(Utils.chat("&fto the right"));
                            ItemMeta meta = toDisplay.getItemMeta();
                            meta.setLore(lore);
                            toDisplay.setItemMeta(meta);
                            NBTItem nbt=new NBTItem(toDisplay);
                            nbt.setInteger("Index", index);
                            inventory.setItem(e.getSlot(), nbt.getItem());
                            shop.rotateItems(index+1, index);
                        }
                    }
                    else{
                        new ShopItemEditMenu(p, shop, page, new NBTItem(e.getCurrentItem()).getInteger("Index"));
                    }

                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                inventory=null;
                HandlerList.unregisterAll(this);
                if(modifications) {
                    shop.saveItems();
                }
            }
        }

    }
}
