package me.qKing12.RoyaleEconomy.Shops;

import me.qKing12.RoyaleEconomy.DataManager.StaticValues;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
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

import java.util.ArrayList;

public class ShopItemsRequirementsMenu {
    private Inventory inventory;
    private Shop shop;
    private int page;
    private int index;
    private Player p;
    private String type;

    public ShopItemsRequirementsMenu(Player p, Shop shop, int page, int index, String type){
        this.shop=shop;
        this.page=page;
        this.p=p;
        this.type=type;
        this.index=index;
        inventory = Bukkit.createInventory(null, 45, Utils.chat("&8Items Req | "+type));
        ArrayList<ItemStack> items;
        if(type.equals("Main Display"))
            items = shop.getItems().get(index).getDefaultItemRequirements();
        else if(type.equals("Amount 1"))
            items = shop.getItems().get(index).getAmount1Items();
        else if(type.equals("Amount 2"))
            items = shop.getItems().get(index).getAmount2Items();
        else if(type.equals("Amount 3"))
            items = shop.getItems().get(index).getAmount3Items();
        else if(type.equals("Amount 4"))
            items = shop.getItems().get(index).getAmount4Items();
        else
            items = shop.getItems().get(index).getAmount5Items();

        if(items!=null) {
            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(i, items.get(i));
            }
        }

        for(int i=36;i<45;i++){
            inventory.setItem(i, shop.getBackgroundGlass().clone());
        }

        inventory.setItem(40, RoyaleEconomy.staticValues.goBackShopEdit.clone());

        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runNextTick((task) -> p.openInventory(inventory));

        Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                if(e.getSlot()>35)
                    e.setCancelled(true);
                if(e.getSlot()==40)
                    p.closeInventory();
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getInventory().equals(inventory)){
                Shop.ShopItem shopItem = shop.getItems().get(index);
                ArrayList<ItemStack> items = new ArrayList<>();
                ItemStack[] contents = inventory.getContents();
                for(int i=0; i<36; i++){
                    ItemStack item = contents[i];
                    if(item!=null && !item.getType().equals(Material.AIR))
                        items.add(item);
                }
                if(type.equals("Main Display")){
                    shopItem.setDefaultItemRequirements(items);
                }
                else if(type.equals("Amount 1")){
                    shopItem.setAmount1Items(items);
                }
                else if(type.equals("Amount 2")){
                    shopItem.setAmount2Items(items);
                }
                else if(type.equals("Amount 3")){
                    shopItem.setAmount3Items(items);
                }
                else if(type.equals("Amount 4")){
                    shopItem.setAmount4Items(items);
                }
                else{
                    shopItem.setAmount5Items(items);
                }
                inventory=null;
                HandlerList.unregisterAll(this);
                shopItem.addToConfiguration();
                shop.saveConfig();
                new ShopItemEditMenu(p, shop, page, index);
                shop=null;
                p=null;
            }
        }

    }
}
