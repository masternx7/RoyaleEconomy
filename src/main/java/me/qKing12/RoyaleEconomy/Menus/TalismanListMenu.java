package me.qKing12.RoyaleEconomy.Menus;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;



public class TalismanListMenu {
    private Inventory inventory;

    public TalismanListMenu(Player p, ArrayList<ItemStack> talismans){
        int size=talismans.size()/9;
        if(talismans.size()%9!=0)
            size++;
        inventory = Bukkit.createInventory(null, size*9);

        for(ItemStack talisman : talismans)
            inventory.addItem(talisman);

        // initial pusesem runNexTick, asa e peste tot
        RoyaleEconomy.plugin.getSchedulerLib().getScheduler().runAtEntity(p, (task) -> {
            p.openInventory(inventory);
            Bukkit.getPluginManager().registerEvents(new ClickListener(), RoyaleEconomy.plugin);
        });
    }

    private class ClickListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(!e.getInventory().equals(inventory))
                return;
            e.setCancelled(true);
            if(e.getSlot()<0 || e.getCurrentItem()==null) {
                return;
            }
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
                if(!e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
                    Player p = (Player) e.getWhoClicked();
                    p.getInventory().addItem(Utils.addNonStackablePropertyToItem(e.getCurrentItem()));
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
