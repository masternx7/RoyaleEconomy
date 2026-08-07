package me.qKing12.RoyaleEconomy.Shops.SellAll;

import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import me.qKing12.RoyaleEconomy.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SellWandMenu {
    private Inventory inventory;
    private Player player;

    public SellWandMenu(Player p){
        inventory= Bukkit.createInventory(null, 54, Utils.chat("&6&lSell Wands"));
        for(SellWand wand : SellWand.wands){
            ItemStack toAdd=wand.getWand(wand.getDefaultUses());
            ArrayList<String> lore=(ArrayList<String>)toAdd.getItemMeta().getLore();
            lore.add("");
            lore.add(Utils.chat("&8&m-------------------"));
            lore.add(Utils.chat("&fWand Name: &e"+wand.getName()));
            lore.add(Utils.chat("&eClick to get!"));
            toAdd.getItemMeta().setLore(lore);
            inventory.addItem(toAdd);
        }

        player=p;
        p.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(new ClickListen(), RoyaleEconomy.plugin);
    }

    private class ClickListen implements Listener{
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getSlot()<0 || e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            //if(inventory.getViewers().isEmpty()) RoyaleEconomy.plugin.getLogger().warning("A closed listener is still active.");
            if(e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                if (inventory.equals(e.getClickedInventory())) {
                    SellWand wand = SellWand.wands.get(e.getSlot());
                    player.getInventory().addItem(wand.getWand(wand.getDefaultUses()));
                }
            }
        }
    }

}
